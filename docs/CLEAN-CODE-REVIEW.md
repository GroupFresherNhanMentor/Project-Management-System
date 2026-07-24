# Báo Cáo Đánh Giá Clean Code (Clean Code Review)

> **Dự án**: Mini Project Management System  
> **Thời gian đánh giá**: 24/07/2026  
> **Phạm vi kiểm tra**: Backend (`BE` - Java 25 / Spring Boot 4.1 / jOOQ) & Frontend (`FE` - Angular 19 / TypeScript / Tailwind CSS)  
> **Vị trí tài liệu**: `docs/CLEAN-CODE-REVIEW.md`

---

## 📋 Mục lục
1. [Tổng quan dự án & Điểm số Clean Code](#1-tổng-quan-dự-án--điểm-số-clean-code)
2. [Đánh giá Chi tiết Backend (Java / Spring Boot / jOOQ)](#2-đánh-giá-chi-tiết-backend-java--spring-boot--jooq)
   - [2.1 Kiến trúc & Cấu trúc Mô-đun](#21-kiến-trúc--cấu-trúc-mô-đun)
   - [2.2 Nguyên tắc SOLID](#22-nguyên-tắc-solid)
   - [2.3 Code Smells & Điểm cần cải thiện](#23-code-smells--điểm-cần-cải-thiện)
   - [2.4 Xử lý Lỗi (Error Handling) & Logging](#24-xử-lý-lỗi-error-handling--logging)
   - [2.5 Truy vấn Dữ liệu, Transaction & Bảo mật](#25-truy-vấn-dữ-liệu-transaction--bảo-mật)
   - [2.6 Unit Test & Integration Test](#26-unit-test--integration-test)
3. [Đánh giá Chi tiết Frontend (Angular / TypeScript)](#3-đánh-giá-chi-tiết-frontend-angular--typescript)
   - [2.1 Kiến trúc Component (Smart vs Dumb)](#31-kiến-trúc-component-smart-vs-dumb)
   - [3.2 Chất lượng TypeScript & Code Smells](#32-chất-lượng-typescript--code-smells)
   - [3.3 Quản lý Trạng thái (State) & Bất đồng bộ (Async/RxJS)](#33-quản-lý-trạng-thái-state--bất-đồng-bộ-asyncrxjs)
   - [3.4 Template HTML, Performance & Styling](#34-template-html-performance--styling)
   - [3.5 Unit Test Frontend](#35-unit-test-frontend)
4. [Ví dụ Refactoring Cụ thể (Before / After)](#4-ví-dụ-refactoring-cụ-thể-before--after)
5. [Lộ trình Cải thiện Khuyến nghị (Actionable Roadmap)](#5-lộ-trình-cải-thiện-khuyến-nghị-actionable-roadmap)

---

## 1. Tổng quan dự án & Điểm số Clean Code

Dự án **Mini Project Management System** được thiết kế hiện đại, áp dụng công nghệ tiên tiến (Spring Boot 4.1, Java 25, jOOQ, Flyway, Angular 19 Standalone Components, Tailwind CSS). Codebase nhìn chung đạt mức độ hoàn thiện cao, tuân thủ nguyên tắc Clean Code ở mức khá - giỏi.

### 🌟 Bảng điểm đánh giá tổng quan (Scale 1 - 10)

| Tiêu chí đánh giá | Backend (BE) | Frontend (FE) | Ghi chú tổng quan |
| :--- | :---: | :---: | :--- |
| **Kiến trúc & Cấu trúc (Architecture)** | **9.0 / 10** | **8.5 / 10** | Cấu trúc phân tầng rõ ràng, áp dụng Standalone Components & jOOQ tốt. |
| **Tuân thủ SOLID** | **8.5 / 10** | **8.0 / 10** | DI tốt, SRP ổn định; FE cần tách biệt Smart / Dumb components hơn. |
| **Chất lượng Mã nguồn (Code Quality)** | **8.5 / 10** | **7.5 / 10** | Đặt tên rõ ràng, tuy nhiên FE còn dính Magic Strings & Hardcoded styles. |
| **Xử lý Lỗi & Logging** | **8.5 / 10** | **8.0 / 10** | Backend dùng `GlobalExceptionHandler` thống nhất; FE interceptor cần tối ưu. |
| **Hiệu năng & Bất đồng bộ** | **9.0 / 10** | **7.0 / 10** | Backend dùng jOOQ chống N+1; FE dính hàm gọi trong template gây re-render. |
| **Kiểm thử (Testability & Coverage)** | **8.5 / 10** | **5.0 / 10** | Backend test integration với DB thực rất tốt; Frontend thiếu unit test component. |

---

## 2. Đánh giá Chi tiết Backend (Java / Spring Boot / jOOQ)

### 2.1 Kiến trúc & Cấu trúc Mô-đun
- **Điểm mạnh**:
  - Tuân thủ chặt chẽ mô hình **Controller - Service - Repository**.
  - Tách biệt hoàn toàn giữa **Persistence Objects (jOOQ Records)** và **DTO (Data Transfer Objects)**.
  - Sử dụng **MapStruct** (`UserMapper.java`, `ProjectMapper.java`) giúp tự động hoá việc mapping mà không bị lọt thông tin persistence ra tầng Controller.
  - Quản lý database schema bằng **Flyway** rất chuẩn chỉnh.

### 2.2 Nguyên tắc SOLID
- **Single Responsibility Principle (SRP)**:
  - Tốt. `GlobalExceptionHandler` tập trung xử lý lỗi web, `Mappers` chuyên trách biến đổi dữ liệu, `Repository` chuyên cho jOOQ DSLContext.
- **Dependency Inversion Principle (DIP)**:
  - Tốt. Tất cả các Service đều định nghĩa qua Interface (ví dụ: `UserService`, `ProjectService`) và triển khai ở class `Impl`.
  - Inject dependency sạch sẽ qua Lombok `@RequiredArgsConstructor` với `@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)`.

### 2.3 Code Smells & Điểm cần cải thiện
1. **Bắt ngoại lệ bằng String Matching (Brittle Exception Handling)**:
   - Trong `UserServiceImpl.java` (dòng 81-101), việc phát hiện vi phạm unique constraint được thực hiện bằng cách duyệt `getCause()` và kiểm tra chuỗi `.contains("users_username_key")`.
   - *Rủi ro*: Khi đổi tên index/constraint trong DB, logic code Java sẽ bị vỡ silently.
2. **Thiếu annotation nhất quán ở Controller**:
   - `ProjectController` có dùng `@Validated` ở level class để validate path variable / query param, nhưng `UserController` lại chưa có.

### 2.4 Xử lý Lỗi (Error Handling) & Logging
- **Điểm mạnh**: `GlobalExceptionHandler` chuẩn hóa mọi response về dạng `ApiResponse<T>`. Log dùng `@Slf4j` thay vì `System.out.println`.
- **Cần cải thiện (Hierarchy chưa đồng nhất)**:
  - Hầu hết các Exception tùy chỉnh đều kế thừa từ `AppException` (chứa HTTP Status). Tuy nhiên `InvalidOldPasswordException.java` lại kế thừa trực tiếp từ `RuntimeException`.
  - Điều này dẫn tới việc phải viết riêng `@ExceptionHandler(InvalidOldPasswordException.class)` trong `GlobalExceptionHandler`.

### 2.5 Truy vấn Dữ liệu, Transaction & Bảo mật
- **Data Access**: Dự án sử dụng **jOOQ** thay vì JPA/Hibernate ORM. Điều này loại bỏ triệt để vấn đề **N+1 Query** ngầm định của JPA, bắt buộc truy vấn explicit join và fetch.
- **Transaction**: Áp dụng chuẩn xác `@Transactional` và `@Transactional(readOnly = true)` tại các method của Service.
- **Security**: Phân quyền sạch sẽ bằng `@PreAuthorize("hasAuthority('ADMIN')")` hoặc `@PreAuthorize("isAuthenticated()")`. Sử dụng JWT Token chuẩn với Spring Security OAuth2 Resource Server.

### 2.6 Unit Test & Integration Test
- **Điểm mạnh vượt trội**: Dự án dựng `BaseIntegrationTest` cung cấp real database context qua jOOQ `DSLContext` cho việc test integration (như `ProjectServiceTest.java`). Đã phủ các case trùng lặp mã, phân quyền và xác thực context.

---

## 3. Đánh giá Chi tiết Frontend (Angular / TypeScript)

### 3.1 Kiến trúc Component (Smart vs Dumb)
- **Điểm mạnh**:
  - Đã cập nhật lên Angular 19 với **Standalone Components** (`imports: [RouterOutlet]...`).
  - Cấu trúc thư mục theo Feature (`features/board`, `features/projects`) rõ ràng.
  - Endpoints được tập trung tại `configs/api-endpoints.ts`.
- **Cần cải thiện**:
  - Component `app-board` đang rơi vào dạng **God Component / Smart Component quá tải**: Vừa xử lý route param, gọi 2 services khác nhau (`TaskService`, `ProjectService`), vừa tự quản lý state signals, vừa render toàn bộ giao diện Kanban.

### 3.2 Chất lượng TypeScript & Code Smells
- **Điểm mạnh**: Sử dụng Type / Interface nghiêm túc (`ProjectDto`, `TaskStatusDto`), không dùng `any` bừa bãi.
- **Cần cải thiện**:
  - **Magic Strings**: Trong `board.ts`, các chuỗi như `'BUG'`, `'STORY'`, ưu tiên `'LOW'`, `'HIGH'` bị hardcode dưới dạng string literal. Trong `project-list.ts`, status `'PLANNING'`, `'ACTIVE'` cũng bị rải rác.
  - **Khởi tạo Object trùng lặp (Performance)**: Trong `project-list.ts`, hàm `statusBadge()` và `statusLabel()` tạo mới 1 Object Dictionary trên mỗi lần hàm được gọi.

### 3.3 Quản lý Trạng thái (State) & Bất đồng bộ (Async/RxJS)
- **Điểm mạnh**: Áp dụng tốt Angular Signals (`signal`, `toSignal`).
- **Cần cải thiện**:
  - **Memory Leak**: Ở `project-list.ts` và `board.ts`, các thao tác `.subscribe()` chưa có cơ chế hủy đăng ký (như `takeUntilDestroyed()`).
  - **Race Condition (Bất đồng bộ phụ thuộc)**: Trong `board.ts` (`ngOnInit`), hai API `getTaskStatuses` và `getSprints` được gọi song song rời rạc. API sau tự động gọi `loadTasks()` dựa trên giả định API trước đã hoàn thành.
  - **Interceptor Anti-pattern**: Trong `auth.interceptor.ts`, hàm `authService.refresh()` lại dùng `.subscribe()` thủ công bên trong stream Interceptor thay vì nối chain Observable (`switchMap`).

### 3.4 Template HTML, Performance & Styling
- **Điểm mạnh**: Dùng cú pháp Angular 17+ control flow mới (`@for`, `@if`).
- **Cần cải thiện**:
  - **Hardcoded Inline Styles**: Các template sử dụng quá nhiều `style="border-color:#E1E4E9;color:#4B5160"` kết hợp với Tailwind CSS. Điều này làm vỡ nguyên tắc utility class của Tailwind và ngăn cản tính năng Dark Mode.
  - **Gọi hàm trong Template (Performance Killer)**: Trong `board.html`: `@for (task of tasksFor(col.id); ...)` khiến hàm `tasksFor()` bị thực thi lại trong **mọi chu kỳ Change Detection** của Angular.

### 3.5 Unit Test Frontend
- **Cần cải thiện**: Thiếu hụt unit test ở tầng Component. Các component phức tạp như `ProjectList` hay `Board` hoàn toàn chưa có file `.spec.ts`.

---

## 4. Ví dụ Refactoring Cụ thể (Before / After)

### 4.1 Backend: Chuẩn hóa Hierarchy Exception

❌ **Before (`InvalidOldPasswordException.java`)**:
```java
public class InvalidOldPasswordException extends RuntimeException {
    public InvalidOldPasswordException(String message) {
        super(message);
    }
}
```

✅ **After (Sử dụng `AppException`)**:
```java
public class InvalidOldPasswordException extends AppException {
    public InvalidOldPasswordException() {
        super(ErrorCode.INVALID_OLD_PASSWORD, HttpStatus.BAD_REQUEST);
    }
}
```
*Lợi ích*: Giảm thiểu code thừa trong `GlobalExceptionHandler`, tất cả lỗi hệ thống tự động qua 1 handler chung duy nhất.

---

### 4.2 Frontend: Loại bỏ Gọi hàm trong Template bằng `computed()` Signal

❌ **Before (`board.ts` & `board.html`)**:
```typescript
// Component: Lọc task mỗi lần gọi
tasksFor(statusId: string) {
  return this.tasks().filter(t => t.statusId === statusId);
}
```
```html
<!-- Template: Chạy lại filter liên tục khi hover/click -->
@for (task of tasksFor(col.id); track task.id) {
  <app-task-card [task]="task" />
}
```

✅ **After (`board.ts` & `board.html`)**:
```typescript
// Component: Tính toán sẵn Map theo statusId bằng computed Signal
readonly tasksByStatus = computed(() => {
  const map = new Map<string, TaskDto[]>();
  for (const task of this.tasks()) {
    const list = map.get(task.statusId) ?? [];
    list.push(task);
    map.set(task.statusId, list);
  }
  return map;
});
```
```html
<!-- Template: Chỉ re-render khi signal tasks thay đổi -->
@for (task of tasksByStatus().get(col.id) ?? []; track task.id) {
  <app-task-card [task]="task" />
}
```
*Lợi ích*: Tăng tốc độ render UI lên gấp nhiều lần, loại bỏ Change Detection nghẽn bottle-neck.

---

### 4.3 Frontend: Khắc phục Memory Leak & Race Condition với RxJS

❌ **Before (`board.ts`)**:
```typescript
ngOnInit() {
  this.taskService.getTaskStatuses(this.projectId).subscribe(res => {
    this.statuses.set(res.data);
  });
  this.sprintService.getSprints(this.projectId).subscribe(res => {
    this.sprints.set(res.data);
    this.loadTasks(); // Phụ thuộc ngầm định!
  });
}
```

✅ **After (`board.ts`)**:
```typescript
private readonly destroyRef = inject(DestroyRef);

ngOnInit() {
  forkJoin({
    statuses: this.taskService.getTaskStatuses(this.projectId),
    sprints: this.sprintService.getSprints(this.projectId)
  }).pipe(
    takeUntilDestroyed(this.destroyRef)
  ).subscribe(({ statuses, sprints }) => {
    this.statuses.set(statuses.data);
    this.sprints.set(sprints.data);
    this.loadTasks();
  });
}
```
*Lợi ích*: Đảm bảo dữ liệu tải đồng bộ, tránh lỗi màn hình trắng do Race Condition, tự động unsubcribe chống tràn bộ nhớ.

---

## 5. Lộ trình Cải thiện Khuyến nghị (Actionable Roadmap)

### 🔴 Ưu tiên Cao (High Priority - Sprint tới)
1. **Frontend**: Thay thế tất cả `tasksFor(col.id)` và các method call trong HTML template bằng `computed()` Signals.
2. **Frontend**: Sửa `auth.interceptor.ts` để loại bỏ `.subscribe()` lồng trong stream Interceptor.
3. **Backend**: Chuyển `InvalidOldPasswordException` kế thừa `AppException` để làm sạch `GlobalExceptionHandler`.

### 🟡 Ưu tiên Trung bình (Medium Priority)
1. **Frontend**: Chuyển các magic string (`BUG`, `STORY`, `LOW`, `HIGH`, `ACTIVE`) thành Enums trong `core/models`.
2. **Frontend**: Loại bỏ triệt để inline hex styles (`style="color:#..."`) chuyển sang Tailwind config palette.
3. **Backend**: Thay thế logic string matching `users_username_key` bằng SQLState code hoặc Spring `DataIntegrityViolationException`.

### 🟢 Ưu tiên Dài hạn (Low / Maintenance)
1. **Frontend**: Tách `app-board` thành Smart Component (Container) và Dumb Components (`KanbanColumn`, `TaskCard`).
2. **Frontend**: Bổ sung Unit test (`.spec.ts`) cho các Component chính (`ProjectList`, `Board`).
