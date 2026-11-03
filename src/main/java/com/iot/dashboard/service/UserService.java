package com.iot.dashboard.service;

import com.iot.dashboard.entity.User;
import com.iot.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User getUserByStudentId(String studentId) {
        return userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("User not found with studentId: " + studentId));
    }

    public User getUserByUsernameOrEmail(String identifier) {
        return userRepository.findByStudentId(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElse(null);
    }

    public User login(String usernameOrEmail, String password) {
        User user = userRepository.findByEmail(usernameOrEmail)
                .orElseGet(() -> userRepository.findByStudentId(usernameOrEmail)
                        .orElseThrow(() -> new RuntimeException("Sai tài khoản hoặc mật khẩu")));

        boolean matches = passwordEncoder.matches(password, user.getPassword()) || password.equals(user.getPassword());
        if (!matches) {
            throw new RuntimeException("Sai tài khoản hoặc mật khẩu");
        }

        // Tự động nâng cấp mật khẩu plain-text cũ sang BCrypt
        if (password.equals(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
        }

        return user;
    }

    public User register(Map<String, String> data) {
        String email = data.get("email");
        String username = data.get("username");

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email đã được sử dụng");
        }
        if (userRepository.findByStudentId(username).isPresent()) {
            throw new RuntimeException("Tên tài khoản đã tồn tại");
        }

        User user = new User();
        user.setName(data.get("fullName"));
        user.setEmail(email);
        user.setStudentId(username);
        user.setPassword(passwordEncoder.encode(data.get("password")));

        return userRepository.save(user);
    }
}
