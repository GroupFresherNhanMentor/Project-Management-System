-- Seed initial admin account
INSERT INTO users (employee_id, username, full_name, email, password, role, status)
VALUES (
    'EMP001',
    'admin',
    'System Administrator',
    'admin@pms.com',
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a',
    'ADMIN'::sys_role,
    'ACTIVE'::user_status
) ON CONFLICT (username) DO NOTHING;
