package com.example.smartfactoryservice.Domain.Mqtt;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.Map;

@Data
public class SensorRealtimeMqttPayload {

    private String sn;

    private SensorType type;


    /**
     * 实时采集值会随着 type.sensor 变化：
     * 例如 25 -> {"temp":26,"humi":55}，33 -> {"monoxide":5}。
     */
    private Map<String, Object> value;

    @Data
    public static class SensorType {
        private String com;
        private String sensor;
    }
}
