package com.example.smartfactoryservice.mqtt;

import java.lang.reflect.Method;

public class MqttHandlerMethod {

    private final Object bean;
    private final Method method;
    private final String topicPattern;

    public MqttHandlerMethod(Object bean, Method method, String topicPattern) {
        this.bean = bean;
        this.method = method;
        this.topicPattern = topicPattern;
    }

    public Object getBean() {
        return bean;
    }

    public Method getMethod() {
        return method;
    }

    public String getTopicPattern() {
        return topicPattern;
    }
}
