package com.iot.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Thông tin đăng ký tài khoản mới")
public class RegisterRequest {

    @Schema(description = "Họ và tên đầy đủ", example = "Mai Anh Hoàng", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fullName;

    @Schema(description = "Tên tài khoản (Mã sinh viên)", example = "B23DCCN332", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(description = "Địa chỉ email", example = "hoang@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "Mật khẩu", example = "mypassword123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
