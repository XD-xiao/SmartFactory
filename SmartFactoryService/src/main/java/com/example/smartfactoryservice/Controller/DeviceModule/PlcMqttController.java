package com.example.smartfactoryservice.Controller.DeviceModule;

import com.example.smartfactoryservice.Domain.Mqtt.PlcSwitchMqttPayload;
import com.example.smartfactoryservice.Domain.Mqtt.PlcSyncMqttPayload;
import com.example.smartfactoryservice.Service.DeviceModule.PlcService;
import com.example.smartfactoryservice.mqtt.annotation.MqttController;
import com.example.smartfactoryservice.mqtt.annotation.MqttPayload;
import com.example.smartfactoryservice.mqtt.annotation.MqttSubscribe;
import com.example.smartfactoryservice.mqtt.annotation.MqttTopic;

import lombok.RequiredArgsConstructor;

@MqttController
@RequiredArgsConstructor
public class PlcMqttController {

    private final PlcService plcService;

    @MqttSubscribe("order/service/switch")
    public void getPlcSwitch(@MqttTopic String topic, @MqttPayload PlcSwitchMqttPayload payload) {
        System.out.println("Topic:" + topic);
        System.out.println("Payload:" + payload);
        plcService.saveRunTime(payload);
    }

    @MqttSubscribe("order/service/sync")
    public void getPlcSync(@MqttTopic String topic, @MqttPayload PlcSyncMqttPayload payload) {
        System.out.println("Topic:" + topic);
        System.out.println("Payload:" + payload);
        plcService.saveSyncRunTimes(payload);
    }
}
