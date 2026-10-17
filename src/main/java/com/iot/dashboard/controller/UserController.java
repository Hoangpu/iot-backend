package com.iot.dashboard.controller;

import com.iot.dashboard.dto.LoginRequest;
import com.iot.dashboard.dto.RegisterRequest;
import com.iot.dashboard.entity.User;
import com.iot.dashboard.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "Quản lý người dùng - Đăng nhập, Đăng ký, Thông tin cá nhân")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Lấy thông tin user theo ID", description = "Trả về thông tin chi tiết của người dùng theo ID (password được ẩn)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy user")
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(
            @Parameter(description = "ID của user", example = "1") @PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Lấy thông tin user theo Mã sinh viên", description = "Tìm kiếm user theo mã sinh viên (student_id)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy user")
    })
    @GetMapping("/student/{studentId}")
    public ResponseEntity<User> getUserByStudentId(
            @Parameter(description = "Mã sinh viên", example = "B23DCCN332") @PathVariable String studentId) {
        return ResponseEntity.ok(userService.getUserByStudentId(studentId));
    }

    @Operation(summary = "Đăng nhập", description = "Đăng nhập bằng email hoặc mã sinh viên + mật khẩu")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đăng nhập thành công",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Sai tài khoản hoặc mật khẩu")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.login(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Đăng ký tài khoản mới", description = "Tạo tài khoản mới với thông tin: họ tên, mã sinh viên, email, mật khẩu")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Đăng ký thành công",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Email hoặc tài khoản đã tồn tại")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            java.util.Map<String, String> data = new java.util.HashMap<>();
            data.put("fullName", request.getFullName());
            data.put("username", request.getUsername());
            data.put("email", request.getEmail());
            data.put("password", request.getPassword());
            User user = userService.register(data);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
