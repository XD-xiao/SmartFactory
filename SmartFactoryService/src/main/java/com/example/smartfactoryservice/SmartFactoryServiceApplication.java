package com.example.smartfactoryservice;

import com.example.smartfactoryservice.Domain.MqttConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;

@SpringBootApplication
@EnableIntegration
@IntegrationComponentScan
@EnableConfigurationProperties(value = MqttConfigurationProperties.class)
public class SmartFactoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartFactoryServiceApplication.class, args);
    }

}
