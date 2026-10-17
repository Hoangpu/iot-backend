package com.iot.dashboard.config;

import com.iot.dashboard.entity.DataSensor;
import com.iot.dashboard.entity.Device;
import com.iot.dashboard.entity.Sensor;
import com.iot.dashboard.entity.User;
import com.iot.dashboard.repository.DataSensorRepository;
import com.iot.dashboard.repository.DeviceRepository;
import com.iot.dashboard.repository.SensorRepository;
import com.iot.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tự động tạo dữ liệu mẫu khi database trống
 * (sensors, devices, user mặc định, dữ liệu cảm biến demo)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final SensorRepository sensorRepository;
    private final DeviceRepository deviceRepository;
    private final DataSensorRepository dataSensorRepository;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedUsers();
        seedSensors();
        seedDevices();
        seedSampleSensorData();
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            User user = User.builder()
                    .name("Nguyen Van A")
                    .studentId("B21DCCN001")
                    .className("D21CNPM01")
                    .email("nguyenvana@example.com")
                    .password("$2a$10$dummyhashedpassword123456789")
                    .build();
            userRepository.save(user);
            log.info("✅ Seeded default user: {}", user.getName());
        }
    }

    private void seedSensors() {
        if (sensorRepository.count() == 0) {
            sensorRepository.save(Sensor.builder().name("DHT11 - Nhiệt độ").sensorType("Nhiệt độ").isActive(true).build());
            sensorRepository.save(Sensor.builder().name("DHT11 - Độ ẩm").sensorType("Độ ẩm").isActive(true).build());
            sensorRepository.save(Sensor.builder().name("LDR - Ánh sáng").sensorType("Ánh sáng").isActive(true).build());
            log.info("✅ Seeded 3 sensors (Nhiệt độ, Độ ẩm, Ánh sáng)");
        }
    }

    private void seedDevices() {
        if (deviceRepository.count() == 0) {
            deviceRepository.save(Device.builder().name("LED 1").type("ACTUATOR").isActive(false).build());
            deviceRepository.save(Device.builder().name("LED 2").type("ACTUATOR").isActive(false).build());
            log.info("✅ Seeded 2 devices (LED 1, LED 2)");
        }

        // Delete Workstation Laptop directly from DB to clean up previous seeds
        try {
            jdbcTemplate.execute("DELETE FROM history WHERE device_id IN (SELECT id FROM devices WHERE name='Workstation Laptop')");
            jdbcTemplate.execute("DELETE FROM devices WHERE name='Workstation Laptop'");
        } catch (Exception e) {
            log.warn("Could not delete Workstation Laptop: {}", e.getMessage());
        }
    }

    private void seedSampleSensorData() {
        if (dataSensorRepository.count() == 0) {
            Sensor tempSensor = sensorRepository.findById(1).orElse(null);
            Sensor humidSensor = sensorRepository.findById(2).orElse(null);
            Sensor lightSensor = sensorRepository.findById(3).orElse(null);

            if (tempSensor == null || humidSensor == null || lightSensor == null) {
                log.warn("⚠️ Sensors not found, skipping sample data seeding");
                return;
            }

            LocalDateTime now = LocalDateTime.now();

            // 5 bộ dữ liệu mẫu cách nhau 5 giây
            double[] temps = {26.5, 26.8, 27.1, 26.9, 27.3};
            double[] humids = {62.0, 63.5, 61.8, 64.2, 63.0};
            double[] lights = {480, 520, 450, 510, 490};

            for (int i = 0; i < 5; i++) {
                LocalDateTime time = now.minusSeconds((4 - i) * 5L);
                dataSensorRepository.save(DataSensor.builder()
                        .sensor(tempSensor).value(BigDecimal.valueOf(temps[i])).time(time).build());
                dataSensorRepository.save(DataSensor.builder()
                        .sensor(humidSensor).value(BigDecimal.valueOf(humids[i])).time(time).build());
                dataSensorRepository.save(DataSensor.builder()
                        .sensor(lightSensor).value(BigDecimal.valueOf(lights[i])).time(time).build());
            }

            log.info("✅ Seeded 15 sample sensor data records");
        }
    }
}
