package com.iot.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Lệnh điều khiển thiết bị")
public class DeviceControlRequest {

    @Schema(description = "Hành động điều khiển: ON hoặc OFF", example = "ON", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"ON", "OFF"})
    private String action;

    @Schema(description = "Tên người thực hiện (tùy chọn)", example = "Mai Anh Hoàng")
    private String username;
}
