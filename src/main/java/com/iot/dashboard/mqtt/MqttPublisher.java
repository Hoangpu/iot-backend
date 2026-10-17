package com.iot.dashboard.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MqttPublisher {

    @Autowired(required = false)
    private MqttClient mqttClient;

    @Value("${mqtt.topic.control:device/control}")
    private String controlTopic;

    /**
     * Publish lệnh điều khiển xuống ESP32
     * Topic: device/control
     * ESP32 nhận plain text: "ON", "OFF" cho LED 1; "ON2", "OFF2" cho LED 2
     * 
     * deviceId = 1 → LED 1 → "ON"/"OFF"
     * deviceId = 2 → LED 2 → "ON2"/"OFF2"
     * deviceId đặc biệt → ALL_ON/ALL_OFF (nếu cần)
     */
    public boolean publishControl(Integer deviceId, String action) {
        if (mqttClient == null || !mqttClient.isConnected()) {
            log.warn("⚠️ MQTT client not connected. Control command not sent to hardware.");
            return false;
        }

        try {
            // Map deviceId + action sang lệnh ESP32 hiểu
            String espCommand = mapToEspCommand(deviceId, action);

            MqttMessage message = new MqttMessage(espCommand.getBytes());
            message.setQos(1);
            message.setRetained(false);

            mqttClient.publish(controlTopic, message);
            log.info("📤 Published control command: {} (device={}, action={})", espCommand, deviceId, action);
            return true;
        } catch (Exception e) {
            log.error("Failed to publish MQTT control message: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Map deviceId và action sang lệnh ESP32
     * LED 1 (id=1): ON/OFF
     * LED 2 (id=2): ON2/OFF2
     * Các device khác: không gửi MQTT (chỉ lưu DB)
     */
    private String mapToEspCommand(Integer deviceId, String action) {
        boolean isOn = "ON".equalsIgnoreCase(action);

        return switch (deviceId) {
            case 0 -> isOn ? "ALL_ON" : "ALL_OFF";
            case 1 -> isOn ? "ON" : "OFF";
            case 2 -> isOn ? "ON2" : "OFF2";
            default -> {
                log.info("Device {} is not a hardware device, skipping MQTT", deviceId);
                yield action; // Gửi nguyên action cho các device khác
            }
        };
    }
}
