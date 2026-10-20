package com.example.smartfactoryservice;

import com.example.smartfactoryservice.Service.AgentModule.AIAgentService;
import com.example.smartfactoryservice.Service.MqttMessageSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SmartFactoryServiceApplicationTests {

    @Autowired
    private MqttMessageSender mqttMessageSender;

    @Autowired
    private AIAgentService aiAgentService;

    @Test
    void contextLoads() {
        // mqttMessageSender.sendMsg("testa", "test message");
    }

    @Test
    void testDeepSeekChat() {
        String reply = aiAgentService.chat("你好，请用一句话介绍你自己");
        System.out.println("DeepSeek 回复: " + reply);
    }

    @Test
    void testDeepSeekChatWithSystem() {
        String reply = aiAgentService.chatWithSystem(
                "你是一个工厂设备诊断专家，请用专业简洁的语言回答",
                "设备温度过高可能是什么原因？"
        );
        System.out.println("DeepSeek 回复: " + reply);
    }

}
