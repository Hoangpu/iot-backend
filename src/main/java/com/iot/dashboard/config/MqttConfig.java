package com.iot.dashboard.config;

import lombok.Getter;
import lombok.Setter;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ConfigurationProperties(prefix = "mqtt")
@Getter @Setter
@Slf4j
public class MqttConfig {

    private String brokerUrl;
    private String clientId;
    private String username;
    private String password;

    @Bean
    public MqttClient mqttClient() {
        try {
            MqttClient client = new MqttClient(brokerUrl, clientId + "-" + System.currentTimeMillis(), new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(20);

            // Set MQTT credentials nếu có
            if (username != null && !username.isEmpty()) {
                options.setUserName(username);
                options.setPassword(password.toCharArray());
            }

            client.connect(options);
            log.info("✅ Connected to MQTT Broker: {}", brokerUrl);
            return client;
        } catch (MqttException e) {
            log.warn("⚠️ Could not connect to MQTT Broker at {}. Running without MQTT. Error: {}",
                    brokerUrl, e.getMessage());
            // Return null - MQTT subscriber/publisher will handle gracefully
            return null;
        }
    }
}
