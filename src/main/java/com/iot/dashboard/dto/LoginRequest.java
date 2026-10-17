package com.iot.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Thông tin đăng nhập")
public class LoginRequest {

    @Schema(description = "Email hoặc Mã sinh viên", example = "hoangmaianh@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(description = "Mật khẩu", example = "hashed_password_here", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
