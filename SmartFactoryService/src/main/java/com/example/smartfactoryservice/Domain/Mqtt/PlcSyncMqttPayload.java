package com.example.smartfactoryservice.Domain.Mqtt;

import lombok.Data;

@Data
public class PlcSyncMqttPayload {

    private String sn;

    /** 各产线开关状态: 0-关, 1-开 */
    private int[] device;

    private Integer totalLamps;

    private int[] lamps;

    private String version;
}
