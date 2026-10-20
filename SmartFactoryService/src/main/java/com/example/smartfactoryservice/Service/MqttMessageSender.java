package com.example.smartfactoryservice.Service;


import com.example.smartfactoryservice.gateway.MqttGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MqttMessageSender {

    private final MqttGateway mqttGateway ;

    public void sendMsg(String topic , String msg) {
        mqttGateway.sendMsgToMqtt(topic , msg);
    }

    public void sendMsg(String topic , int qos ,  String msg) {
        mqttGateway.sendMsgToMqtt(topic , qos ,  msg);
    }

}
