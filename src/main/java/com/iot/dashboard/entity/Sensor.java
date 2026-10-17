package com.iot.dashboard.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sensors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "sensor_type", nullable = false, length = 50)
    private String sensorType; // 'Nhiệt độ', 'Độ ẩm', 'Ánh sáng'

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
