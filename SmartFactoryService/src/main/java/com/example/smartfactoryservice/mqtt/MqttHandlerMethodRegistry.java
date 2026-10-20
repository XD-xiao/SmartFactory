package com.example.smartfactoryservice.mqtt;

import com.example.smartfactoryservice.mqtt.annotation.MqttController;
import com.example.smartfactoryservice.mqtt.annotation.MqttPayload;
import com.example.smartfactoryservice.mqtt.annotation.MqttSubscribe;
import com.example.smartfactoryservice.mqtt.annotation.MqttTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MqttHandlerMethodRegistry implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(MqttHandlerMethodRegistry.class);

    private final ApplicationContext applicationContext;

    private List<MqttHandlerMethod> handlerMethods = List.of();

    public MqttHandlerMethodRegistry(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        List<MqttHandlerMethod> discoveredMethods = new ArrayList<>();
        Map<String, Object> controllerBeans = applicationContext.getBeansWithAnnotation(MqttController.class);

        for (Object controllerBean : controllerBeans.values()) {
            Class<?> targetClass = AopUtils.getTargetClass(controllerBean);
            ReflectionUtils.doWithMethods(targetClass, method -> registerHandlerMethod(controllerBean, method, discoveredMethods));
        }

        if (discoveredMethods.isEmpty()) {
            throw new IllegalStateException("未找到任何 @MqttSubscribe 方法，MQTT 入站订阅无法建立");
        }

        this.handlerMethods = List.copyOf(discoveredMethods);
        log.info("Registered {} MQTT handler methods for topics: {}", discoveredMethods.size(),
                discoveredMethods.stream().map(MqttHandlerMethod::getTopicPattern).distinct().collect(Collectors.toList()));
    }

    public Optional<MqttHandlerMethod> findHandler(String topic) {
        return handlerMethods.stream()
                .filter(handlerMethod -> MqttTopicMatcher.matches(handlerMethod.getTopicPattern(), topic))
                .findFirst();
    }

    private void registerHandlerMethod(Object controllerBean, Method method, List<MqttHandlerMethod> discoveredMethods) {
        MqttSubscribe mqttSubscribe = AnnotatedElementUtils.findMergedAnnotation(method, MqttSubscribe.class);
        if (mqttSubscribe == null) {
            return;
        }

        Method invocableMethod = AopUtils.selectInvocableMethod(method, controllerBean.getClass());
        ReflectionUtils.makeAccessible(invocableMethod);
        validateHandlerMethod(invocableMethod);

        for (String topic : mqttSubscribe.value()) {
            String normalizedTopic = normalizeTopic(topic, invocableMethod);
            discoveredMethods.add(new MqttHandlerMethod(controllerBean, invocableMethod, normalizedTopic));
        }
    }

    private void validateHandlerMethod(Method method) {
        int plainStringCount = 0;
        for (Parameter parameter : method.getParameters()) {
            boolean topicParameter = parameter.isAnnotationPresent(MqttTopic.class);
            boolean payloadParameter = parameter.isAnnotationPresent(MqttPayload.class);

            if (topicParameter && payloadParameter) {
                throw new IllegalStateException("参数不能同时标注 @MqttTopic 和 @MqttPayload: " + method);
            }

            Class<?> parameterType = parameter.getType();
            if (topicParameter) {
                if (!String.class.equals(parameterType)) {
                    throw new IllegalStateException("标注了 @MqttTopic 的参数必须是 String: " + method);
                }
                continue;
            }

            if (payloadParameter) {
                if (Message.class.isAssignableFrom(parameterType) || MessageHeaders.class.isAssignableFrom(parameterType)) {
                    throw new IllegalStateException("@MqttPayload 参数不支持 Message/MessageHeaders: " + method);
                }
                continue;
            }

            if (String.class.equals(parameterType)) {
                plainStringCount++;
                continue;
            }

            if (Message.class.isAssignableFrom(parameterType) || MessageHeaders.class.isAssignableFrom(parameterType)) {
                continue;
            }

            throw new IllegalStateException("不支持的 MQTT 方法参数类型: " + parameterType.getName() + " in " + method);
        }

        if (plainStringCount > 2) {
            throw new IllegalStateException("未标注的 String 参数最多支持两个(topic, payload): " + method);
        }
    }

    private String normalizeTopic(String topic, Method method) {
        if (topic == null || topic.isBlank()) {
            throw new IllegalStateException("@MqttSubscribe 主题不能为空: " + method);
        }
        return topic.trim();
    }
}
