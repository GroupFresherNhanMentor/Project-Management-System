package fpt.qn.pms.config;

import static fpt.qn.pms.jooq.Tables.PROJECTS;
import static fpt.qn.pms.jooq.Tables.PROJECT_MEMBERS;
import static fpt.qn.pms.jooq.Tables.SPRINTS;
import static fpt.qn.pms.jooq.Tables.TASKS;
import static fpt.qn.pms.jooq.Tables.USERS;

import java.time.LocalDate;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
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

import org.springframework.context.annotation.Profile;

@Component
@Profile("!test")
public class DataInitializer implements ApplicationRunner {

    public static final UUID ADMIN_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final UUID DEV_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    public static final UUID PROJECT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID SPRINT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID TASK_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Value("${app.seed.admin.username}")
    private String adminUsername;

    @Value("${app.seed.admin.password}")
    private String adminPassword;

    @Value("${app.seed.admin.full-name}")
    private String adminFullName;

    @Value("${app.seed.admin.email}")
    private String adminEmail;

    @Value("${app.seed.admin.employee-id}")
    private String adminEmployeeId;

    private final DSLContext dsl;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(DSLContext dsl, PasswordEncoder passwordEncoder) {
        this.dsl = dsl;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        seedAdmin();
        seedUsers();
        seedProjects();
        seedProjectMembers();
        seedSprints();
        seedTasks();
        System.out.println("✅ Sample test data initialized successfully!");
    }

    private void seedAdmin() {
        if (!dsl.fetchExists(USERS, USERS.USERNAME.eq(adminUsername))) {
            dsl.insertInto(USERS)
                    .set(USERS.ID, ADMIN_USER_ID)
                    .set(USERS.USERNAME, adminUsername)
                    .set(USERS.PASSWORD, passwordEncoder.encode(adminPassword))
                    .set(USERS.FULL_NAME, adminFullName)
                    .set(USERS.EMAIL, adminEmail)
                    .set(USERS.EMPLOYEE_ID, adminEmployeeId)
                    .set(USERS.ROLE, SysRole.ADMIN)
                    .set(USERS.STATUS, UserStatus.ACTIVE)
                    .execute();
        }
    }

    private void seedUsers() {
        if (!dsl.fetchExists(USERS, USERS.USERNAME.eq("dev1"))) {
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

    private UUID getAdminUserId() {
        return dsl.select(USERS.ID)
                .from(USERS)
                .where(USERS.USERNAME.eq(adminUsername))
                .fetchOptional(USERS.ID)
                .orElse(ADMIN_USER_ID);
    }

    private UUID getDevUserId() {
        return dsl.select(USERS.ID)
                .from(USERS)
                .where(USERS.USERNAME.eq("dev1"))
                .fetchOptional(USERS.ID)
                .orElse(DEV_USER_ID);
    }

    private void seedProjects() {
        UUID adminId = getAdminUserId();
        if (!dsl.fetchExists(PROJECTS, PROJECTS.ID.eq(PROJECT_ID))) {
            dsl.insertInto(PROJECTS)
                    .set(PROJECTS.ID, PROJECT_ID)
                    .set(PROJECTS.PROJECT_CODE, "WEB")
                    .set(PROJECTS.PROJECT_NAME, "Web Portal Project")
                    .set(PROJECTS.DESCRIPTION, "Project Management Web Portal Application")
                    .set(PROJECTS.START_DATE, LocalDate.now())
                    .set(PROJECTS.END_DATE, LocalDate.now().plusMonths(3))
                    .set(PROJECTS.STATUS, ProjectStatus.ACTIVE)
                    .set(PROJECTS.CREATED_BY, adminId)
                    .set(PROJECTS.UPDATED_BY, adminId)
                    .execute();
        }
    }

    private void seedProjectMembers() {
        UUID adminId = getAdminUserId();
        UUID devId = getDevUserId();

        // Admin as PM
        boolean pmExists = dsl.fetchExists(
                PROJECT_MEMBERS,
                PROJECT_MEMBERS.PROJECT_ID.eq(PROJECT_ID).and(PROJECT_MEMBERS.USER_ID.eq(adminId))
        );
        if (!pmExists) {
            dsl.insertInto(PROJECT_MEMBERS)
                    .set(PROJECT_MEMBERS.ID, UUID.randomUUID())
                    .set(PROJECT_MEMBERS.PROJECT_ID, PROJECT_ID)
                    .set(PROJECT_MEMBERS.USER_ID, adminId)
                    .set(PROJECT_MEMBERS.PROJECT_ROLE, ProjectRole.PM)
                    .set(PROJECT_MEMBERS.STATUS, ProjectMemberStatus.ACTIVE)
                    .execute();
        }

        // Dev1 as DEV
        boolean devExists = dsl.fetchExists(
                PROJECT_MEMBERS,
                PROJECT_MEMBERS.PROJECT_ID.eq(PROJECT_ID).and(PROJECT_MEMBERS.USER_ID.eq(devId))
        );
        if (!devExists) {
            dsl.insertInto(PROJECT_MEMBERS)
                    .set(PROJECT_MEMBERS.ID, UUID.randomUUID())
                    .set(PROJECT_MEMBERS.PROJECT_ID, PROJECT_ID)
                    .set(PROJECT_MEMBERS.USER_ID, devId)
                    .set(PROJECT_MEMBERS.PROJECT_ROLE, ProjectRole.DEV)
                    .set(PROJECT_MEMBERS.STATUS, ProjectMemberStatus.ACTIVE)
                    .execute();
        }
    }

    private void seedSprints() {
        UUID adminId = getAdminUserId();
        if (!dsl.fetchExists(SPRINTS, SPRINTS.ID.eq(SPRINT_ID))) {
            dsl.insertInto(SPRINTS)
                    .set(SPRINTS.ID, SPRINT_ID)
                    .set(SPRINTS.PROJECT_ID, PROJECT_ID)
                    .set(SPRINTS.SPRINT_NAME, "Sprint 1")
                    .set(SPRINTS.GOAL, "Initial foundation setup and core task features")
                    .set(SPRINTS.START_DATE, LocalDate.now())
                    .set(SPRINTS.END_DATE, LocalDate.now().plusWeeks(2))
                    .set(SPRINTS.STATUS, SprintStatus.ACTIVE)
                    .set(SPRINTS.CREATED_BY, adminId)
                    .set(SPRINTS.UPDATED_BY, adminId)
                    .execute();
        }
    }

    private void seedTasks() {
        UUID adminId = getAdminUserId();
        UUID devId = getDevUserId();
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
                    .set(TASKS.ASSIGNEE_ID, devId)
                    .set(TASKS.REPORTER_ID, adminId)
                    .set(TASKS.CREATED_BY, adminId)
                    .set(TASKS.UPDATED_BY, adminId)
                    .execute();
        }
    }
}
