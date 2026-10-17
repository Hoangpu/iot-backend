-- Khởi tạo Database cho dự án IoT Dashboard
CREATE DATABASE IF NOT EXISTS iot_dashboard CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE iot_dashboard;

-- 1. Bảng lưu thông tin người dùng
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    student_id VARCHAR(20) NOT NULL UNIQUE,
    class_name VARCHAR(50),
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    avatar VARCHAR(255)
);

-- 2. Bảng lưu danh mục thiết bị (Đèn, Quạt...)
CREATE TABLE IF NOT EXISTS devices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL, -- 'SENSOR' hoặc 'ACTUATOR'
    is_active BOOLEAN DEFAULT TRUE
);

-- 3. Bảng lưu danh mục cảm biến
CREATE TABLE IF NOT EXISTS sensors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    sensor_type VARCHAR(50) NOT NULL, -- 'Nhiệt độ', 'Độ ẩm', 'Ánh sáng'
    is_active BOOLEAN DEFAULT TRUE
);

-- 4. Bảng lưu dữ liệu đo đạc từ cảm biến (Time-series data)
CREATE TABLE IF NOT EXISTS datasensor (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sensor_id INT NOT NULL,
    value DECIMAL(6,2) NOT NULL,
    time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sensor_id) REFERENCES sensors(id) ON DELETE CASCADE
);

-- 5. Bảng lưu lịch sử điều khiển bật/tắt thiết bị
CREATE TABLE IF NOT EXISTS history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    device_id INT NOT NULL,
    action VARCHAR(20) NOT NULL, -- 'ON' hoặc 'OFF'
    status VARCHAR(20) NOT NULL, -- 'SUCCESS' hoặc 'FAILED'
    time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
);

-- ==========================================
-- SEED DỮ LIỆU MẪU BAN ĐẦU
-- ==========================================

-- Thêm user Mai Anh Hoàng
INSERT IGNORE INTO users (id, name, student_id, class_name, email, password, avatar)
VALUES (1, 'Mai Anh Hoàng', 'B23DCCN332', 'D23CQCN', 'hoangmaianh@example.com', 'hashed_password_here', 'https://api.dicebear.com/7.x/avataaars/svg?seed=Hoang');

-- Thêm các thiết bị ngoại vi (Actuators)
INSERT IGNORE INTO devices (id, name, type, is_active) VALUES
(1, 'Đèn LED 1 (Phòng Khách)', 'ACTUATOR', FALSE),
(2, 'Quạt thông gió', 'ACTUATOR', FALSE);

-- Thêm các module cảm biến (Sensors)
INSERT IGNORE INTO sensors (id, name, sensor_type, is_active) VALUES
(1, 'Node DHT11 - Temp', 'Nhiệt độ', TRUE),
(2, 'Node DHT11 - Humid', 'Độ ẩm', TRUE),
(3, 'Node LDR - Light', 'Ánh sáng', TRUE);

-- Thêm một vài dữ liệu đo mẫu
INSERT IGNORE INTO datasensor (sensor_id, value, time) VALUES
(1, 26.5, DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
(2, 75.0, DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
(3, 450.0, DATE_SUB(NOW(), INTERVAL 5 MINUTE)),
(1, 26.8, DATE_SUB(NOW(), INTERVAL 4 MINUTE)),
(2, 74.5, DATE_SUB(NOW(), INTERVAL 4 MINUTE)),
(3, 460.0, DATE_SUB(NOW(), INTERVAL 4 MINUTE));

-- Thêm một vài lịch sử điều khiển mẫu
INSERT IGNORE INTO history (user_id, device_id, action, status, time) VALUES
(1, 1, 'ON', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(1, 1, 'OFF', 'SUCCESS', DATE_SUB(NOW(), INTERVAL 30 MINUTE));
