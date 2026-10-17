package com.iot.dashboard.controller;

import com.iot.dashboard.entity.DataSensor;
import com.iot.dashboard.entity.Sensor;
import com.iot.dashboard.service.DataSensorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/datasensor")
@RequiredArgsConstructor
@Tag(name = "DataSensor", description = "Dữ liệu cảm biến - Dashboard realtime (UC01), Tìm kiếm & Lọc (UC03)")
public class DataSensorController {

    private final DataSensorService dataSensorService;

    /**
     * UC01 - Lấy 15 bản ghi gần nhất cho Dashboard
     */
    @Operation(summary = "Lấy 15 bản ghi cảm biến gần nhất (UC01)",
            description = "Trả về 15 bản ghi dữ liệu cảm biến mới nhất, dùng để hiển thị biểu đồ Dashboard realtime. " +
                    "Bao gồm cả Nhiệt độ, Độ ẩm và Ánh sáng.")
    @ApiResponse(responseCode = "200", description = "Thành công",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = DataSensor.class))))
    @GetMapping("/latest")
    public ResponseEntity<List<DataSensor>> getLatestData() {
        return ResponseEntity.ok(dataSensorService.getLatestData());
    }

    /**
     * UC03 - Tìm kiếm và Lọc dữ liệu cảm biến
     */
    @Operation(summary = "Tìm kiếm & Lọc dữ liệu cảm biến (UC03)",
            description = "Tìm kiếm dữ liệu cảm biến với các bộ lọc: loại cảm biến, khoảng thời gian. " +
                    "Hỗ trợ phân trang và sắp xếp.")
    @ApiResponse(responseCode = "200", description = "Thành công - trả về Page<DataSensor>")
    @GetMapping
    public ResponseEntity<Page<DataSensor>> searchData(
            @Parameter(description = "Loại cảm biến (Nhiệt độ / Độ ẩm / Ánh sáng)", example = "Nhiệt độ")
            @RequestParam(required = false) String sensorType,

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
                dataSensorService.searchData(sensorType, startTime, endTime, page, size, sortBy, sortDir));
    }

    /**
     * Danh sách tất cả cảm biến
     */
    @Operation(summary = "Lấy danh sách tất cả cảm biến", description = "Trả về danh sách các module cảm biến (DHT11 - Temp, DHT11 - Humid, LDR - Light)")
    @ApiResponse(responseCode = "200", description = "Thành công",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Sensor.class))))
    @GetMapping("/sensors")
    public ResponseEntity<List<Sensor>> getAllSensors() {
        return ResponseEntity.ok(dataSensorService.getAllSensors());
    }
}
