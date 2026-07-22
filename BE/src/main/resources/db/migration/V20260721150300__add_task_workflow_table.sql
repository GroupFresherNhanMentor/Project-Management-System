-- ============================================================
-- task_workflow — allowed status transitions per project
-- ============================================================
CREATE TABLE task_workflow (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    from_status_id UUID        NOT NULL REFERENCES task_statuses(id) ON DELETE CASCADE,
    to_status_id   UUID        NOT NULL REFERENCES task_statuses(id) ON DELETE CASCADE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ux_task_workflow_transition UNIQUE (from_status_id, to_status_id),
    CONSTRAINT chk_task_workflow_no_self_loop CHECK (from_status_id <> to_status_id)
);

CREATE INDEX idx_task_workflow_from_status_id ON task_workflow(from_status_id);
CREATE INDEX idx_task_workflow_to_status_id   ON task_workflow(to_status_id);