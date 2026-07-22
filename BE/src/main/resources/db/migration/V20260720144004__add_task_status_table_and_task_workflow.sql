-- ============================================================
-- task_statuses — project-scoped, configurable task statuses
-- ============================================================
CREATE TABLE task_statuses (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id   UUID         NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    name         VARCHAR(50)  NOT NULL,
    color        VARCHAR(7)   NOT NULL DEFAULT '#6B7280',
    is_initial   BOOLEAN      NOT NULL DEFAULT FALSE,
    is_final     BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_task_status_color CHECK (color ~ '^#[0-9A-Fa-f]{6}$'),
    CONSTRAINT chk_task_status_not_both_initial_and_final CHECK (NOT (is_initial AND is_final))
);

CREATE INDEX idx_task_statuses_project_id ON task_statuses(project_id);
CREATE UNIQUE INDEX ux_task_statuses_project_id_name ON task_statuses(project_id, LOWER(name));

CREATE TRIGGER trg_task_statuses_updated_at
    BEFORE UPDATE ON task_statuses
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();


