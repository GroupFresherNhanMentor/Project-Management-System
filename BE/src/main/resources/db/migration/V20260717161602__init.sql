-- ============================================================
-- Enums
-- ============================================================
CREATE TYPE sys_role               AS ENUM ('ADMIN', 'USER');
CREATE TYPE user_status            AS ENUM ('ACTIVE', 'LOCKED');

CREATE TYPE project_status         AS ENUM ('PLANNING', 'ACTIVE', 'ON_HOLD', 'COMPLETED');
CREATE TYPE project_role           AS ENUM ('PM', 'DEV', 'TESTER');
CREATE TYPE project_member_status  AS ENUM ('ACTIVE', 'INACTIVE');

CREATE TYPE sprint_status          AS ENUM ('PLANNED', 'ACTIVE', 'CLOSED');

CREATE TYPE task_type              AS ENUM ('STORY', 'TASK', 'BUG');
CREATE TYPE task_priority          AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL');
CREATE TYPE task_status            AS ENUM ('TODO', 'IN_PROGRESS', 'TESTING', 'DONE');

CREATE TYPE activity_action        AS ENUM (
    'TASK_CREATED', 'STATUS_CHANGED', 'PRIORITY_CHANGED',
    'ASSIGNEE_CHANGED', 'COMMENT_ADDED'
);

-- ============================================================
-- 1. users (UC01, UC02)
-- ============================================================
CREATE TABLE users (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id VARCHAR(50)  NOT NULL UNIQUE,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    full_name   VARCHAR(150) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        sys_role     NOT NULL DEFAULT 'USER',
    status      user_status  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 2. projects (UC03)
-- ============================================================
CREATE TABLE projects (
    id           UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    project_code VARCHAR(20)    NOT NULL UNIQUE,
    project_name VARCHAR(200)   NOT NULL,
    description  TEXT,
    start_date   DATE           NOT NULL,
    end_date     DATE           NOT NULL,
    status       project_status NOT NULL DEFAULT 'PLANNING',
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by   UUID           REFERENCES users(id),
    updated_by   UUID           REFERENCES users(id),
    CONSTRAINT chk_project_dates CHECK (end_date >= start_date)
);

-- ============================================================
-- 3. project_members (UC04) — soft-delete via status INACTIVE
-- ============================================================
CREATE TABLE project_members (
    id           UUID                  PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id   UUID                  NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    user_id      UUID                  NOT NULL REFERENCES users(id)    ON DELETE CASCADE,
    project_role project_role          NOT NULL DEFAULT 'DEV',
    status       project_member_status NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMPTZ           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ux_project_member UNIQUE (project_id, user_id)
);

-- ============================================================
-- 4. sprints (UC05)
-- ============================================================
CREATE TABLE sprints (
    id          UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id  UUID          NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    sprint_name VARCHAR(200)  NOT NULL,
    goal        TEXT,
    start_date  DATE          NOT NULL,
    end_date    DATE          NOT NULL,
    status      sprint_status NOT NULL DEFAULT 'PLANNED',
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  UUID          REFERENCES users(id),
    updated_by  UUID          REFERENCES users(id),
    CONSTRAINT chk_sprint_dates CHECK (end_date >= start_date)
);

-- Max 1 ACTIVE sprint per project (FR-SPR-02)
CREATE UNIQUE INDEX ux_sprint_one_active_per_project
    ON sprints (project_id)
    WHERE (status = 'ACTIVE');

-- ============================================================
-- 5. tasks (UC06, UC07, UC08)
-- ============================================================
CREATE TABLE tasks (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    task_key      VARCHAR(30)   NOT NULL UNIQUE,
    project_id    UUID          NOT NULL REFERENCES projects(id)  ON DELETE CASCADE,
    sprint_id     UUID          REFERENCES sprints(id)            ON DELETE SET NULL,
    summary       VARCHAR(500)  NOT NULL,
    description   TEXT,
    task_type     task_type     NOT NULL DEFAULT 'TASK',
    priority      task_priority NOT NULL DEFAULT 'MEDIUM',
    status        task_status   NOT NULL DEFAULT 'TODO',
    assignee_id   UUID          REFERENCES users(id) ON DELETE SET NULL,
    reporter_id   UUID          NOT NULL REFERENCES users(id),
    story_point   NUMERIC(5,2),
    estimate_hour NUMERIC(6,2),
    due_date      DATE,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by    UUID          REFERENCES users(id),
    updated_by    UUID          REFERENCES users(id)
);

-- ============================================================
-- 6. task_comments (UC09)
-- ============================================================
CREATE TABLE task_comments (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id     UUID        NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    content     TEXT        NOT NULL,
    created_by  UUID        NOT NULL REFERENCES users(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 7. worklogs (UC10)
-- ============================================================
CREATE TABLE worklogs (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id     UUID        NOT NULL REFERENCES tasks(id)  ON DELETE CASCADE,
    user_id     UUID        NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
    work_date   DATE        NOT NULL,
    hours       NUMERIC(4,2) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_worklog_hours CHECK (hours > 0 AND hours <= 24)
);

-- ============================================================
-- 8. task_activities (UC11)
-- ============================================================
CREATE TABLE task_activities (
    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id     UUID            NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    user_id     UUID            NOT NULL REFERENCES users(id),
    action      activity_action NOT NULL,
    old_value   TEXT,
    new_value   TEXT,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- Indexes (NFR-05: response < 3s with ~10k tasks)
-- ============================================================
CREATE INDEX idx_tasks_project_id  ON tasks(project_id);
CREATE INDEX idx_tasks_sprint_id   ON tasks(sprint_id);
CREATE INDEX idx_tasks_status      ON tasks(status);
CREATE INDEX idx_tasks_priority    ON tasks(priority);
CREATE INDEX idx_tasks_assignee_id ON tasks(assignee_id);

CREATE INDEX idx_worklogs_task_id   ON worklogs(task_id);
CREATE INDEX idx_worklogs_work_date ON worklogs(work_date);

CREATE INDEX idx_task_activities_task_id   ON task_activities(task_id);
CREATE INDEX idx_task_activities_created_at ON task_activities(created_at);

CREATE INDEX idx_project_members_project_id ON project_members(project_id);
CREATE INDEX idx_project_members_user_id    ON project_members(user_id);

CREATE INDEX idx_sprints_project_id ON sprints(project_id);


-- ============================================================
-- Auto-update updated_at via trigger
-- ============================================================
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_projects_updated_at
    BEFORE UPDATE ON projects
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_sprints_updated_at
    BEFORE UPDATE ON sprints
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_tasks_updated_at
    BEFORE UPDATE ON tasks
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_project_members_updated_at
    BEFORE UPDATE ON project_members
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

