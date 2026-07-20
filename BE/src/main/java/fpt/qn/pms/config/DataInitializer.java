package fpt.qn.pms.config;

import static fpt.qn.pms.jooq.Tables.PROJECTS;
import static fpt.qn.pms.jooq.Tables.PROJECT_MEMBERS;
import static fpt.qn.pms.jooq.Tables.SPRINTS;
import static fpt.qn.pms.jooq.Tables.TASKS;
import static fpt.qn.pms.jooq.Tables.USERS;

import java.time.LocalDate;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import fpt.qn.pms.jooq.enums.ProjectMemberStatus;
import fpt.qn.pms.jooq.enums.ProjectRole;
import fpt.qn.pms.jooq.enums.ProjectStatus;
import fpt.qn.pms.jooq.enums.SprintStatus;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.TaskPriority;
import fpt.qn.pms.jooq.enums.TaskStatus;
import fpt.qn.pms.jooq.enums.TaskType;
import fpt.qn.pms.jooq.enums.UserStatus;

@Component
public class DataInitializer implements CommandLineRunner {

    public static final UUID ADMIN_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final UUID DEV_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    public static final UUID PROJECT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID SPRINT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID TASK_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private final DSLContext dsl;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(DSLContext dsl, PasswordEncoder passwordEncoder) {
        this.dsl = dsl;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        seedUsers();
        seedProjects();
        seedProjectMembers();
        seedSprints();
        seedTasks();
        System.out.println("✅ Sample test data initialized successfully!");
    }

    private void seedUsers() {
        if (!dsl.fetchExists(USERS, USERS.ID.eq(ADMIN_USER_ID))) {
            dsl.insertInto(USERS)
                    .set(USERS.ID, ADMIN_USER_ID)
                    .set(USERS.USERNAME, "admin")
                    .set(USERS.PASSWORD, passwordEncoder.encode("admin123"))
                    .set(USERS.FULL_NAME, "Project Manager Admin")
                    .set(USERS.EMAIL, "admin@pms.com")
                    .set(USERS.EMPLOYEE_ID, "EMP-001")
                    .set(USERS.ROLE, SysRole.ADMIN)
                    .set(USERS.STATUS, UserStatus.ACTIVE)
                    .execute();
        }

        if (!dsl.fetchExists(USERS, USERS.ID.eq(DEV_USER_ID))) {
            dsl.insertInto(USERS)
                    .set(USERS.ID, DEV_USER_ID)
                    .set(USERS.USERNAME, "dev1")
                    .set(USERS.PASSWORD, passwordEncoder.encode("dev123"))
                    .set(USERS.FULL_NAME, "Developer One")
                    .set(USERS.EMAIL, "dev1@pms.com")
                    .set(USERS.EMPLOYEE_ID, "EMP-002")
                    .set(USERS.ROLE, SysRole.USER)
                    .set(USERS.STATUS, UserStatus.ACTIVE)
                    .execute();
        }
    }

    private void seedProjects() {
        if (!dsl.fetchExists(PROJECTS, PROJECTS.ID.eq(PROJECT_ID))) {
            dsl.insertInto(PROJECTS)
                    .set(PROJECTS.ID, PROJECT_ID)
                    .set(PROJECTS.PROJECT_CODE, "WEB")
                    .set(PROJECTS.PROJECT_NAME, "Web Portal Project")
                    .set(PROJECTS.DESCRIPTION, "Project Management Web Portal Application")
                    .set(PROJECTS.START_DATE, LocalDate.now())
                    .set(PROJECTS.END_DATE, LocalDate.now().plusMonths(3))
                    .set(PROJECTS.STATUS, ProjectStatus.ACTIVE)
                    .set(PROJECTS.CREATED_BY, ADMIN_USER_ID)
                    .set(PROJECTS.UPDATED_BY, ADMIN_USER_ID)
                    .execute();
        }
    }

    private void seedProjectMembers() {
        // Admin as PM
        boolean pmExists = dsl.fetchExists(
                PROJECT_MEMBERS,
                PROJECT_MEMBERS.PROJECT_ID.eq(PROJECT_ID).and(PROJECT_MEMBERS.USER_ID.eq(ADMIN_USER_ID))
        );
        if (!pmExists) {
            dsl.insertInto(PROJECT_MEMBERS)
                    .set(PROJECT_MEMBERS.ID, UUID.randomUUID())
                    .set(PROJECT_MEMBERS.PROJECT_ID, PROJECT_ID)
                    .set(PROJECT_MEMBERS.USER_ID, ADMIN_USER_ID)
                    .set(PROJECT_MEMBERS.PROJECT_ROLE, ProjectRole.PM)
                    .set(PROJECT_MEMBERS.STATUS, ProjectMemberStatus.ACTIVE)
                    .execute();
        }

        // Dev1 as DEV
        boolean devExists = dsl.fetchExists(
                PROJECT_MEMBERS,
                PROJECT_MEMBERS.PROJECT_ID.eq(PROJECT_ID).and(PROJECT_MEMBERS.USER_ID.eq(DEV_USER_ID))
        );
        if (!devExists) {
            dsl.insertInto(PROJECT_MEMBERS)
                    .set(PROJECT_MEMBERS.ID, UUID.randomUUID())
                    .set(PROJECT_MEMBERS.PROJECT_ID, PROJECT_ID)
                    .set(PROJECT_MEMBERS.USER_ID, DEV_USER_ID)
                    .set(PROJECT_MEMBERS.PROJECT_ROLE, ProjectRole.DEV)
                    .set(PROJECT_MEMBERS.STATUS, ProjectMemberStatus.ACTIVE)
                    .execute();
        }
    }

    private void seedSprints() {
        if (!dsl.fetchExists(SPRINTS, SPRINTS.ID.eq(SPRINT_ID))) {
            dsl.insertInto(SPRINTS)
                    .set(SPRINTS.ID, SPRINT_ID)
                    .set(SPRINTS.PROJECT_ID, PROJECT_ID)
                    .set(SPRINTS.SPRINT_NAME, "Sprint 1")
                    .set(SPRINTS.GOAL, "Initial foundation setup and core task features")
                    .set(SPRINTS.START_DATE, LocalDate.now())
                    .set(SPRINTS.END_DATE, LocalDate.now().plusWeeks(2))
                    .set(SPRINTS.STATUS, SprintStatus.ACTIVE)
                    .set(SPRINTS.CREATED_BY, ADMIN_USER_ID)
                    .set(SPRINTS.UPDATED_BY, ADMIN_USER_ID)
                    .execute();
        }
    }

    private void seedTasks() {
        if (!dsl.fetchExists(TASKS, TASKS.ID.eq(TASK_ID))) {
            dsl.insertInto(TASKS)
                    .set(TASKS.ID, TASK_ID)
                    .set(TASKS.TASK_KEY, "WEB-1")
                    .set(TASKS.PROJECT_ID, PROJECT_ID)
                    .set(TASKS.SPRINT_ID, SPRINT_ID)
                    .set(TASKS.SUMMARY, "Setup Backend Project Structure & Task API")
                    .set(TASKS.DESCRIPTION, "Initial setup for Jira-like Task management endpoints")
                    .set(TASKS.TASK_TYPE, TaskType.TASK)
                    .set(TASKS.PRIORITY, TaskPriority.HIGH)
                    .set(TASKS.STATUS, TaskStatus.TODO)
                    .set(TASKS.ASSIGNEE_ID, DEV_USER_ID)
                    .set(TASKS.REPORTER_ID, ADMIN_USER_ID)
                    .set(TASKS.CREATED_BY, ADMIN_USER_ID)
                    .set(TASKS.UPDATED_BY, ADMIN_USER_ID)
                    .execute();
        }
    }
}
