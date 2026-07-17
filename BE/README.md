# Project Management System (Jira-like) - Backend

Dự án Backend cho hệ thống Quản lý Dự án (tương tự Jira) được phát triển bằng Spring Boot, phục vụ cho việc quản lý các dự án, thành viên, sprint, task, bình luận, và nhật ký hoạt động.

---

## 🛠️ Công nghệ sử dụng (Tech Stack)

*   **Ngôn ngữ:** Java 25
*   **Framework:** Spring Boot 4.1.0
*   **Database:** PostgreSQL (Lưu trữ dữ liệu)
*   **Database Migration:** Flyway (Tự động hóa nâng cấp lược đồ cơ sở dữ liệu)
*   **Security:** Spring Security + JWT (OAuth2 Resource Server)
*   **Object Mapping:** MapStruct 1.6.3 + Lombok (Chuyển đổi DTO - Entity tự động)
*   **Tài liệu API:** Springdoc OpenAPI 3.0 (Swagger UI)

---

## 🏗️ Kiến trúc & Tổ chức mã nguồn (Package-by-Feature)

Dự án được cấu trúc theo giải pháp **Package-by-Feature** nhằm tối ưu hóa tính độc lập giữa các tính năng nghiệp vụ, dễ dàng mở rộng và hạn chế tối đa xung đột khi ghép mã nguồn (Git merge conflicts) giữa các thành viên trong nhóm.

### Cấu trúc thư mục chi tiết

```text
fpt.qn.project_management_system
├── common/                  # Các lớp tiện ích dùng chung
│   ├── ApiResponse.java     # Cấu trúc dữ liệu phản hồi API tiêu chuẩn
│   ├── PageResponse.java    # Cấu trúc dữ liệu phân trang
│   ├── BaseEntity.java      # Lớp thực thể cha tự động cập nhật thời gian
│   ├── CustomException.java
│   └── GlobalExceptionHandler.java
│
├── config/                  # Các cấu hình hệ thống (Security, Jackson...)
├── security/                # Bộ lọc xác thực JWT & UserDetails
│
# --- Các gói tính năng (Feature Packages) ---
├── auth/                    # Xác thực người dùng (Đăng nhập, làm mới token)
├── user/                    # Quản lý tài khoản và phân quyền hệ thống
├── project/                 # Quản lý dự án
├── projectmember/           # Quản lý thành viên tham gia từng dự án
├── sprint/                  # Quản lý các chu kỳ chạy dự án (Sprint)
├── task/                    # Quản lý nhiệm vụ (Tasks, Bugs, Subtasks)
├── comment/                 # Quản lý bình luận trong task
├── activity/                # Ghi nhận nhật ký thay đổi của task (Audit log)
├── worklog/                 # Ghi nhận thời gian làm việc thực tế
├── dashboard/               # Dữ liệu thống kê tiến độ nhanh
└── report/                  # Xuất báo cáo hiệu suất
```

### Cấu trúc bên trong mỗi gói tính năng

Mỗi module nghiệp vụ tự đóng gói đầy đủ các lớp của nó để đảm bảo tính liên kết tối đa:

```text
[feature_package]/
├── controller/              # REST Controllers (Nhận request, validate dữ liệu)
├── dto/                     # Request/Response Data Transfer Objects
├── entity/                  # Các thực thể cơ sở dữ liệu (JPA Entities)
├── mapper/                  # Chuyển đổi dữ liệu Entity <=> DTO (MapStruct)
├── repository/              # Truy vấn cơ sở dữ liệu (Spring Data JPA)
└── service/                 # Logic nghiệp vụ chính (Interfaces & Impls)
```

---

## 🚀 Hướng dẫn Chạy & Phát triển

### Yêu cầu hệ thống (Prerequisites)
1.  **JDK 25** đã được cài đặt và cấu hình biến môi trường `JAVA_HOME`.
2.  **PostgreSQL** đang chạy trên máy cục bộ hoặc Docker.
3.  **Maven 3.9+** (hoặc sử dụng trình bao đóng `./mvnw` đi kèm).

### Các bước khởi chạy dự án

1.  **Cấu hình cơ sở dữ liệu:**
    Chỉnh sửa thông tin kết nối DB trong tệp [application.yaml](file:///home/hegoplay/Documents/workspace/FPT/java/Project-Management-System/BE/src/main/resources/application.yaml):
    ```yaml
    spring:
      datasource:
        url: jdbc:postgresql://localhost:5432/your_database_name
        username: your_username
        password: your_password
    ```

2.  **Biên dịch dự án:**
    Chạy lệnh sau từ thư mục gốc của BE để kiểm tra biên dịch và tải các thư viện phụ thuộc:
    ```bash
    ./mvnw clean compile
    ```

3.  **Khởi động ứng dụng:**
    Chạy lệnh dưới đây để khởi chạy Spring Boot:
    ```bash
    ./mvnw spring-boot:run
    ```

4.  **Tài liệu API (Swagger UI):**
    Sau khi ứng dụng khởi động thành công, bạn có thể xem và kiểm thử trực tiếp các API tại địa chỉ:
    [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 📌 Nguyên tắc phối hợp trong nhóm (Best Practices)

1.  **Giao tiếp một chiều:** Luôn tuân thủ luồng gọi code: `Controller` ➜ `Service` ➜ `Repository`. Không được gọi chéo ngoài luồng (Ví dụ: Controller gọi thẳng Repository).
2.  **Sử dụng ApiResponse:** Mọi API phản hồi dữ liệu ra ngoài phải được bọc trong lớp `ApiResponse<T>` để đồng bộ cấu trúc với Frontend.
3.  **Không dùng chung Entity làm Request/Response:** Luôn map dữ liệu qua lớp `DTO` tương ứng ở tầng Controller trước khi gửi đi hoặc nhận vào để tránh lộ cấu trúc DB.
4.  **Đặt tên gói viết thường:** Đặt tên gói (packages) viết thường hoàn toàn, không camelCase hoặc dùng dấu gạch dưới (Ví dụ: `projectmember` thay vì `projectMember`).
