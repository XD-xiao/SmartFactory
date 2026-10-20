package com.example.smartfactoryservice.Domain.Mqtt;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class SensorExperimentSwitchMqttPayload {

    @JSONField(name = "sceneId", alternateNames = {"id"})
    private Integer id;

    private List<Integer> sensorList;
}
