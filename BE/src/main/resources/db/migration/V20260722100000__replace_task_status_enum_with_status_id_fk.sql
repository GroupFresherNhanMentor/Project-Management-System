-- Replace tasks.status (enum) with tasks.status_id (FK -> task_statuses)
ALTER TABLE tasks DROP COLUMN status;
ALTER TABLE tasks ADD COLUMN status_id UUID REFERENCES task_statuses(id) ON DELETE SET NULL;

CREATE INDEX idx_tasks_status_id ON tasks(status_id);

-- Drop the now-unused PostgreSQL enum type
DROP TYPE task_status;
