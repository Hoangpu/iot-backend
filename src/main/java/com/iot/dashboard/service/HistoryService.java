package com.iot.dashboard.service;

import com.iot.dashboard.entity.History;
import com.iot.dashboard.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryRepository historyRepository;

    /**
     * UC05 - Xem & Tra cứu lịch sử thao tác điều khiển thiết bị
     */
    public Page<History> searchHistory(Integer deviceId, String action,
                                        LocalDateTime startTime, LocalDateTime endTime,
                                        int page, int size,
                                        String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return historyRepository.searchHistory(deviceId, action, startTime, endTime, pageable);
    }

    /**
     * UC04 - Lấy trạng thái cuối cùng của thiết bị
     */
    public History getLatestByDevice(Integer deviceId) {
        return historyRepository.findLatestByDeviceId(deviceId);
    }
}
