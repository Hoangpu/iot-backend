package com.iot.dashboard.mqtt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.dashboard.service.DataSensorService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class MqttSubscriber implements MqttCallback {

    @Autowired(required = false)
    private MqttClient mqttClient;

    private final DataSensorService dataSensorService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${mqtt.topic.sensor:device/sensor}")
    private String sensorTopic;

    @Value("${mqtt.topic.ack:device/status}")
    private String ackTopic;

    // Pattern để parse format ESP32: "[HH:MM:SS] ---> Nhiet do: 26.50, Do am: 75.00, Anh sang: 450"
    private static final Pattern ESP_SENSOR_PATTERN = Pattern.compile(
            "Nhiet do:\\s*([\\d.]+).*Do am:\\s*([\\d.]+).*Anh sang:\\s*(\\d+)",
            Pattern.CASE_INSENSITIVE
    );

    public MqttSubscriber(DataSensorService dataSensorService) {
        this.dataSensorService = dataSensorService;
    }

    @PostConstruct
    public void init() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.setCallback(this);
                mqttClient.subscribe(sensorTopic, 1);
                mqttClient.subscribe(ackTopic, 1);
                log.info("✅ Subscribed to MQTT topics: {}, {}", sensorTopic, ackTopic);
            } catch (Exception e) {
                log.error("Failed to subscribe to MQTT topics: {}", e.getMessage());
            }
        } else {
            log.warn("⚠️ MQTT client not available. Sensor data will only work via REST API.");
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("⚠️ MQTT connection lost: {}", cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload());
            log.info("📥 MQTT message on [{}]: {}", topic, payload);

            if (sensorTopic.equals(topic)) {
                handleSensorData(payload);
            } else if (ackTopic.equals(topic)) {
                handleDeviceAck(payload);
            }
        } catch (Exception e) {
            log.error("Error processing MQTT message: {}", e.getMessage());
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Not used for subscriber
    }

    /**
     * Xử lý dữ liệu cảm biến từ ESP32
     * Hỗ trợ 2 format:
     * 1. Plain text từ ESP: "[HH:MM:SS] ---> Nhiet do: 26.50, Do am: 75.00, Anh sang: 450"
     * 2. JSON: {"temperature": 26.5, "humidity": 75.0, "light": 450}
     */
    private void handleSensorData(String payload) {
        try {
            BigDecimal temp = null, humid = null, light = null;

            // Thử parse plain text format từ ESP32 trước
            Matcher matcher = ESP_SENSOR_PATTERN.matcher(payload);
            if (matcher.find()) {
                temp = new BigDecimal(matcher.group(1));
                humid = new BigDecimal(matcher.group(2));
                light = new BigDecimal(matcher.group(3));
                log.debug("Parsed ESP plain text: temp={}, humid={}, light={}", temp, humid, light);
            } else {
                // Fallback: thử parse JSON
                try {
                    JsonNode json = objectMapper.readTree(payload);
                    temp = json.has("temperature") ? json.get("temperature").decimalValue() : null;
                    humid = json.has("humidity") ? json.get("humidity").decimalValue() : null;
                    light = json.has("light") ? json.get("light").decimalValue() : null;
                } catch (Exception jsonEx) {
                    log.warn("⚠️ Could not parse sensor data as text or JSON: {}", payload);
                    return;
                }
            }

            if (temp != null && humid != null && light != null) {
                dataSensorService.saveBatchSensorData(temp, humid, light);
            } else {
                log.warn("⚠️ Incomplete sensor data: {}", payload);
            }
        } catch (Exception e) {
            log.error("Failed to parse sensor data: {}", e.getMessage());
        }
    }

    /**
     * Xử lý xác nhận trạng thái từ ESP32
     * Format: "[HH:MM:SS] ---> DEN 1 DA BAT" hoặc "[HH:MM:SS] ---> DEN 2 DA TAT"
     */
    private void handleDeviceAck(String payload) {
        log.info("✅ Device acknowledgment: {}", payload);
    }
}
