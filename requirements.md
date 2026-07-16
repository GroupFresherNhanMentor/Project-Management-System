# User Requirement
Mini Project Management System (Jira-like)

## 1. Tổng quan

### 1.1 Mục đích

Xây dựng hệ thống quản lý công việc nội bộ cho phép quản lý dự án, sprint, task, worklog và tiến độ thực hiện của thành viên trong nhóm.

Hệ thống nhằm hỗ trợ:
- Quản lý dự án
- Quản lý thành viên dự án
- Theo dõi trạng thái công việc
- Ghi nhận thời gian làm việc
- Theo dõi tiến độ sprint
- Thống kê và báo cáo

### 1.2 Phạm vi

Hệ thống bao gồm:
- Quản lý người dùng
- Quản lý dự án
- Quản lý thành viên dự án
- Quản lý Sprint
- Quản lý Task
- Comment Task
- Worklog
- Dashboard & Report

Không bao gồm:
- Email Notification
- Realtime Update
- Chat
- File Upload
- Drag & Drop Kanban

## 2. Danh sách vai trò

### 2.1 Administrator

Administrator có quyền:
- Quản lý User
- Quản lý Project
- Xem toàn bộ dữ liệu

### 2.2 Project Manager

Project Manager có quyền:
- Quản lý Project được phân công
- Quản lý Sprint
- Quản lý Task
- Phân công Task
- Theo dõi tiến độ

### 2.3 Developer

Developer có quyền:
- Xem Task được giao
- Cập nhật trạng thái Task
- Tạo Comment
- Log Work

## 3. Chức năng chi tiết

### UC01 - Đăng nhập

**Mô tả**

Người dùng đăng nhập hệ thống bằng tài khoản đã được cấp.

**Input**

```
Plain Text

Username
Password
```

**Output**

```
Plain Text

JWT Access Token
User Information
```

**Điều kiện**

- Username tồn tại
- Password chính xác

**Kết quả**

Người dùng truy cập được hệ thống theo quyền của mình.

### UC02 - Quản lý User

**Mô tả**

Administrator quản lý tài khoản người dùng.

**Chức năng**

- Tạo User
- Cập nhật User
- Khóa User
- Tìm kiếm User
- Xem danh sách User

**Thông tin User**

```
Plain Text

Employee ID
Username
Full Name
Email
Role
Status
```
### UC03 - Tạo Project

**Mô tả**

Administrator tạo dự án mới.

**Thông tin Project**

```
Plain Text

Project Code
Project Name
Description
Start Date
End Date
Status
```

**Status**

```
Plain Text

PLANNING
ACTIVE
ON_HOLD
COMPLETED
```
### UC04 - Quản lý thành viên dự án

**Mô tả**

Administrator hoặc Project Manager thêm thành viên vào dự án.

**Chức năng**

- Thêm thành viên
- Xóa thành viên
- Xem danh sách thành viên

**Thông tin**

```
Plain Text

Project
User
Project Role
```

**Project Role**

```
Plain Text

PM
DEV
TESTER
```
### UC05 - Quản lý Sprint

**Mô tả**

Project Manager quản lý Sprint của dự án.

**Tạo Sprint**

Thông tin:

```
Plain Text

Sprint Name
Goal
Start Date
End Date
```

**Trạng thái Sprint**

```
Plain Text

PLANNED
ACTIVE
CLOSED
```

**Ràng buộc**

Một thời điểm chỉ có tối đa một Sprint ACTIVE.

### UC06 - Tạo Task

**Mô tả**

Project Manager tạo Task cho dự án.

**Thông tin Task**

```
Plain Text

Task Key
Summary
Description
Task Type
Priority
Assignee
Reporter
Story Point
Estimate Hour
Due Date
```

**Task Type**

```
Plain Text

STORY
TASK
BUG
```

**Priority**

```
Plain Text

LOW
MEDIUM
HIGH
CRITICAL
```

**Status mặc định**

```
Plain Text

TODO
```
### UC07 - Cập nhật Task

**Mô tả**

Người dùng được phân công có thể cập nhật Task.

**Trường được phép chỉnh sửa**

```
Plain Text

Status
Description
Estimate Hour
Due Date
```

**Luồng trạng thái**

```
Plain Text

TODO
↓
IN_PROGRESS
↓
TESTING
↓
DONE
```

Hoặc:

```
Plain Text

IN_PROGRESS
↓
TODO
```
### UC08 - Gán Task

**Mô tả**

Project Manager gán Task cho thành viên dự án.

**Điều kiện**

User phải thuộc cùng Project.

**Kết quả**

Task được cập nhật Assignee mới.

### UC09 - Comment Task

**Mô tả**

Người dùng có thể trao đổi trên Task.

**Thông tin**

```
Plain Text

Task
Comment Content
Created By
Created Time
```

**Chức năng**

- Tạo Comment
- Xem danh sách Comment

### UC10 - Worklog

**Mô tả**

Developer ghi nhận thời gian làm việc trên từng Task.

**Thông tin**

```
Plain Text

Task
Work Date
Hour
Description
```

Ví dụ:

```
Plain Text

Task: WEB-101
Ngày:
15/07/2026
Số giờ:
4
Mô tả:
Implement Login API
```

**Ràng buộc**

```
Plain Text

Hour > 0
Hour <= 24
```
### UC11 - Theo dõi Activity History

**Mô tả**

Hệ thống ghi nhận các thay đổi quan trọng của Task.

**Các sự kiện cần lưu**

```
Plain Text

Task Created
Status Changed
Priority Changed
Assignee Changed
Comment Added
```

**Thông tin lưu**

```
Plain Text

User
Action
Old Value
New Value
Created Time
```
### UC12 - Dashboard cá nhân

**Mô tả**

Developer xem tình hình công việc của mình.

**Hiển thị**

```
Plain Text

My Open Tasks
My Completed Tasks
My Overdue Tasks
Total Logged Hours
```
### UC13 - Dashboard dự án

**Mô tả**

Project Manager xem tình hình dự án.

**Hiển thị**

```
Plain Text

Total Tasks
Task By Status
Task By Priority
Total Logged Hours
Sprint Progress
```
### UC14 - Báo cáo Worklog

**Mô tả**

Người dùng xem báo cáo số giờ làm việc.

**Điều kiện tìm kiếm**

```
Plain Text

Project
User
From Date
To Date
```

**Kết quả**

```
Plain Text

User
Total Hours
Number Of Tasks
```
### UC15 - Tìm kiếm Task

**Mô tả**

Người dùng tìm kiếm Task theo nhiều điều kiện.

**Điều kiện**

```
Plain Text

Project
Sprint
Status
Priority
Assignee
Keyword
```

**Kết quả**

Danh sách Task phù hợp.

## 4. Yêu cầu phi chức năng

**Bảo mật**
- JWT Authentication
- Password mã hóa BCrypt
- Role-based Authorization

**Hiệu năng**
- Hỗ trợ phân trang cho tất cả màn hình danh sách
- Thời gian phản hồi dưới 3 giây với dữ liệu khoảng 10.000 task

**Audit**
- Lưu lịch sử thay đổi Task

**Logging**
- Log request và exception

## 5. Database dự kiến

```
Plain Text

users
roles
projects
project_members
sprints
tasks
task_comments
worklogs
task_activities
refresh_tokens
```

**Technicals:**

- Spring Boot
- REST API
- JWT
- JPA/Hibernate
- PostgreSQL
- Validation
- Exception Handling
- Transaction
- Reporting Query
- Angular CRUD + Dashboard đơn giản

Độ khó khoảng 7/10
