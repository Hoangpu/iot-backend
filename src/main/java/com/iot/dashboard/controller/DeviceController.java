package com.iot.dashboard.controller;

import com.iot.dashboard.dto.DeviceControlRequest;
import com.iot.dashboard.entity.Device;
import com.iot.dashboard.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Tag(name = "Device", description = "Quản lý thiết bị - Xem danh sách, Điều khiển bật/tắt (UC02)")
public class DeviceController {

    private final DeviceService deviceService;

    @Operation(summary = "Lấy danh sách tất cả thiết bị", description = "Trả về tất cả thiết bị (cả SENSOR và ACTUATOR)")
    @ApiResponse(responseCode = "200", description = "Thành công",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Device.class))))
    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices() {
        return ResponseEntity.ok(deviceService.getAllDevices());
    }

    @Operation(summary = "Lấy danh sách thiết bị điều khiển (Actuators)", description = "Chỉ trả về các thiết bị loại ACTUATOR (Đèn, Quạt...)")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/actuators")
    public ResponseEntity<List<Device>> getActuators() {
        return ResponseEntity.ok(deviceService.getActuators());
    }

    @Operation(summary = "Lấy thông tin thiết bị theo ID", description = "Trả về chi tiết 1 thiết bị theo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = Device.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy thiết bị")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(
            @Parameter(description = "ID thiết bị", example = "1") @PathVariable Integer id) {
        return ResponseEntity.ok(deviceService.getDeviceById(id));
    }

    /**
     * UC02 - Điều khiển bật/tắt thiết bị
     * Body: { "action": "ON" } hoặc { "action": "OFF" }
     */
    @Operation(summary = "Điều khiển bật/tắt thiết bị (UC02)",
            description = "Gửi lệnh ON/OFF cho thiết bị. ID=0 để điều khiển tất cả thiết bị cùng lúc. " +
                    "Lệnh sẽ được gửi qua MQTT xuống ESP32 và ghi lịch sử.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Điều khiển thành công"),
            @ApiResponse(responseCode = "400", description = "Action không hợp lệ (phải là ON hoặc OFF)")
    })
    @PostMapping("/{id}/control")
    public ResponseEntity<Map<String, Object>> controlDevice(
            @Parameter(description = "ID thiết bị (0 = tất cả)", example = "1") @PathVariable Integer id,
            @RequestBody DeviceControlRequest request) {
        String action = request.getAction();
        String username = request.getUsername();
        if (action == null || (!action.equalsIgnoreCase("ON") && !action.equalsIgnoreCase("OFF"))) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Action must be 'ON' or 'OFF'"
            ));
        }
        return ResponseEntity.ok(deviceService.controlDevice(id, action, username));
    }
}
