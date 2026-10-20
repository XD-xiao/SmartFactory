package com.example.smartfactoryservice.Domain.Mqtt;

import lombok.Data;

@Data
public class PlcSwitchMqttPayload {

    private Integer index;

    /** 0-关, 1-开 */
    private Integer action;

    private String sn;

    private Integer totalLamps;
}
