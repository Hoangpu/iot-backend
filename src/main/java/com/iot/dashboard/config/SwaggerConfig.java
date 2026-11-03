package com.iot.dashboard.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IoT Dashboard API")
                        .description("REST API cho hệ thống IoT Dashboard - Quản lý thiết bị, cảm biến, lịch sử điều khiển. Yêu cầu JWT Bearer Token cho các API được bảo vệ.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Mai Anh Hoàng")
                                .email("hoangmaianh@example.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Nhập JWT Token nhận được từ API /api/users/login")))
                .tags(List.of(
                        new Tag().name("User").description("Quản lý người dùng - Đăng nhập, Đăng ký, Thông tin cá nhân"),
                        new Tag().name("Device").description("Quản lý thiết bị - Xem danh sách, Điều khiển bật/tắt (UC02)"),
                        new Tag().name("DataSensor").description("Dữ liệu cảm biến - Dashboard realtime (UC01), Tìm kiếm & Lọc (UC03)"),
                        new Tag().name("History").description("Lịch sử điều khiển - Tra cứu thao tác (UC05), Trạng thái cuối (UC04)")));
    }
}
