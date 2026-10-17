package com.iot.dashboard.repository;

import com.iot.dashboard.entity.DataSensor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DataSensorRepository extends JpaRepository<DataSensor, Integer> {

    // Lấy N bản ghi gần nhất (cho Dashboard chart)
    List<DataSensor> findTop15ByOrderByTimeDesc();

    // Lọc theo loại cảm biến (UC03)
    @Query("SELECT d FROM DataSensor d WHERE d.sensor.sensorType = :sensorType ORDER BY d.time DESC")
    Page<DataSensor> findBySensorType(@Param("sensorType") String sensorType, Pageable pageable);

    // Tìm kiếm theo khoảng thời gian
    Page<DataSensor> findByTimeBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    // Tất cả + phân trang
    @Query("SELECT d FROM DataSensor d ORDER BY d.time DESC")
    Page<DataSensor> findAllOrderByTimeDesc(Pageable pageable);

    // Tìm kiếm kết hợp: loại cảm biến + khoảng thời gian
    @Query("SELECT d FROM DataSensor d WHERE " +
           "(:sensorType IS NULL OR d.sensor.sensorType = :sensorType) AND " +
           "(:startTime IS NULL OR d.time >= :startTime) AND " +
           "(:endTime IS NULL OR d.time <= :endTime) " +
           "ORDER BY d.time DESC")
    Page<DataSensor> searchData(
            @Param("sensorType") String sensorType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
}
