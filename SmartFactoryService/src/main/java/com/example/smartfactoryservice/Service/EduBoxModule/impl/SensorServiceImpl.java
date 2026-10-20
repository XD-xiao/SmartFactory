package com.example.smartfactoryservice.Service.EduBoxModule.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.smartfactoryservice.Domain.Entity.DeviceModule.DeviceDevice;
import com.example.smartfactoryservice.Domain.Entity.EduBoxModule.SensorConfig;
import com.example.smartfactoryservice.Domain.Entity.EduBoxModule.SensorRecord;
import com.example.smartfactoryservice.Domain.Mqtt.SensorExperimentSwitchMqttPayload;
import com.example.smartfactoryservice.Domain.Mqtt.SensorRealtimeMqttPayload;
import com.example.smartfactoryservice.Mapper.DeviceModule.DeviceDeviceMapper;
import com.example.smartfactoryservice.Mapper.EduBoxModule.SensorConfigMapper;
import com.example.smartfactoryservice.Mapper.EduBoxModule.SensorRecordMapper;
import com.example.smartfactoryservice.Service.EduBoxModule.SensorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 传感器 MQTT 业务服务实现。
 * 负责实验场景切换和数据接收的完整处理流程。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SensorServiceImpl implements SensorService {

    private final DeviceDeviceMapper deviceDeviceMapper;
    private final SensorConfigMapper sensorConfigMapper;
    private final SensorRecordMapper sensorRecordMapper;

    /** 物理传感器编号：温湿度传感器（一次上报温度+湿度两个值） */
    private static final Long SENSOR_TEMP_HUMI = 25L;
    /** 拆分后的虚拟传感器编号：湿度 */
    private static final Long SENSOR_HUMIDITY = 99L;

    @Override
    public void handleExamSwitch(String topic, SensorExperimentSwitchMqttPayload payload) {
        String sn = extractSnFromTopic(topic);
        Integer sceneId = payload.getId();
        List<Integer> sensorList = payload.getSensorList();

        if (sn == null || sceneId == null || sensorList == null || sensorList.isEmpty()) {
            log.warn("切换实验类型消息参数不完整: topic={}, payload={}", topic, payload);
            return;
        }

        // 1. 校验设备表中是否存在该 SN
        DeviceDevice device = deviceDeviceMapper.selectOne(
                new LambdaQueryWrapper<DeviceDevice>().eq(DeviceDevice::getSn, sn));
        if (device == null) {
            log.warn("设备SN在设备表中不存在，忽略切换: sn={}", sn);
            return;
        }

        Long sceneType = sceneId.longValue();

        // 2. 场景切换仅记录日志，不操作配置表（配置表由前端管理）
        log.info("实验场景切换成功: sn={}, sceneType={}, sensorList={}", sn, sceneType, sensorList);
    }

    @Override
    public void handleSensorData(String topic, SensorRealtimeMqttPayload payload) {
        String sn = payload.getSn();
        SensorRealtimeMqttPayload.SensorType type = payload.getType();
        Map<String, Object> valueMap = payload.getValue();

        if (sn == null || type == null || type.getSensor() == null
                || valueMap == null || valueMap.isEmpty()) {
            System.out.println("传感器数据消息参数不完整，忽略");
            return;
        }

        Long sensorCode;
        try {
            sensorCode = Long.valueOf(type.getSensor());
        } catch (NumberFormatException e) {
            System.out.println("sensor 编号格式异常: "+ type.getSensor());
            return;
        }

        String com = type.getCom();
        LocalDateTime now = LocalDateTime.now();

        // 温湿度传感器转译：一条消息产出两条记录
        if (SENSOR_TEMP_HUMI.equals(sensorCode)) {
            Object tempVal = valueMap.get("temp");
            if (tempVal != null) {
                saveSensorRecord(sn, SENSOR_TEMP_HUMI, com, toInt(tempVal), now);
            }
            Object humiVal = valueMap.get("humi");
            if (humiVal != null) {
                saveSensorRecord(sn, SENSOR_HUMIDITY, com, toInt(humiVal), now);
            }
        } else {
            // 其他传感器：取 value map 中唯一的第一个值
            Object dataValue = valueMap.values().iterator().next();
            saveSensorRecord(sn, sensorCode, com, toInt(dataValue), now);
        }
    }

    /**
     * 查找配置、计算状态并保存记录。若无匹配配置则静默忽略。
     */
    private void saveSensorRecord(String sn, Long sensorCode, String com, int value, LocalDateTime now) {
        SensorConfig config = sensorConfigMapper.selectOne(
                new LambdaQueryWrapper<SensorConfig>()
                        .eq(SensorConfig::getSn, sn)
                        .eq(SensorConfig::getSensor, sensorCode)
                        .eq(SensorConfig::getDeleteFlag, false));

        if (config == null) {
            log.debug("未找到传感器配置，忽略数据: sn={}, sensor={}, value={}", sn, sensorCode, value);
            return;
        }

        SensorRecord record = new SensorRecord();
        record.setSensorId(config.getId());
        record.setCom(com);
        record.setValue(value);
        record.setStatus(calcStatus(value, config));
        record.setDatetime(now);
        sensorRecordMapper.insert(record);
    }

    /**
     * 根据传感器配置的阈值计算当前值所属状态。
     * -1: 超出上下限  0: 正常  1: 轻微  2: 警告  3: 危险
     */
    private int calcStatus(int value, SensorConfig config) {
        Integer max = config.getMax();
        Integer min = config.getMin();
        Integer maxTh = config.getMaxTh();
        Integer midTh = config.getMidTh();
        Integer minTh = config.getMinTh();

        if ((max != null && value > max) || (min != null && value < min)) {
            return -1;
        }
        if (max != null && maxTh != null && value > maxTh && value <= max) {
            return 3;
        }
        if (maxTh != null && midTh != null && value > midTh && value <= maxTh) {
            return 2;
        }
        if (midTh != null && minTh != null && value > minTh && value <= midTh) {
            return 1;
        }
        return 0;
    }

    /**
     * 从 topic 路径中提取 SN，格式: eduBox/server/examId/{sn}
     */
    private String extractSnFromTopic(String topic) {
        if (topic == null) return null;
        int lastSlash = topic.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < topic.length() - 1) {
            return topic.substring(lastSlash + 1);
        }
        return null;
    }

    private int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }
}
