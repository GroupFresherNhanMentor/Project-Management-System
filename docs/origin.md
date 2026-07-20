# Software Requirement Specification (SRS)
## Project: Mini Project Management System (Jira-like)

---

## 1. Tổng quan dự án

### 1.1 Mục đích
Xây dựng hệ thống quản lý công việc nội bộ cho phép quản lý dự án, sprint, task, worklog và tiến độ thực hiện của thành viên trong nhóm.

**Hệ thống nhằm hỗ trợ:**
*   Quản lý dự án
*   Quản lý thành viên dự án
*   Theo dõi trạng thái công việc
*   Ghi nhận thời gian làm việc (Worklog)
*   Theo dõi tiến độ sprint
*   Thống kê và báo cáo

### 1.2 Phạm vi hệ thống
| Bao gồm (In Scope) | Không bao gồm (Out of Scope) |
| :--- | :--- |
| Quản lý người dùng, dự án, thành viên | Email Notification |
| Quản lý Sprint, Task, Comment, Worklog | Realtime Update |
| Dashboard cá nhân & Dashboard dự án | Chat nội bộ |
| Báo cáo Worklog & Tìm kiếm nâng cao | File Upload |
| Phân quyền theo vai trò (RBAC) | Kéo thả Kanban (Drag & Drop) |

---

## 2. Danh sách vai trò (Roles)

*   **Administrator (Admin):**
    *   Quản lý User (Tài khoản người dùng).
    *   Quản lý Project (Dự án).
    *   Xem toàn bộ dữ liệu trên hệ thống.
*   **Project Manager (PM):**
    *   Quản lý Project được phân công.
    *   Quản lý Sprint và Task trong dự án.
    *   Phân công (Assign) Task cho thành viên.
    *   Theo dõi tiến độ tổng thể của dự án.
*   **Developer (DEV):**
    *   Xem danh sách Task được giao.
    *   Cập nhật trạng thái Task đang thực hiện.
    *   Tạo Comment trao đổi.
    *   Log Work (Ghi nhận thời gian làm việc).

---

## 3. Chức năng chi tiết (Use Cases)

### UC01 - Đăng nhập
*   **Mô tả:** Người dùng đăng nhập hệ thống bằng tài khoản đã được cấp.
*   **Input:** `Username`, `Password` (Plain Text).
*   **Output:** JWT Access Token, User Information.
*   **Điều kiện:** Username tồn tại và Password chính xác.
*   **Kết quả:** Người dùng truy cập được hệ thống theo đúng Role của mình.

### UC02 - Quản lý User
*   **Mô tả:** Administrator quản lý toàn bộ tài khoản nhân viên trong hệ thống.
*   **Chức năng:** Tạo mới, Cập nhật, Khóa (Lock/Disable), Tìm kiếm và Xem danh sách User.
*   **Thông tin User gồm:** `Employee ID`, `Username`, `Full Name`, `Email`, `Role`, `Status`.

### UC03 - Tạo Project
*   **Mô tả:** Administrator cấu hình và tạo dự án mới.
*   **Thông tin Project gồm:** `Project Code`, `Project Name`, `Description`, `Start Date`, `End Date`, `Status`.
*   **Trạng thái Project:** `PLANNING`, `ACTIVE`, `ON_HOLD`, `COMPLETED`.

### UC04 - Quản lý thành viên dự án
*   **Mô tả:** Administrator hoặc Project Manager cấu hình nhân sự cho dự án.
*   **Chức năng:** Thêm thành viên, Xóa thành viên, Xem danh sách thành viên trong dự án.
*   **Thông tin lưu trữ:** `Project`, `User`, `Project Role`.
*   **Project Role hợp lệ:** `PM`, `DEV`, `TESTER`.

### UC05 - Quản lý Sprint
*   **Mô tả:** Project Manager lên kế hoạch và quản lý các vòng lặp phát triển (Sprint).
*   **Thông tin Sprint:** `Sprint Name`, `Goal`, `Start Date`, `End Date`.
*   **Trạng thái Sprint:** `PLANNED`, `ACTIVE`, `CLOSED`.
*   **Ràng buộc:** Tại một thời điểm, một dự án chỉ có tối đa **một** Sprint ở trạng thái `ACTIVE`.

### UC06 - Tạo Task
*   **Mô tả:** Project Manager tạo các hạng mục công việc (Task) cho dự án.
*   **Thông tin Task:** `Task Key`, `Summary`, `Description`, `Task Type`, `Priority`, `Assignee`, `Reporter`, `Story Point`, `Estimate Hour`, `Due Date`.
*   **Phân loại Task Type:** `STORY`, `TASK`, `BUG`.
*   **Mức độ ưu tiên (Priority):** `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`.
*   **Trạng thái mặc định:** `TODO`.

### UC07 - Cập nhật Task
*   **Mô tả:** Người dùng được phân công (Assignee) có quyền cập nhật tiến độ công việc.
*   **Trường được phép sửa:** `Status`, `Description`, `Estimate Hour`, `Due Date`.
*   **Luồng trạng thái (Workflow):**
    *   `TODO` $\rightarrow$ `IN_PROGRESS` $\rightarrow$ `TESTING` $\rightarrow$ `DONE`
    *   Hoặc chuyển ngược: `IN_PROGRESS` $\rightarrow$ `TODO`.

### UC08 - Gán Task
*   **Mô tả:** Project Manager giao việc cho thành viên.
*   **Điều kiện:** User được gán (Assignee) bắt buộc phải thuộc danh sách thành viên của Project đó (`project_members`).
*   **Kết quả:** Task được cập nhật thông tin `Assignee` mới.

### UC09 - Comment Task
*   **Mô tả:** Các thành viên trong dự án trao đổi, thảo luận trực tiếp trên Task.
*   **Thông tin lưu trữ:** `Task`, `Comment Content`, `Created By`, `Created Time`.
*   **Chức năng:** Tạo Comment, Xem danh sách Comment theo luồng thời gian.

### UC10 - Worklog (Ghi nhận thời gian)
*   **Mô tả:** Developer ghi nhận thời gian thực tế đã bỏ ra để xử lý từng Task.
*   **Thông tin lưu trữ:** `Task`, `Work Date`, `Hour`, `Description`.
    *   *Ví dụ:* Task `WEB-101`, Ngày `15/07/2026`, Số giờ `4`, Mô tả `Implement Login API`.
*   **Ràng buộc validation:** $0 < \text{Hour} \le 24$.

### UC11 - Theo dõi Activity History (Audit Trail)
*   **Mô tả:** Hệ thống tự động ghi lại vết các thay đổi quan trọng của Task để phục vụ kiểm toán dữ liệu.
*   **Các sự kiện cần lưu:** `Task Created`, `Status Changed`, `Priority Changed`, `Assignee Changed`, `Comment Added`.
*   **Thông tin lưu chi tiết:** `User`, `Action`, `Old Value`, `New Value`, `Created Time`.

### UC12 - Dashboard cá nhân
*   **Mô tả:** Màn hình tổng quan dành riêng cho từng Developer khi đăng nhập hệ thống.
*   **Thông tin hiển thị:**
    *   Danh sách công việc đang mở (My Open Tasks).
    *   Danh sách công việc đã hoàn thành (My Completed Tasks).
    *   Danh sách công việc trễ hạn (My Overdue Tasks).
    *   Tổng số giờ đã log (Total Logged Hours).

### UC13 - Dashboard dự án
*   **Mô tả:** Màn hình thống kê nhanh giúp Project Manager nắm bắt sức khỏe dự án.
*   **Thông tin hiển thị:**
    *   Tổng số lượng Task (Total Tasks).
    *   Biểu đồ Task phân theo trạng thái (Task By Status) và theo mức độ ưu tiên (Task By Priority).
    *   Tổng số giờ làm việc đã cống hiến cho dự án (Total Logged Hours).
    *   Tiến độ Sprint hiện tại (Sprint Progress).

### UC14 - Báo cáo Worklog
*   **Mô tả:** Xuất/Xem báo cáo tổng hợp thời gian làm việc phục vụ tính công hoặc đánh giá hiệu suất.
*   **Điều kiện lọc tìm kiếm:** `Project`, `User`, `From Date`, `To Date`.
*   **Thông tin kết quả:** `User`, `Total Hours`, `Number Of Tasks`.

### UC15 - Tìm kiếm Task
*   **Mô tả:** Bộ lọc tìm kiếm nâng cao đa điều kiện.
*   **Điều kiện lọc:** `Project`, `Sprint`, `Status`, `Priority`, `Assignee`, `Keyword` (Tìm theo Summary/Description).
*   **Kết quả:** Danh sách các Task thỏa mãn điều kiện.

---

## 4. Yêu cầu phi chức năng (Non-Functional Requirements)

*   **Bảo mật (Security):**
    *   Xác thực người dùng qua cơ chế **JWT Authentication** (Access Token & Refresh Token).
    *   Mã hóa mật khẩu một chiều bằng thuật toán **BCrypt**.
    *   Phân quyền hệ thống chặt chẽ dựa trên vai trò (**Role-based Authorization**).
*   **Hiệu năng (Performance):**
    *   Tất cả các API trả về danh sách dữ liệu bắt buộc phải hỗ trợ **phân trang (Pagination)** và sắp xếp (Sorting).
    *   Thời gian phản hồi (Response Time) của các API truy vấn nghiệp vụ phải **dưới 3 giây** với quy mô dữ liệu thử nghiệm khoảng **10.000 tasks**.
*   **Kiểm toán hệ thống (Audit):**
    *   Lưu lịch sử mọi biến động cấu hình và dữ liệu của Task thông qua bảng `task_activities`.
*   **Nhật ký hệ thống (Logging):**
    *   Cấu hình log tập trung cho toàn bộ Request đến hệ thống và bắt các ngoại lệ đột xuất (Exception Handling).

---

## 5. Kiến trúc cơ sở dữ liệu dự kiến (Database Schema)

Hệ thống sử dụng cơ sở dữ liệu quan hệ **PostgreSQL** với các bảng dự kiến sau:
1.  `users`: Lưu thông tin tài khoản nhân viên.
2.  `roles`: Danh mục vai trò trong hệ thống.
3.  `projects`: Danh sách các dự án.
4.  `project_members`: Bảng trung gian quản lý thành viên tham gia từng dự án và vai trò của họ trong dự án đó.
5.  `sprints`: Các giai đoạn (Sprint) phát triển của dự án.
6.  `tasks`: Các hạng mục công việc chi tiết.
7.  `task_comments`: Lưu bình luận trong Task.
8.  `worklogs`: Nhật ký ghi nhận thời gian làm việc của Developer.
9.  `task_activities`: Nhật ký Audit ghi lại toàn bộ lịch sử thay đổi thông tin Task.
10. `refresh_tokens`: Lưu vết mã định danh token phục vụ cơ chế giữ phiên đăng nhập an toàn.

---

## 6. Công nghệ áp dụng & Đánh giá độ khó

### 🛠 Công nghệ (Tech Stack)
*   **Backend:** Spring Boot (Java), Spring Security, REST API, JWT.
*   **Data Access Layer:** Spring Data JPA / Hibernate, PostgreSQL.
*   **Khác:** Validation API (JSR 380), Global Exception Handling, Spring Transaction.
*   **Frontend:** Angular (Xây dựng các màn hình CRUD cơ bản và Dashboard hiển thị số liệu trực quan).

### 📊 Đánh giá tổng quan
*   **Độ khó dự án:** **7/10**
*   **Điểm mấu chốt kỹ thuật:** Cần xử lý tốt các câu lệnh truy vấn phức tạp (Reporting Query) cho phần Dashboard/Report, thiết kế database chuẩn hóa để tối ưu hiệu năng dưới 3 giây, cấu hình luồng bảo mật chặt chẽ với JWT và xử lý Transaction đồng bộ dữ liệu history một cách toàn vẹn.