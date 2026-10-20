package com.example.smartfactoryservice.Config;

import com.example.smartfactoryservice.Domain.MqttConfigurationProperties;
import com.example.smartfactoryservice.mqtt.MqttMessageRouter;
import com.example.smartfactoryservice.mqtt.MqttSubscriptionTopicCollector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
@ConditionalOnProperty(prefix = "spring.mqtt", name = "enabled", havingValue = "true")
public class MqttInboundConfiguration {

    @Autowired
    private MqttConfigurationProperties mqttConfigurationProperties ;

    @Autowired
    private MqttPahoClientFactory mqttPahoClientFactory ;

    @Autowired
    private MqttMessageRouter mqttMessageRouter ;

    @Autowired
    private MqttSubscriptionTopicCollector mqttSubscriptionTopicCollector ;

    // 消息通道
    @Bean
    public MessageChannel messageInboundChannel() {
        return new DirectChannel();
    }

    // 配置入站适配器，作用：设置订阅主题，以及指定消息的相关属性
    @Bean
    public MessageProducer messageProducer() {
        String[] subscribedTopics = mqttSubscriptionTopicCollector.getTopics();
        if (subscribedTopics.length == 0) {
            throw new IllegalStateException("未找到任何 @MqttSubscribe 主题，无法初始化 MQTT 入站订阅");
        }

        // public MqttPahoMessageDrivenChannelAdapter(String url, String clientId, MqttPahoClientFactory clientFactory,
        //			String... topic)
        MqttPahoMessageDrivenChannelAdapter mqttPahoMessageDrivenChannelAdapter = new MqttPahoMessageDrivenChannelAdapter(
                mqttConfigurationProperties.getUrl() ,
                mqttConfigurationProperties.getSubClientId() ,
                mqttPahoClientFactory ,
                subscribedTopics
        ) ;

        mqttPahoMessageDrivenChannelAdapter.setQos(1);
        mqttPahoMessageDrivenChannelAdapter.setConverter(new DefaultPahoMessageConverter());
        mqttPahoMessageDrivenChannelAdapter.setOutputChannel(messageInboundChannel());
        return mqttPahoMessageDrivenChannelAdapter ;
    }

    @Bean
    @ServiceActivator(inputChannel = "messageInboundChannel")
    public MessageHandler messageHandler() {
        return mqttMessageRouter::route;
    }

}
