# SOFTWARE REQUIREMENTS SPECIFICATION (SRS)
## Mini Project Management System (Jira-like)

| Thông tin tài liệu | |
|---|---|
| Phiên bản | 1.0 |
| Trạng thái | Draft — chờ review |
| Nguồn | Biên soạn dựa trên `requirements.md` do khách hàng/PO cung cấp |
| Độ khó ước tính | 7/10 |

> **Ghi chú phương pháp luận:** các mục được đánh dấu **(*)** là yêu cầu được suy diễn hợp lý từ nghiệp vụ (cần thiết để hệ thống vận hành được) nhưng **không được nêu tường minh** trong tài liệu gốc — cần xác nhận lại với PO/stakeholder trước khi triển khai.

---

## 1. Giới thiệu

### 1.1 Mục đích tài liệu
Tài liệu này đặc tả đầy đủ, không nhập nhằng các yêu cầu chức năng và phi chức năng của hệ thống Mini Project Management System, làm cơ sở để đội phát triển thiết kế, xây dựng, kiểm thử và nghiệm thu sản phẩm.

### 1.2 Phạm vi sản phẩm
Hệ thống quản lý công việc nội bộ hỗ trợ: quản lý dự án, quản lý thành viên dự án, quản lý sprint, quản lý task, ghi nhận thời gian làm việc (worklog), theo dõi lịch sử thay đổi, và thống kê báo cáo tiến độ.

**Ngoài phạm vi (Out of scope):** Email Notification, Realtime Update, Chat, File Upload, Drag & Drop Kanban.

### 1.3 Đối tượng đọc tài liệu
Product Owner/khách hàng (rà soát yêu cầu), đội phát triển (BE/FE), QA/Tester, PM.

### 1.4 Định nghĩa, từ viết tắt

| Thuật ngữ | Ý nghĩa |
|---|---|
| SRS | Software Requirements Specification |
| FR | Functional Requirement — Yêu cầu chức năng |
| NFR | Non-Functional Requirement — Yêu cầu phi chức năng |
| UC | Use Case (đánh số theo tài liệu yêu cầu gốc) |
| JWT | JSON Web Token |
| PM | Project Manager (vai trò hệ thống) |
| System Role | Vai trò cấp hệ thống của User: `ADMIN` / `USER` (các quyền cụ thể trong dự án được phân cấp qua Project Role) |
| Project Role | Vai trò của User trong một Project cụ thể: PM / DEV / TESTER (khác với System Role — xem mục 2.6) |

### 1.5 Tổng quan tài liệu
Mục 2 mô tả tổng quan hệ thống. Mục 3 đặc tả chi tiết từng yêu cầu chức năng theo module, có mã hoá (FR-ID) và truy vết về Use Case gốc. Mục 4 đặc tả yêu cầu phi chức năng. Mục 5 đặc tả giao diện ngoài. Mục 6 mô tả mô hình dữ liệu. Mục 7 là ma trận truy vết yêu cầu. Mục 8 là phụ lục.

---

## 2. Mô tả tổng quan

### 2.1 Bối cảnh sản phẩm
Hệ thống độc lập (standalone), phục vụ nội bộ tổ chức, kiến trúc REST API (Spring Boot) + SPA Frontend (Angular), dữ liệu lưu trên PostgreSQL.

### 2.2 Tổng quan chức năng

| Nhóm chức năng | Mô tả ngắn |
|---|---|
| Authentication | Đăng nhập, cấp JWT |
| User Management | Quản lý tài khoản người dùng |
| Project Management | Quản lý dự án |
| Project Member Management | Quản lý thành viên trong dự án |
| Sprint Management | Quản lý sprint theo dự án |
| Task Management | Tạo, cập nhật, gán, tìm kiếm task |
| Comment | Trao đổi trên task |
| Worklog | Ghi nhận thời gian làm việc và báo cáo worklog |
| Activity History | Ghi nhận lịch sử thay đổi task |
| Dashboard | Thống kê tiến độ cá nhân/dự án |

### 2.3 Vai trò người dùng và quyền hạn

| Vai trò (System Role) | Quyền hạn |
|---|---|
| **ADMIN** | Quản lý User (tạo, khóa tài khoản), tạo Project mới, xem toàn bộ dữ liệu hệ thống |
| **USER** | Tài khoản người dùng (bao gồm PM, Developer, Tester). Quyền hạn cụ thể đối với các dự án, sprint, task, worklog được quyết định bởi **Project Role** khi họ được gán vào dự án cụ thể. |

### 2.4 Môi trường vận hành / Công nghệ

| Thành phần | Công nghệ |
|---|---|
| Backend | Spring Boot, REST API, jOOQ |
| Bảo mật | JWT, Spring Security |
| Cơ sở dữ liệu | PostgreSQL |
| Frontend | Angular (CRUD + Dashboard) |
| Khác | Bean Validation, Global Exception Handling, Transaction Management |

### 2.5 Ràng buộc thiết kế
- Toàn bộ API phải theo chuẩn REST, trả response có cấu trúc thống nhất.
- Toàn bộ màn hình danh sách bắt buộc hỗ trợ phân trang (ràng buộc NFR — xem mục 4.1).
- Mật khẩu người dùng phải được mã hoá bằng BCrypt, không lưu plaintext.

### 2.6 Giả định và phụ thuộc
- **(*)** `System Role` (Administrator/Project Manager/Developer, gán ở cấp User) và `Project Role` (PM/DEV/TESTER, gán ở cấp thành viên dự án qua `project_members`) là **hai khái niệm khác nhau**. Tài liệu gốc không nêu rõ mối quan hệ giữa hai loại vai trò này — cần xác nhận: một User có System Role "Developer" có được gán Project Role "PM" trong một dự án cụ thể hay không.
- **(*)** Giả định mỗi Task chỉ thuộc một Project và (tuỳ chọn) một Sprint tại một thời điểm.
- **(*)** Giả định `Task Key` được sinh tự động theo tiền tố của Project (ví dụ `WEB-101`), dựa trên ví dụ minh hoạ ở UC10 của tài liệu gốc.

---

## 3. Yêu cầu chức năng (Functional Requirements)

### 3.1 Bảng tổng hợp

| FR-ID | Tên yêu cầu | UC nguồn | Actor | Ưu tiên |
|---|---|---|---|---|
| FR-AUTH-01 | Đăng nhập hệ thống | UC01 | Tất cả | Cao |
| FR-USER-01 | Tạo User | UC02 | Administrator | Cao |
| FR-USER-02 | Cập nhật User | UC02 | Administrator | Cao |
| FR-USER-03 | Khoá/Mở khoá User | UC02 | Administrator | Cao |
| FR-USER-04 | Tìm kiếm User | UC02 | Administrator | Trung bình |
| FR-USER-05 | Xem danh sách User | UC02 | Administrator | Cao |
| FR-PROJ-01 | Tạo Project | UC03 | Administrator | Cao |
| FR-PROJ-02 (*) | Xem danh sách Project | Suy diễn | Administrator, PM | Cao |
| FR-PROJ-03 (*) | Xem chi tiết Project | Suy diễn | Administrator, PM | Cao |
| FR-PROJ-04 (*) | Cập nhật Project | Suy diễn | Administrator | Cao |
| FR-MEM-01 | Thêm thành viên dự án | UC04 | Administrator, PM | Cao |
| FR-MEM-02 | Xoá thành viên dự án | UC04 | Administrator, PM | Trung bình |
| FR-MEM-03 | Xem danh sách thành viên dự án | UC04 | Administrator, PM | Cao |
| FR-SPR-01 | Tạo Sprint | UC05 | PM | Cao |
| FR-SPR-02 | Chuyển trạng thái Sprint | UC05 | PM | Cao |
| FR-SPR-03 (*) | Xem danh sách Sprint theo Project | Suy diễn | PM, Developer | Cao |
| FR-TASK-01 | Tạo Task | UC06 | PM | Cao |
| FR-TASK-02 | Cập nhật Task | UC07 | Assignee, Reporter, PM | Cao |
| FR-TASK-03 | Gán Task | UC08 | PM | Cao |
| FR-TASK-04 | Tìm kiếm Task đa điều kiện | UC15 | Tất cả | Cao |
| FR-TASK-05 (*) | Xem chi tiết Task | Suy diễn | Tất cả | Cao |
| FR-CMT-01 | Tạo Comment trên Task | UC09 | Tất cả | Trung bình |
| FR-CMT-02 | Xem danh sách Comment | UC09 | Tất cả | Trung bình |
| FR-WLOG-01 | Ghi nhận Worklog | UC10 | Developer | Cao |
| FR-WLOG-02 (*) | Cập nhật Worklog | Suy diễn | Developer | Cao |
| FR-WLOG-03 (*) | Xóa Worklog | Suy diễn | Developer | Cao |
| FR-WLOG-04 | Báo cáo Worklog | UC14 | Tất cả | Trung bình |
| FR-ACT-01 | Tự động ghi nhận lịch sử thay đổi Task | UC11 | Hệ thống | Trung bình |
| FR-ACT-02 (*) | Xem lịch sử Activity của Task | UC11 | Tất cả | Trung bình |
| FR-DASH-01 | Dashboard cá nhân | UC12 | Developer | Trung bình |
| FR-DASH-02 | Dashboard dự án | UC13 | PM | Trung bình |

### 3.2 Chi tiết yêu cầu chức năng

#### FR-AUTH-01 — Đăng nhập hệ thống
- **Actor:** Tất cả người dùng đã có tài khoản
- **Input:** Username, Password
- **Output:** JWT Access Token, User Information
- **Điều kiện tiên quyết:** Username tồn tại, Password chính xác
- **Kết quả:** Người dùng truy cập hệ thống theo đúng quyền System Role của mình

#### FR-USER-01 → FR-USER-05 — Quản lý User
- **Actor:** Administrator
- **Thông tin User quản lý:** Employee ID, Username, Full Name, Email, Role, Status
- **Chức năng:**
  - Tạo User mới
  - Cập nhật thông tin User
  - Khoá/Mở khoá User (đổi Status)
  - Tìm kiếm User
  - Xem danh sách User (có phân trang)

#### FR-PROJ-01 — Tạo Project
- **Actor:** Administrator
- **Thông tin Project:** Project Code, Project Name, Description, Start Date, End Date, Status
- **Enum Status:** `PLANNING`, `ACTIVE`, `ON_HOLD`, `COMPLETED`

#### FR-PROJ-02, FR-PROJ-03 (*) — Xem danh sách / chi tiết Project
- **(*) Suy diễn:** cần thiết để Administrator/PM chọn Project khi thao tác với Sprint, Task, thành viên

#### FR-PROJ-04 (*) — Cập nhật Project
- **(*) Suy diễn:** Từ quyền "Quản lý Project" của Administrator.
- **Actor:** Administrator
- **Thông tin cho phép cập nhật:** Project Name, Description, Start Date, End Date, Status (Project Code không được thay đổi).

#### FR-MEM-01 → FR-MEM-03 — Quản lý thành viên dự án
- **Actor:** Administrator hoặc Project Manager
- **Thông tin:** Project, User, Project Role, Status
- **Enum Project Role:** `PM`, `DEV`, `TESTER`
- **Enum Project Member Status:** `ACTIVE`, `INACTIVE`
- **Chức năng:**
  - Thêm thành viên mới (mặc định status = `ACTIVE`)
  - Xoá thành viên khỏi dự án (thay đổi status thành `INACTIVE` để bảo toàn lịch sử công việc và Worklog)
  - Xem danh sách thành viên dự án

#### FR-SPR-01, FR-SPR-02 — Quản lý Sprint
- **Actor:** Project Manager
- **Thông tin tạo Sprint:** Sprint Name, Goal, Start Date, End Date
- **Enum Trạng thái Sprint:** `PLANNED`, `ACTIVE`, `CLOSED`
- **Ràng buộc nghiệp vụ (bắt buộc):** Tại một thời điểm, một Project chỉ được có tối đa **một** Sprint ở trạng thái `ACTIVE`

#### FR-SPR-03 (*) — Xem danh sách Sprint theo Project
- **(*) Suy diễn:** cần để chọn Sprint khi tạo/gán Task, và hiển thị Sprint Progress ở Dashboard dự án

#### FR-TASK-01 — Tạo Task
- **Actor:** Project Manager
- **Thông tin Task:** Task Key, Summary, Description, Task Type, Priority, Assignee, Reporter, Story Point, Estimate Hour, Due Date
- **Enum Task Type:** `STORY`, `TASK`, `BUG`
- **Enum Priority:** `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- **Trạng thái mặc định khi tạo:** `TODO`

#### FR-TASK-02 — Cập nhật Task
- **Actor:** Assignee (người được gán), Reporter (người tạo task), hoặc PM của dự án.
- **Quy tắc phân quyền và trường được phép sửa:**
  - **Với PM của dự án:** Được phép chỉnh sửa toàn bộ các trường của Task và tự do chuyển đổi giữa bất kỳ trạng thái nào (vượt qua/bypass workflow).
  - **Với Developer (là Assignee hoặc Reporter của Task):**
    - Chỉ được phép chỉnh sửa 4 trường: `Status`, `Description`, `Estimate Hour`, `Due Date`.
    - Phải tuân thủ nghiêm ngặt luồng chuyển trạng thái (workflow), không được phép nhảy bậc.
- **Luồng trạng thái hợp lệ (cho Developer):**
  - `TODO → IN_PROGRESS → TESTING → DONE`
  - `IN_PROGRESS → TODO` (cho phép revert)
  - Mọi bước chuyển trạng thái khác đối với Developer đều bị coi là không hợp lệ và hệ thống sẽ từ chối.

#### FR-TASK-03 — Gán Task
- **Actor:** Project Manager
- **Điều kiện:** User được gán phải thuộc cùng Project với Task và có trạng thái thành viên `ACTIVE`.
- **Kết quả:** Task được cập nhật Assignee mới

#### FR-TASK-04 — Tìm kiếm Task đa điều kiện
- **Actor:** Tất cả người dùng
- **Điều kiện tìm kiếm:** Project, Sprint, Status, Priority, Assignee, Keyword
- **Output:** Danh sách Task phù hợp (có phân trang)

#### FR-TASK-05 (*) — Xem chi tiết Task
- **(*) Suy diễn:** cần để hiển thị đầy đủ thông tin Task cùng Comment, Activity History, Worklog liên quan

#### FR-CMT-01, FR-CMT-02 — Comment Task
- **Actor:** Tất cả người dùng có quyền truy cập Task
- **Thông tin Comment:** Task, Comment Content, Created By, Created Time
- **Chức năng:** Tạo Comment, Xem danh sách Comment

#### FR-WLOG-01 — Ghi nhận Worklog
- **Actor:** Developer (thành viên `ACTIVE` của Project)
- **Phương thức:** Nhập thủ công (manual), cho phép log bù (ghi nhận cho các ngày trong quá khứ).
- **Thời hạn:** Cho phép cập nhật bất kỳ lúc nào, không giới hạn khoảng thời gian ghi bù.
- **Thông tin:** Task, Work Date, Hour, Description
- **Ràng buộc:** `Hour > 0` và `Hour <= 24`

#### FR-WLOG-02 (*) — Cập nhật Worklog
- **Actor:** Người tạo Worklog (Developer)
- **Mô tả:** Cho phép chỉnh sửa các trường thông tin của bản ghi Worklog do mình tạo ra (Work Date, Hour, Description). Ràng buộc `0 < Hour <= 24` vẫn áp dụng.

#### FR-WLOG-03 (*) — Xóa Worklog
- **Actor:** Người tạo Worklog (Developer)
- **Mô tả:** Cho phép xóa bản ghi Worklog đã tạo để điều chỉnh lại dữ liệu chính xác.

#### FR-ACT-01 — Tự động ghi nhận lịch sử thay đổi Task
- **Actor:** Hệ thống (tự động, không do người dùng chủ động kích hoạt)
- **Các sự kiện phải được ghi nhận:** Task Created, Status Changed, Priority Changed, Assignee Changed, Comment Added
- **Thông tin lưu mỗi sự kiện:** User, Action, Old Value, New Value, Created Time

#### FR-ACT-02 (*) — Xem lịch sử Activity của Task
- **(*) Suy diễn** từ tên UC11 "Theo dõi Activity History" — cần màn hình hiển thị lịch sử đã ghi nhận

#### FR-DASH-01 — Dashboard cá nhân
- **Actor:** Developer
- **Hiển thị:** My Open Tasks, My Completed Tasks, My Overdue Tasks, Total Logged Hours

#### FR-DASH-02 — Dashboard dự án
- **Actor:** Project Manager
- **Hiển thị:** Total Tasks, Task By Status, Task By Priority, Total Logged Hours, Sprint Progress

#### FR-WLOG-04 — Báo cáo Worklog
- **Actor:** Tất cả người dùng (phạm vi dữ liệu theo quyền)
- **Điều kiện tìm kiếm:** Project, User, From Date, To Date
- **Output:** User, Total Hours, Number Of Tasks

---

## 4. Yêu cầu phi chức năng (Non-Functional Requirements)

| NFR-ID | Nhóm | Mô tả |
|---|---|---|
| NFR-01 | Bảo mật | Xác thực bằng JWT |
| NFR-02 | Bảo mật | Mật khẩu mã hoá bằng BCrypt |
| NFR-03 | Bảo mật | Phân quyền theo Role (Role-based Authorization) |
| NFR-04 | Hiệu năng | Toàn bộ màn hình danh sách phải hỗ trợ phân trang |
| NFR-05 | Hiệu năng | Thời gian phản hồi dưới 3 giây với dữ liệu khoảng 10.000 task |
| NFR-06 | Audit | Lưu lại lịch sử thay đổi của Task (liên kết trực tiếp với FR-ACT-01) |
| NFR-07 | Logging | Ghi log toàn bộ request và exception phát sinh |

---

## 5. Yêu cầu giao diện ngoài (External Interface Requirements)

### 5.1 Giao diện người dùng
Ứng dụng web (Angular SPA), gồm các màn hình CRUD cho từng module và 2 màn hình Dashboard (cá nhân, dự án).

### 5.2 Giao diện phần mềm
REST API (Spring Boot) giao tiếp qua HTTP/JSON, xác thực bằng JWT Bearer Token gửi kèm header `Authorization`.

### 5.3 Giao diện dữ liệu
Kết nối cơ sở dữ liệu quan hệ PostgreSQL qua jOOQ.

---

## 6. Mô hình dữ liệu (tổng quan)

| Bảng | Mô tả | Quan hệ chính |
|---|---|---|
| `users` | Tài khoản người dùng | 1—N với `project_members`, `tasks` (assignee/reporter), `worklogs`, `task_comments`, `task_activities` |
| ~~`roles`~~ | *(Không tạo bảng riêng — đã quyết định dùng cột `role VARCHAR` với CHECK constraint trực tiếp trên `users`; xem Database Design mục 2)* | — |
| `projects` | Dự án | 1—N với `project_members`, `sprints`, `tasks` |
| `project_members` | Thành viên trong dự án + Project Role + Status | N—1 với `projects`, N—1 với `users` |
| `sprints` | Sprint theo dự án | N—1 với `projects`; 1—N với `tasks` |
| `tasks` | Công việc | N—1 với `projects`, `sprints`; N—1 với `users` (assignee, reporter) |
| `task_comments` | Bình luận trên Task | N—1 với `tasks`, `users` |
| `worklogs` | Thời gian làm việc ghi nhận trên Task | N—1 với `tasks`, `users` |
| `task_activities` | Lịch sử thay đổi Task | N—1 với `tasks`, `users` |

---

## 7. Ma trận truy vết yêu cầu (Requirements Traceability Matrix)

| UC nguồn | Tên Use Case gốc | FR-ID tương ứng |
|---|---|---|
| UC01 | Đăng nhập | FR-AUTH-01 |
| UC02 | Quản lý User | FR-USER-01 → FR-USER-05 |
| UC03 | Tạo Project | FR-PROJ-01 (+ FR-PROJ-02, FR-PROJ-03, FR-PROJ-04 suy diễn) |
| UC04 | Quản lý thành viên dự án | FR-MEM-01 → FR-MEM-03 |
| UC05 | Quản lý Sprint | FR-SPR-01, FR-SPR-02 (+ FR-SPR-03 suy diễn) |
| UC06 | Tạo Task | FR-TASK-01 |
| UC07 | Cập nhật Task | FR-TASK-02 |
| UC08 | Gán Task | FR-TASK-03 |
| UC09 | Comment Task | FR-CMT-01, FR-CMT-02 |
| UC10 | Worklog | FR-WLOG-01, FR-WLOG-02 (*), FR-WLOG-03 (*) |
| UC11 | Theo dõi Activity History | FR-ACT-01 (+ FR-ACT-02 suy diễn) |
| UC12 | Dashboard cá nhân | FR-DASH-01 |
| UC13 | Dashboard dự án | FR-DASH-02 |
| UC14 | Báo cáo Worklog | FR-WLOG-04 |
| UC15 | Tìm kiếm Task | FR-TASK-04 |

---

## 8. Phụ lục

### 8.1 Danh sách Enum sử dụng trong hệ thống

| Enum | Giá trị |
|---|---|
| System Role | `ADMIN`, `USER` |
| User Status | `ACTIVE`, `LOCKED` |
| Project Status | `PLANNING`, `ACTIVE`, `ON_HOLD`, `COMPLETED` |
| Project Role | `PM`, `DEV`, `TESTER` |
| Project Member Status | `ACTIVE`, `INACTIVE` |
| Sprint Status | `PLANNED`, `ACTIVE`, `CLOSED` |
| Task Type | `STORY`, `TASK`, `BUG` |
| Task Priority | `LOW`, `MEDIUM`, `HIGH`, `CRITICAL` |
| Task Status | `TODO`, `IN_PROGRESS`, `TESTING`, `DONE` |
| Activity Action | `TASK_CREATED`, `STATUS_CHANGED`, `PRIORITY_CHANGED`, `ASSIGNEE_CHANGED`, `COMMENT_ADDED` |

1. Quy tắc sinh `Task Key` tự động (tiền tố theo Project Code?).
2. Ai có quyền xoá Task/Sprint/Project (tài liệu gốc chỉ mô tả Tạo/Cập nhật, chưa đề cập Xoá) — có cần bổ sung chức năng xoá (hoặc archive) không?
3. Phạm vi dữ liệu trong Báo cáo Worklog (FR-WLOG-04) đối với Developer — chỉ xem được worklog của bản thân hay của cả team trong Project mình tham gia?
