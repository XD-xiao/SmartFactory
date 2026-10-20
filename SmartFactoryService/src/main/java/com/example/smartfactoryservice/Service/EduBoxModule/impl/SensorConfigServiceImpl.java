package com.example.smartfactoryservice.Service.EduBoxModule.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.smartfactoryservice.Domain.Entity.DeviceModule.DeviceDevice;
import com.example.smartfactoryservice.Domain.Entity.EduBoxModule.SensorConfig;
import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Mapper.DeviceModule.DeviceDeviceMapper;
import com.example.smartfactoryservice.Mapper.EduBoxModule.SensorConfigMapper;
import com.example.smartfactoryservice.Service.EduBoxModule.SensorConfigService;
import com.example.smartfactoryservice.Service.MqttMessageSender;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 传感器配置与记录管理服务实现。
 * 每种传感器提供独立的配置获取、阈值修改、配置删除、记录分页查询方法。
 */
@Service
@RequiredArgsConstructor
public class SensorConfigServiceImpl implements SensorConfigService {

    private final SensorConfigMapper sensorConfigMapper;
    private final DeviceDeviceMapper deviceDeviceMapper;
    private final MqttMessageSender mqttMessageSender;

    @Override
    public SensorConfig getById(Long id) {
        return sensorConfigMapper.selectById(id);
    }

    @Override
    public PageBean pageList(int pageNum, int pageSize, String sn, Long sceneType, Long sensor) {
        LambdaQueryWrapper<SensorConfig> wrapper = new LambdaQueryWrapper<>();
        if (sn != null) {
            wrapper.eq(SensorConfig::getSn, sn);
        }
        if (sceneType != null) {
            wrapper.eq(SensorConfig::getSceneType, sceneType);
        }
        if (sensor != null) {
            wrapper.eq(SensorConfig::getSensor, sensor);
        }
        wrapper.eq(SensorConfig::getDeleteFlag, false);
        wrapper.orderByDesc(SensorConfig::getUpdateTime);

        Page<SensorConfig> page = sensorConfigMapper.selectPage(
                new Page<>(pageNum, pageSize), wrapper);

        return new PageBean(page.getTotal(), page.getRecords());
    }

    @Override
    public void save(SensorConfig config) {
        // 校验设备 sn 是否存在
        LambdaQueryWrapper<DeviceDevice> deviceWrapper = new LambdaQueryWrapper<>();
        deviceWrapper.eq(DeviceDevice::getSn, config.getSn());
        if (!deviceDeviceMapper.exists(deviceWrapper)) {
            throw new IllegalArgumentException("设备 sn=" + config.getSn() + " 不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        config.setCreateTime(now);
        config.setUpdateTime(now);
        config.setDeleteFlag(false);
        sensorConfigMapper.insert(config);
    }

    @Override
    public void update(SensorConfig config) {
        SensorConfig exist = sensorConfigMapper.selectById(config.getId());
        if (exist == null) {
            return;
        }

        // 1. 非阈值字段直接更新数据库
        boolean hasNonThreshold = false;
        if (config.getSn() != null) { exist.setSn(config.getSn()); hasNonThreshold = true; }
        if (config.getSensor() != null) { exist.setSensor(config.getSensor()); hasNonThreshold = true; }
        if (config.getSensorName() != null) { exist.setSensorName(config.getSensorName()); hasNonThreshold = true; }
        if (config.getSceneType() != null) { exist.setSceneType(config.getSceneType()); hasNonThreshold = true; }
        if (config.getValueType() != null) { exist.setValueType(config.getValueType()); hasNonThreshold = true; }
        if (config.getMax() != null) { exist.setMax(config.getMax()); hasNonThreshold = true; }
        if (config.getMin() != null) { exist.setMin(config.getMin()); hasNonThreshold = true; }

        if (hasNonThreshold) {
            exist.setUpdateTime(LocalDateTime.now());
            sensorConfigMapper.updateById(exist);
        }

        // 2. 阈值字段通过 MQTT 下发到设备，不直接写库
        Integer maxTh = config.getMaxTh();
        Integer midTh = config.getMidTh();
        Integer minTh = config.getMinTh();

        if (maxTh != null || midTh != null || minTh != null) {
            // 未传的阈值使用数据库当前值补全
            Integer sendMaxTh = maxTh != null ? maxTh : exist.getMaxTh();
            Integer sendMidTh = midTh != null ? midTh : exist.getMidTh();
            Integer sendMinTh = minTh != null ? minTh : exist.getMinTh();

            Map<String, Object> configMap = new HashMap<>();
            if (sendMaxTh != null) configMap.put("max_threshold", sendMaxTh);
            if (sendMidTh != null) configMap.put("mid_threshold", sendMidTh);
            if (sendMinTh != null) configMap.put("min_threshold", sendMinTh);

            Map<String, Object> payload = new HashMap<>();
            payload.put("source", 1);
            payload.put("sensor", config.getSensor() != null ? config.getSensor() : exist.getSensor());
            payload.put("config", configMap);

            mqttMessageSender.sendMsg("eduBox/device/examConfig", JSON.toJSONString(payload));
        }
    }

    @Override
    public void delete(Long id) {
        SensorConfig config = sensorConfigMapper.selectById(id);
        if (config != null) {
            config.setDeleteFlag(true);
            config.setUpdateTime(LocalDateTime.now());
            sensorConfigMapper.updateById(config);
        }
    }

    @Override
    public void updateThresholdFromMqtt(Integer sensor, Map<String, Object> config) {
        LambdaQueryWrapper<SensorConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SensorConfig::getSensor, sensor.longValue())
               .eq(SensorConfig::getDeleteFlag, false);
        java.util.List<SensorConfig> list = sensorConfigMapper.selectList(wrapper);
        if (list.isEmpty()) {
            return;
        }

        Object maxThObj = config.get("max_threshold");
        Object midThObj = config.get("mid_threshold");
        Object minThObj = config.get("min_threshold");

        for (SensorConfig exist : list) {
            if (maxThObj != null) exist.setMaxTh(toInt(maxThObj));
            if (midThObj != null) exist.setMidTh(toInt(midThObj));
            if (minThObj != null) exist.setMinTh(toInt(minThObj));
            exist.setUpdateTime(LocalDateTime.now());
            sensorConfigMapper.updateById(exist);
        }
    }

    private int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }
}
