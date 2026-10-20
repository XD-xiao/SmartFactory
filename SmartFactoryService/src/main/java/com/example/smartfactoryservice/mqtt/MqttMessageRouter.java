package com.example.smartfactoryservice.mqtt;

import com.alibaba.fastjson2.JSON;
import com.example.smartfactoryservice.mqtt.annotation.MqttPayload;
import com.example.smartfactoryservice.mqtt.annotation.MqttTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

@Component
public class MqttMessageRouter {

    private static final Logger log = LoggerFactory.getLogger(MqttMessageRouter.class);

    private final MqttHandlerMethodRegistry handlerMethodRegistry;

    public MqttMessageRouter(MqttHandlerMethodRegistry handlerMethodRegistry) {
        this.handlerMethodRegistry = handlerMethodRegistry;
    }

    public void route(Message<?> message) {
        Object topicHeader = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC);
        if (topicHeader == null) {
            throw new IllegalStateException("MQTT 消息缺少 mqtt_receivedTopic 头信息");
        }

        String topic = topicHeader.toString();
        String payload = message.getPayload() == null ? null : message.getPayload().toString();

        Optional<MqttHandlerMethod> handlerMethodOptional = handlerMethodRegistry.findHandler(topic);
        if (handlerMethodOptional.isEmpty()) {
            log.warn("No MQTT handler matched topic '{}', payload='{}'", topic, payload);
            return;
        }

        MqttHandlerMethod handlerMethod = handlerMethodOptional.get();
        Object[] args = buildArguments(handlerMethod.getMethod(), topic, payload, message);

        try {
            handlerMethod.getMethod().invoke(handlerMethod.getBean(), args);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "调用 MQTT 处理方法失败: " + handlerMethod.getMethod().getDeclaringClass().getSimpleName()
                            + "#" + handlerMethod.getMethod().getName(), ex);
        }
    }

    private Object[] buildArguments(Method method, String topic, String payload, Message<?> message) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        int plainStringCount = countPlainStringParameters(parameters);
        int stringBindingIndex = 0;

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(MqttTopic.class)) {
                args[i] = topic;
                continue;
            }
            if (parameter.isAnnotationPresent(MqttPayload.class)) {
                args[i] = resolvePayloadArgument(parameter, payload);
                continue;
            }

            Class<?> parameterType = parameter.getType();
            if (Message.class.isAssignableFrom(parameterType)) {
                args[i] = message;
                continue;
            }
            if (MessageHeaders.class.isAssignableFrom(parameterType)) {
                args[i] = message.getHeaders();
                continue;
            }
            if (String.class.equals(parameterType)) {
                if (plainStringCount <= 1) {
                    args[i] = payload;
                } else {
                    args[i] = stringBindingIndex == 0 ? topic : payload;
                    stringBindingIndex++;
                }
            }
        }

        return args;
    }

    private int countPlainStringParameters(Parameter[] parameters) {
        int plainStringCount = 0;
        for (Parameter parameter : parameters) {
            if (String.class.equals(parameter.getType())
                    && !parameter.isAnnotationPresent(MqttTopic.class)
                    && !parameter.isAnnotationPresent(MqttPayload.class)) {
                plainStringCount++;
            }
        }
        return plainStringCount;
    }

    private Object resolvePayloadArgument(Parameter parameter, String payload) {
        if (payload == null || String.class.equals(parameter.getType())) {
            return payload;
        }

        try {
            return JSON.parseObject(payload, parameter.getType());
        } catch (Exception ex) {
            throw new IllegalStateException("MQTT payload 反序列化失败: " + parameter.getType().getSimpleName(), ex);
        }
    }
}
