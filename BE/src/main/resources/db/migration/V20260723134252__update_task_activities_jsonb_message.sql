ALTER TABLE task_activities
    ALTER COLUMN old_value TYPE JSONB
        USING CASE WHEN old_value IS NULL THEN NULL ELSE to_jsonb(old_value) END,
    ALTER COLUMN new_value TYPE JSONB
        USING CASE WHEN new_value IS NULL THEN NULL ELSE to_jsonb(new_value) END,
    ADD COLUMN IF NOT EXISTS message TEXT;
