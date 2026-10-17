package com.iot.dashboard.controller;

import com.iot.dashboard.entity.History;
import com.iot.dashboard.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
@Tag(name = "History", description = "Lịch sử điều khiển - Tra cứu thao tác (UC05), Trạng thái cuối (UC04)")
public class HistoryController {

    private final HistoryService historyService;

    /**
     * UC05 - Xem & Tra cứu lịch sử thao tác điều khiển thiết bị
     */
    @Operation(summary = "Tìm kiếm lịch sử điều khiển thiết bị (UC05)",
            description = "Tra cứu lịch sử bật/tắt thiết bị với các bộ lọc: thiết bị, hành động, khoảng thời gian. " +
                    "Hỗ trợ phân trang và sắp xếp.")
    @ApiResponse(responseCode = "200", description = "Thành công - trả về Page<History>")
    @GetMapping
    public ResponseEntity<Page<History>> searchHistory(
            @Parameter(description = "ID thiết bị (tùy chọn)", example = "1")
            @RequestParam(required = false) Integer deviceId,

            @Parameter(description = "Loại hành động: ON hoặc OFF", example = "ON")
            @RequestParam(required = false) String action,

            @Parameter(description = "Thời gian bắt đầu (ISO format)", example = "2024-01-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,

            @Parameter(description = "Thời gian kết thúc (ISO format)", example = "2024-12-31T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,

            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Số bản ghi mỗi trang", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sắp xếp theo trường", example = "time")
            @RequestParam(defaultValue = "time") String sortBy,

            @Parameter(description = "Hướng sắp xếp: asc hoặc desc", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(
                historyService.searchHistory(deviceId, action, startTime, endTime, page, size, sortBy, sortDir));
    }

    /**
     * UC04 - Lấy trạng thái cuối cùng của thiết bị
     */
    @Operation(summary = "Lấy trạng thái cuối cùng của thiết bị (UC04)",
            description = "Trả về bản ghi lịch sử gần nhất của thiết bị để đồng bộ trạng thái ON/OFF hiện tại")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tìm thấy bản ghi cuối",
                    content = @Content(schema = @Schema(implementation = History.class))),
            @ApiResponse(responseCode = "204", description = "Thiết bị chưa có lịch sử điều khiển")
    })
    @GetMapping("/latest/{deviceId}")
    public ResponseEntity<History> getLatestByDevice(
            @Parameter(description = "ID thiết bị", example = "1") @PathVariable Integer deviceId) {
        History history = historyService.getLatestByDevice(deviceId);
        if (history == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(history);
    }
}
