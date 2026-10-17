package com.iot.dashboard.service;

import com.iot.dashboard.entity.Device;
import com.iot.dashboard.entity.History;
import com.iot.dashboard.entity.User;
import com.iot.dashboard.mqtt.MqttPublisher;
import com.iot.dashboard.repository.DeviceRepository;
import com.iot.dashboard.repository.HistoryRepository;
import com.iot.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final HistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MqttPublisher mqttPublisher;

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public List<Device> getActuators() {
        return deviceRepository.findByType("ACTUATOR");
    }

    public Device getDeviceById(Integer id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found with id: " + id));
    }

    /**
     * UC02 - Điều khiển bật/tắt thiết bị
     * UC04 - Đồng bộ trạng thái cuối
     */
    @Transactional
    public Map<String, Object> controlDevice(Integer deviceId, String action, String username) {
        if (deviceId == 0) {
            return controlAllDevices(action, username);
        }

        Device device = getDeviceById(deviceId);
        Map<String, Object> result = new HashMap<>();

        try {
            // Cập nhật trạng thái thiết bị
            boolean newState = "ON".equalsIgnoreCase(action);
            device.setIsActive(newState);
            deviceRepository.save(device);

            // Gửi lệnh điều khiển xuống ESP qua MQTT

            mqttPublisher.publishControl(deviceId, action.toUpperCase());

            // Ghi lịch sử
            User user = null;
            if (username != null && !username.trim().isEmpty()) {
                user = userRepository.findByName(username).orElseGet(() -> {
                    User newUser = User.builder()
                            .name(username)
                            .studentId("TEMP_" + System.currentTimeMillis() % 100000)
                            .email(System.currentTimeMillis() + "@temp.com")
                            .password("$2a$10$dummy")
                            .build();
                    return userRepository.save(newUser);
                });
            } else {
                user = userRepository.findById(1).orElse(null);
            }

            History history = History.builder()
                    .user(user)
                    .device(device)
                    .action(action.toUpperCase())
                    .status("SUCCESS")
                    .time(LocalDateTime.now())
                    .build();
            historyRepository.save(history);

            // Broadcast trạng thái mới qua WebSocket
            Map<String, Object> wsPayload = new HashMap<>();
            wsPayload.put("deviceId", device.getId());
            wsPayload.put("deviceName", device.getName());
            wsPayload.put("isActive", device.getIsActive());
            wsPayload.put("action", action.toUpperCase());
            wsPayload.put("time", LocalDateTime.now().toString());
            messagingTemplate.convertAndSend("/topic/device-status", wsPayload);

            result.put("success", true);
            result.put("device", device);
            result.put("message", "Device " + device.getName() + " turned " + action.toUpperCase());

            log.info("Device {} ({}) turned {}", device.getId(), device.getName(), action.toUpperCase());

        } catch (Exception e) {
            // Ghi lịch sử thất bại
            log.error("Failed to control device {}: {}", deviceId, e.getMessage());

            result.put("success", false);
            result.put("message", "Failed to control device: " + e.getMessage());
        }

        return result;
    }

    @Transactional
    public Map<String, Object> controlAllDevices(String action, String username) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean newState = "ON".equalsIgnoreCase(action);
            List<Device> actuators = getActuators();
            
            User user = null;
            if (username != null && !username.trim().isEmpty()) {
                user = userRepository.findByName(username).orElseGet(() -> {
                    User newUser = User.builder()
                            .name(username)
                            .studentId("TEMP_" + System.currentTimeMillis() % 100000)
                            .email(System.currentTimeMillis() + "@temp.com")
                            .password("$2a$10$dummy")
                            .build();
                    return userRepository.save(newUser);
                });
            } else {
                user = userRepository.findById(1).orElse(null);
            }

            for (Device device : actuators) {
                device.setIsActive(newState);
                deviceRepository.save(device);

                if (user != null) {
                    History history = History.builder()
                            .user(user)
                            .device(device)
                            .action(action.toUpperCase())
                            .status("SUCCESS")
                            .time(LocalDateTime.now())
                            .build();
                    historyRepository.save(history);
                }

                Map<String, Object> wsPayload = new HashMap<>();
                wsPayload.put("deviceId", device.getId());
                wsPayload.put("deviceName", device.getName());
                wsPayload.put("isActive", device.getIsActive());
                wsPayload.put("action", action.toUpperCase());
                wsPayload.put("time", LocalDateTime.now().toString());
                messagingTemplate.convertAndSend("/topic/device-status", wsPayload);
            }

            // Gửi lệnh MQTT
            mqttPublisher.publishControl(0, action);

            result.put("success", true);
            result.put("message", "All devices turned " + action.toUpperCase());
            log.info("All devices turned {}", action.toUpperCase());

        } catch (Exception e) {
            log.error("Failed to control all devices: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "Failed to control all devices: " + e.getMessage());
        }

        return result;
    }
}
