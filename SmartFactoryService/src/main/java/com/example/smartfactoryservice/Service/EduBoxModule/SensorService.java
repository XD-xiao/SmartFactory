package com.example.smartfactoryservice.Service.EduBoxModule;

import com.example.smartfactoryservice.Domain.Mqtt.SensorExperimentSwitchMqttPayload;
import com.example.smartfactoryservice.Domain.Mqtt.SensorRealtimeMqttPayload;

/**
 * 传感器 MQTT 业务服务。
 * 负责处理设备元数据、阈值配置和实时数据落库逻辑。
 */
public interface SensorService {

    /**
     * 处理实验场景切换：校验设备SN → 更新传感器配置表。
     */
    void handleExamSwitch(String topic, SensorExperimentSwitchMqttPayload payload);

    /**
     * 处理实时传感器数据：校验配置 → 数据转译 → 状态计算 → 记录落库。
     */
    void handleSensorData(String topic, SensorRealtimeMqttPayload payload);
}
