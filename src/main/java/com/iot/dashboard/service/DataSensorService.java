package com.iot.dashboard.service;

import com.iot.dashboard.entity.DataSensor;
import com.iot.dashboard.entity.Sensor;
import com.iot.dashboard.repository.DataSensorRepository;
import com.iot.dashboard.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataSensorService {

    private final DataSensorRepository dataSensorRepository;
    private final SensorRepository sensorRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * UC01 - Lấy 15 bản ghi gần nhất cho Dashboard chart
     */
    public List<DataSensor> getLatestData() {
        return dataSensorRepository.findTop15ByOrderByTimeDesc();
    }

    /**
     * UC03 - Tìm kiếm và lọc dữ liệu cảm biến
     */
    public Page<DataSensor> searchData(String sensorType, LocalDateTime startTime,
            LocalDateTime endTime, int page, int size,
            String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return dataSensorRepository.searchData(sensorType, startTime, endTime, pageable);
    }

    /**
     * Lưu dữ liệu cảm biến mới (gọi từ MQTT subscriber)
     * + Broadcast qua WebSocket cho dashboard
     */
    @Transactional
    public void saveSensorData(Integer sensorId, BigDecimal value) {
        Sensor sensor = sensorRepository.findById(sensorId)
                .orElseThrow(() -> new RuntimeException("Sensor not found: " + sensorId));

        DataSensor data = DataSensor.builder()
                .sensor(sensor)
                .value(value)
                .time(LocalDateTime.now())
                .build();
        dataSensorRepository.save(data);

        // Broadcast qua WebSocket
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", data.getId());
        payload.put("sensorId", sensor.getId());
        payload.put("sensorName", sensor.getName());
        payload.put("sensorType", sensor.getSensorType());
        payload.put("value", value);
        payload.put("time", data.getTime().toString());
        messagingTemplate.convertAndSend("/topic/sensor-data", payload);

        log.info("Sensor data saved: {} = {} at {}", sensor.getName(), value, data.getTime());
    }

    /**
     * Lưu batch (Nhiệt độ, Độ ẩm, Ánh sáng) từ 1 gói MQTT
     */
    @Transactional
    public void saveBatchSensorData(BigDecimal temperature, BigDecimal humidity, BigDecimal light) {
        saveSensorData(1, temperature); // DHT11 - Temp
        saveSensorData(2, humidity); // DHT11 - Humid
        saveSensorData(3, light); // LDR - Light
    }

    public List<Sensor> getAllSensors() {
        return sensorRepository.findAll();
    }
}
