package com.iot.dashboard.service;

import com.iot.dashboard.entity.User;
import com.iot.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User getUserByStudentId(String studentId) {
        return userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("User not found with studentId: " + studentId));
    }

    public User login(String usernameOrEmail, String password) {
        User user = userRepository.findByEmail(usernameOrEmail)
                .orElseGet(() -> userRepository.findByStudentId(usernameOrEmail)
                        .orElseThrow(() -> new RuntimeException("Sai tài khoản hoặc mật khẩu")));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Sai tài khoản hoặc mật khẩu");
        }
        return user;
    }

    public User register(java.util.Map<String, String> data) {
        String email = data.get("email");
        String username = data.get("username"); // we'll use this as studentId since User entity doesn't have username

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
        user.setPassword(data.get("password"));

        return userRepository.save(user);
    }
}
