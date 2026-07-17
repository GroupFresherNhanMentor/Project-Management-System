
-- Hệ thống phân quyền hệ thống (UC01, UC02)
CREATE TYPE sys_role AS ENUM ('ADMINISTRATOR', 'USER');
CREATE TYPE user_status AS ENUM ('ACTIVE', 'LOCKED');

-- Trạng thái dự án và vai trò trong dự án (UC03, UC04)
CREATE TYPE project_status AS ENUM ('PLANNING', 'ACTIVE', 'ON_HOLD', 'COMPLETED');
CREATE TYPE project_role AS ENUM ('PM', 'DEV', 'TESTER');

-- Trạng thái Sprint (UC05)
CREATE TYPE sprint_status AS ENUM ('PLANNED', 'ACTIVE', 'CLOSED');

-- Thuộc tính của Task (UC06, UC07)
CREATE TYPE task_type AS ENUM ('STORY', 'TASK', 'BUG');
CREATE TYPE task_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');
CREATE TYPE task_status AS ENUM ('TODO', 'IN_PROGRESS', 'TESTING', 'DONE');

-- Trạng thái của Refresh Token Family
CREATE TYPE token_status AS ENUM ('ACTIVE', 'USED', 'REVOKED');

-- 1. Bảng Người dùng (Hỗ trợ UC01, UC02)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id VARCHAR(50) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL, -- Lưu hash BCrypt
    role sys_role NOT NULL DEFAULT 'USER',
    status user_status NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Bảng Dự án (UC03)
CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_code VARCHAR(10) NOT NULL UNIQUE, -- Ví dụ: 'WEB', 'API'
    project_name VARCHAR(100) NOT NULL,
    description TEXT,
    start_date DATE,
    end_date DATE,
    status project_status NOT NULL DEFAULT 'PLANNING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Bảng Thành viên Dự án (UC04 - Quan hệ N-N giữa Users và Projects)
CREATE TABLE project_members (
    project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    role project_role NOT NULL DEFAULT 'DEV',
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (project_id, user_id)
);

-- 4. Bảng Sprint (UC05)
CREATE TABLE sprints (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    sprint_name VARCHAR(100) NOT NULL,
    goal TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status sprint_status NOT NULL DEFAULT 'PLANNED',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- RÀNG BUỘC: Một dự án tại một thời điểm chỉ có tối đa 1 Sprint ở trạng thái ACTIVE
CREATE UNIQUE INDEX idx_unique_active_sprint_per_project 
ON sprints (project_id) 
WHERE (status = 'ACTIVE');


-- 5. Bảng Task / Công việc (UC06, UC07, UC08)
CREATE TABLE tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_key VARCHAR(20) NOT NULL UNIQUE, -- Sinh tự động dạng 'WEB-101' từ Backend
    project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    sprint_id UUID REFERENCES sprints(id) ON DELETE SET NULL,
    summary VARCHAR(255) NOT NULL,
    description TEXT,
    type task_type NOT NULL DEFAULT 'TASK',
    priority task_priority NOT NULL DEFAULT 'MEDIUM',
    status task_status NOT NULL DEFAULT 'TODO',
    
    assignee_id UUID REFERENCES users(id) ON DELETE SET NULL,
    reporter_id UUID NOT NULL REFERENCES users(id),
    
    story_point INT DEFAULT 0,
    estimate_hour INT DEFAULT 0,
    due_date DATE,
    
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Index phục vụ tìm kiếm nhanh (UC15)
CREATE INDEX idx_tasks_search ON tasks (project_id, sprint_id, status, assignee_id);


-- 6. Bảng Bình luận (UC09)
CREATE TABLE task_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. Bảng Nhật ký công việc (UC10)
CREATE TABLE worklogs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    work_date DATE NOT NULL,
    hours NUMERIC(4, 2) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- RÀNG BUỘC nghiệp vụ: Số giờ phải lớn hơn 0 và nhỏ hơn hoặc bằng 24h/ngày
    CONSTRAINT chk_worklog_hours CHECK (hours > 0 AND hours <= 24)
);

-- 8. Bảng Lịch sử hoạt động của Task (UC11)
CREATE TABLE task_activities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    action VARCHAR(50) NOT NULL, -- E.g., 'STATUS_CHANGED', 'ASSIGNEE_CHANGED'
    old_value TEXT,
    new_value TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

