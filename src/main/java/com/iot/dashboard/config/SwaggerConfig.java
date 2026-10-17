package com.iot.dashboard.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IoT Dashboard API")
                        .description("REST API cho hệ thống IoT Dashboard - Quản lý thiết bị, cảm biến, lịch sử điều khiển")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Mai Anh Hoàng")
                                .email("hoangmaianh@example.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development")))
                .tags(List.of(
                        new Tag().name("User").description("Quản lý người dùng - Đăng nhập, Đăng ký, Thông tin cá nhân"),
                        new Tag().name("Device").description("Quản lý thiết bị - Xem danh sách, Điều khiển bật/tắt (UC02)"),
                        new Tag().name("DataSensor").description("Dữ liệu cảm biến - Dashboard realtime (UC01), Tìm kiếm & Lọc (UC03)"),
                        new Tag().name("History").description("Lịch sử điều khiển - Tra cứu thao tác (UC05), Trạng thái cuối (UC04)")));
    }
}
