package com.example.smartfactoryservice.mqtt;

import com.example.smartfactoryservice.mqtt.annotation.MqttController;
import com.example.smartfactoryservice.mqtt.annotation.MqttSubscribe;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class MqttSubscriptionTopicCollector {

    private final List<String> topics;

    public MqttSubscriptionTopicCollector(ApplicationContext applicationContext) {
        this.topics = scanTopics(applicationContext);
    }

    public String[] getTopics() {
        return topics.toArray(String[]::new);
    }

    private List<String> scanTopics(ApplicationContext applicationContext) {
        if (!AutoConfigurationPackages.has(applicationContext)) {
            throw new IllegalStateException("无法确定 MQTT 注解扫描包路径");
        }

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.setEnvironment(applicationContext.getEnvironment());
        scanner.setResourceLoader(applicationContext);
        scanner.addIncludeFilter(new AnnotationTypeFilter(MqttController.class));

        Set<String> collectedTopics = new LinkedHashSet<>();
        ClassLoader classLoader = applicationContext.getClassLoader();

        for (String basePackage : AutoConfigurationPackages.get(applicationContext)) {
            for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage)) {
                String className = candidate.getBeanClassName();
                if (className == null) {
                    continue;
                }
                try {
                    Class<?> beanClass = ClassUtils.forName(className, classLoader);
                    ReflectionUtils.doWithMethods(beanClass, method -> {
                        MqttSubscribe mqttSubscribe = AnnotatedElementUtils.findMergedAnnotation(method, MqttSubscribe.class);
                        if (mqttSubscribe == null) {
                            return;
                        }
                        for (String topic : mqttSubscribe.value()) {
                            String normalizedTopic = normalizeTopic(topic, beanClass.getName(), method.getName());
                            collectedTopics.add(normalizedTopic);
                        }
                    });
                } catch (ClassNotFoundException ex) {
                    throw new IllegalStateException("加载 MQTT 控制器失败: " + className, ex);
                }
            }
        }

        return new ArrayList<>(collectedTopics);
    }

    private String normalizeTopic(String topic, String className, String methodName) {
        if (topic == null || topic.isBlank()) {
            throw new IllegalStateException("MQTT 订阅主题不能为空: " + className + "#" + methodName);
        }
        return topic.trim();
    }
}
