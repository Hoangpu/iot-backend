package com.iot.dashboard.repository;

import com.iot.dashboard.entity.History;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface HistoryRepository extends JpaRepository<History, Integer> {

    // Tất cả + phân trang
    @Query("SELECT h FROM History h ORDER BY h.time DESC")
    Page<History> findAllOrderByTimeDesc(Pageable pageable);

    // Lọc theo thiết bị
    @Query("SELECT h FROM History h WHERE h.device.id = :deviceId ORDER BY h.time DESC")
    Page<History> findByDeviceId(@Param("deviceId") Integer deviceId, Pageable pageable);

    // Tìm kiếm kết hợp
    @Query("SELECT h FROM History h WHERE " +
           "(:deviceId IS NULL OR h.device.id = :deviceId) AND " +
           "(:action IS NULL OR h.action = :action) AND " +
           "(:startTime IS NULL OR h.time >= :startTime) AND " +
           "(:endTime IS NULL OR h.time <= :endTime) " +
           "ORDER BY h.time DESC")
    Page<History> searchHistory(
            @Param("deviceId") Integer deviceId,
            @Param("action") String action,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    // Lấy bản ghi cuối cùng của thiết bị (UC04 - đồng bộ trạng thái)
    @Query(value = "SELECT * FROM history WHERE device_id = :deviceId ORDER BY time DESC LIMIT 1", nativeQuery = true)
    History findLatestByDeviceId(@Param("deviceId") Integer deviceId);
}
