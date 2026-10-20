package com.example.smartfactoryservice.Controller.DeviceModule;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.smartfactoryservice.Domain.Mqtt.SensorExperimentSwitchMqttPayload;
import com.example.smartfactoryservice.Domain.Mqtt.SensorRealtimeMqttPayload;
import com.example.smartfactoryservice.Mapper.DeviceModule.DeviceDeviceMapper;
import com.example.smartfactoryservice.Service.EduBoxModule.SensorConfigService;
import com.example.smartfactoryservice.Service.EduBoxModule.SensorService;
import com.example.smartfactoryservice.mqtt.annotation.MqttController;
import com.example.smartfactoryservice.mqtt.annotation.MqttPayload;
import com.example.smartfactoryservice.mqtt.annotation.MqttSubscribe;
import com.example.smartfactoryservice.mqtt.annotation.MqttTopic;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.smartfactoryservice.Domain.Entity.DeviceModule.DeviceDevice;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@MqttController
@RequiredArgsConstructor
public class SensorMqttController {

    private final SensorService sensorService;
    private final SensorConfigService sensorConfigService;
    private final DeviceDeviceMapper deviceDeviceMapper;

    /** 物理传感器编号：温湿度传感器（一次上报温度+湿度两个值） */
    private static final Long SENSOR_TEMP_HUMI = 25L;
    /** 拆分后的虚拟传感器编号：湿度 */
    private static final Long SENSOR_HUMIDITY = 99L;

    /** 当前会话已初始化的设备 SN → 允许接收的传感器编号集合 */
    private final Map<String, Set<Long>> initializedSnSensors = new ConcurrentHashMap<>();

    /**
     * 处理所有传感器实时数据消息。
     * 只有当前会话已接收过场景切换（examId）的设备数据才会被处理，
     * 且仅处理 sensorList 中声明的传感器编号。
     */
    @MqttSubscribe("eduBox/server/data/#")
    public void getSensorData(@MqttTopic String topic, @MqttPayload SensorRealtimeMqttPayload payload) {
        String sn = payload.getSn();
        if (sn == null || payload.getType() == null || payload.getType().getSensor() == null) {
//            System.out.println("[getSensorData] 丢弃：sn/type/sensor 为空, topic=" + topic + ", payload=" + payload);
            return;
        }

        // 当前会话未接收过该设备的场景切换消息，忽略
        Set<Long> allowedSensors = initializedSnSensors.get(sn);
        if (allowedSensors == null) {
//            System.out.println("[getSensorData] 丢弃：未收到设备 " + sn + " 的场景切换消息(examId), topic=" + topic);
            return;
        }

        Long sensorCode;
        try {
            sensorCode = Long.valueOf(payload.getType().getSensor());
        } catch (NumberFormatException e) {
//            System.out.println("[getSensorData] 丢弃：传感器编号解析失败, sn=" + sn + ", rawSensor=" + payload.getType().getSensor());
            return;
        }

        // 传感器编号不在场景声明的 sensorList 中，忽略
        if (!allowedSensors.contains(sensorCode)) {
//            System.out.println("[getSensorData] 丢弃：传感器编号 " + sensorCode + " 不在场景声明的 sensorList " + allowedSensors + " 中, sn=" + sn + ", topic=" + topic);
            return;
        }

//        System.out.println("[getSensorData] 放行：sn=" + sn + ", sensorCode=" + sensorCode + ", topic=" + topic);
        sensorService.handleSensorData(topic, payload);
    }

    /**
     * 处理实验场景切换消息：更新数据库配置，同时记录当前会话该设备的有效传感器列表。
     */
    @MqttSubscribe("eduBox/server/examId/#")
    public void getSensorConfig(@MqttTopic String topic, @MqttPayload SensorExperimentSwitchMqttPayload payload) {
        sensorService.handleExamSwitch(topic, payload);

        // 记录当前会话该设备允许接收的传感器编号（仅设备表中已注册的 SN 生效）
        String sn = extractSnFromTopic(topic);
        boolean deviceExists = sn != null
                && deviceDeviceMapper.exists(new LambdaQueryWrapper<DeviceDevice>().eq(DeviceDevice::getSn, sn));
        if (deviceExists && payload.getSensorList() != null) {
            Set<Long> sensorSet = ConcurrentHashMap.newKeySet();
            for (Integer sensorCode : payload.getSensorList()) {
                sensorSet.add(sensorCode.longValue());
                // 温湿度传感器拆分: 物理 sensor=25 → 同时允许 25(温度) + 99(湿度)
                if (sensorCode == 25) {
                    sensorSet.add(SENSOR_HUMIDITY);
                }
            }
            initializedSnSensors.put(sn, sensorSet);
        }
    }

    /**
     * 处理设备上报的传感器阈值配置消息，解析并更新数据库中的最大阈值。
     */
    @MqttSubscribe("eduBox/device/examConfig")
    public void getSensorThresholdConfig(@MqttTopic String topic, @MqttPayload String payload) {
        JSONObject json = JSON.parseObject(payload);
        // 仅处理 source=2（设备上报确认），source=1 为服务端下发，忽略
        int source = json.getIntValue("source");
        if (source != 2) {
            return;
        }
        int sensor = json.getIntValue("sensor");
        JSONObject configJson = json.getJSONObject("config");
        if (configJson == null || configJson.isEmpty()) {
            System.out.println("examConfig 消息中 config 为空，忽略: " + payload);
            return;
        }
        System.out.println( payload );
        Map<String, Object> config = configJson.toJavaObject(Map.class);
        sensorConfigService.updateThresholdFromMqtt(sensor, config);
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

}
