package fpt.qn.pms.dashboard;

import static fpt.qn.pms.jooq.Tables.TASK_STATUSES;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import fpt.qn.pms.BaseIntegrationTest;
import fpt.qn.pms.jooq.enums.ProjectMemberStatus;
import fpt.qn.pms.jooq.enums.ProjectRole;
import fpt.qn.pms.jooq.enums.ProjectStatus;
import fpt.qn.pms.jooq.enums.SprintStatus;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.TaskPriority;
import fpt.qn.pms.jooq.enums.TaskType;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.ProjectMembersRecord;
import fpt.qn.pms.jooq.tables.records.ProjectsRecord;
import fpt.qn.pms.jooq.tables.records.SprintsRecord;
import fpt.qn.pms.jooq.tables.records.TasksRecord;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.jooq.tables.records.WorklogsRecord;
import fpt.qn.pms.project.repository.ProjectRepository;
import fpt.qn.pms.projectmember.repository.ProjectMemberRepository;
import fpt.qn.pms.security.JwtTokenProvider;
import fpt.qn.pms.sprint.repository.SprintRepository;
import fpt.qn.pms.task.repository.TaskRepository;
import fpt.qn.pms.user.repository.UserRepository;
import fpt.qn.pms.worklog.repository.WorklogRepository;

class DashboardIntegrationTest extends BaseIntegrationTest {

    MockMvc mockMvc;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectMemberRepository projectMemberRepository;

    @Autowired
    SprintRepository sprintRepository;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    WorklogRepository worklogRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    DSLContext dsl;

    String adminToken;
    String pmToken;
    String devToken;
    String outsiderToken;

    UsersRecord adminUser;
    UsersRecord pmUser;
    UsersRecord devUser;
    UsersRecord outsiderUser;

    ProjectsRecord project;
    SprintsRecord activeSprint;
    TasksRecord openTask;
    TasksRecord doneTask;
    TasksRecord overdueTask;

    UUID todoStatusId;
    UUID inProgressStatusId;
    UUID doneStatusId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // 1. Seed Users
        adminUser = userRepository.findByUsername("dash_admin").orElseGet(() -> {
            UsersRecord record = new UsersRecord();
            record.setEmployeeId("DASH_EMP_001");
            record.setUsername("dash_admin");
            record.setFullName("Dash Admin");
            record.setEmail("dash_admin@pms.com");
            record.setPassword(passwordEncoder.encode("password"));
            record.setRole(SysRole.ADMIN);
            record.setStatus(UserStatus.ACTIVE);
            return userRepository.create(record);
        });

        pmUser = userRepository.findByUsername("dash_pm").orElseGet(() -> {
            UsersRecord record = new UsersRecord();
            record.setEmployeeId("DASH_EMP_002");
            record.setUsername("dash_pm");
            record.setFullName("Dash PM");
            record.setEmail("dash_pm@pms.com");
            record.setPassword(passwordEncoder.encode("password"));
            record.setRole(SysRole.USER);
            record.setStatus(UserStatus.ACTIVE);
            return userRepository.create(record);
        });

        devUser = userRepository.findByUsername("dash_dev").orElseGet(() -> {
            UsersRecord record = new UsersRecord();
            record.setEmployeeId("DASH_EMP_003");
            record.setUsername("dash_dev");
            record.setFullName("Dash Dev");
            record.setEmail("dash_dev@pms.com");
            record.setPassword(passwordEncoder.encode("password"));
            record.setRole(SysRole.USER);
            record.setStatus(UserStatus.ACTIVE);
            return userRepository.create(record);
        });

        outsiderUser = userRepository.findByUsername("dash_outsider").orElseGet(() -> {
            UsersRecord record = new UsersRecord();
            record.setEmployeeId("DASH_EMP_004");
            record.setUsername("dash_outsider");
            record.setFullName("Dash Outsider");
            record.setEmail("dash_outsider@pms.com");
            record.setPassword(passwordEncoder.encode("password"));
            record.setRole(SysRole.USER);
            record.setStatus(UserStatus.ACTIVE);
            return userRepository.create(record);
        });

        // 2. Seed Tokens
        adminToken = jwtTokenProvider.generateAccessToken(adminUser.getUsername(), SysRole.ADMIN.getLiteral());
        pmToken = jwtTokenProvider.generateAccessToken(pmUser.getUsername(), SysRole.USER.getLiteral());
        devToken = jwtTokenProvider.generateAccessToken(devUser.getUsername(), SysRole.USER.getLiteral());
        outsiderToken = jwtTokenProvider.generateAccessToken(outsiderUser.getUsername(), SysRole.USER.getLiteral());

        // 3. Seed Project
        ProjectsRecord projectRecord = new ProjectsRecord();
        projectRecord.setProjectCode("DSHPRJ_" + UUID.randomUUID().toString().substring(0, 6));
        projectRecord.setProjectName("Dashboard Test Project");
        projectRecord.setStartDate(LocalDate.now());
        projectRecord.setEndDate(LocalDate.now().plusMonths(2));
        projectRecord.setStatus(ProjectStatus.ACTIVE);
        project = projectRepository.create(projectRecord);

        // 4. Seed Project Members
        ProjectMembersRecord pmMember = new ProjectMembersRecord();
        pmMember.setProjectId(project.getId());
        pmMember.setUserId(pmUser.getId());
        pmMember.setProjectRole(ProjectRole.PM);
        pmMember.setStatus(ProjectMemberStatus.ACTIVE);
        projectMemberRepository.create(pmMember);

        ProjectMembersRecord devMember = new ProjectMembersRecord();
        devMember.setProjectId(project.getId());
        devMember.setUserId(devUser.getId());
        devMember.setProjectRole(ProjectRole.DEV);
        devMember.setStatus(ProjectMemberStatus.ACTIVE);
        projectMemberRepository.create(devMember);

        // 5. Seed Task Statuses for this project
        todoStatusId = dsl.insertInto(TASK_STATUSES)
                .set(TASK_STATUSES.PROJECT_ID, project.getId())
                .set(TASK_STATUSES.NAME, "TODO")
                .set(TASK_STATUSES.COLOR, "#808080")
                .set(TASK_STATUSES.IS_INITIAL, true)
                .set(TASK_STATUSES.IS_FINAL, false)
                .set(TASK_STATUSES.IS_ACTIVE, true)
                .returning(TASK_STATUSES.ID)
                .fetchOne(TASK_STATUSES.ID);

        inProgressStatusId = dsl.insertInto(TASK_STATUSES)
                .set(TASK_STATUSES.PROJECT_ID, project.getId())
                .set(TASK_STATUSES.NAME, "IN_PROGRESS")
                .set(TASK_STATUSES.COLOR, "#0052CC")
                .set(TASK_STATUSES.IS_INITIAL, false)
                .set(TASK_STATUSES.IS_FINAL, false)
                .set(TASK_STATUSES.IS_ACTIVE, true)
                .returning(TASK_STATUSES.ID)
                .fetchOne(TASK_STATUSES.ID);

        doneStatusId = dsl.insertInto(TASK_STATUSES)
                .set(TASK_STATUSES.PROJECT_ID, project.getId())
                .set(TASK_STATUSES.NAME, "DONE")
                .set(TASK_STATUSES.COLOR, "#36B37E")
                .set(TASK_STATUSES.IS_INITIAL, false)
                .set(TASK_STATUSES.IS_FINAL, true)
                .set(TASK_STATUSES.IS_ACTIVE, true)
                .returning(TASK_STATUSES.ID)
                .fetchOne(TASK_STATUSES.ID);

        // 6. Seed Active Sprint
        SprintsRecord sprintRecord = new SprintsRecord();
        sprintRecord.setProjectId(project.getId());
        sprintRecord.setSprintName("Sprint 1");
        sprintRecord.setStartDate(LocalDate.now().minusDays(5));
        sprintRecord.setEndDate(LocalDate.now().plusDays(9));
        sprintRecord.setStatus(SprintStatus.ACTIVE);
        activeSprint = sprintRepository.create(sprintRecord);

        // 7. Seed Tasks for devUser
        // 7a. Open Task (IN_PROGRESS — not final)
        TasksRecord t1 = new TasksRecord();
        t1.setTaskKey("DSH-" + System.currentTimeMillis() % 100000 + "-1");
        t1.setProjectId(project.getId());
        t1.setSprintId(activeSprint.getId());
        t1.setSummary("Open Task");
        t1.setTaskType(TaskType.TASK);
        t1.setPriority(TaskPriority.HIGH);
        t1.setStatusId(inProgressStatusId);
        t1.setAssigneeId(devUser.getId());
        t1.setReporterId(pmUser.getId());
        t1.setDueDate(LocalDate.now().plusDays(3));
        openTask = taskRepository.create(t1);

        // 7b. Completed Task (DONE — is_final = true)
        TasksRecord t2 = new TasksRecord();
        t2.setTaskKey("DSH-" + System.currentTimeMillis() % 100000 + "-2");
        t2.setProjectId(project.getId());
        t2.setSprintId(activeSprint.getId());
        t2.setSummary("Done Task");
        t2.setTaskType(TaskType.STORY);
        t2.setPriority(TaskPriority.LOW);
        t2.setStatusId(doneStatusId);
        t2.setAssigneeId(devUser.getId());
        t2.setReporterId(pmUser.getId());
        doneTask = taskRepository.create(t2);

        // 7c. Overdue Task (TODO — not final, past due date)
        TasksRecord t3 = new TasksRecord();
        t3.setTaskKey("DSH-" + System.currentTimeMillis() % 100000 + "-3");
        t3.setProjectId(project.getId());
        t3.setSummary("Overdue Task");
        t3.setTaskType(TaskType.BUG);
        t3.setPriority(TaskPriority.CRITICAL);
        t3.setStatusId(todoStatusId);
        t3.setAssigneeId(devUser.getId());
        t3.setReporterId(pmUser.getId());
        t3.setDueDate(LocalDate.now().minusDays(2));
        overdueTask = taskRepository.create(t3);

        // 8. Seed Worklog for devUser on openTask
        WorklogsRecord worklog = new WorklogsRecord();
        worklog.setTaskId(openTask.getId());
        worklog.setUserId(devUser.getId());
        worklog.setWorkDate(LocalDate.now());
        worklog.setHours(BigDecimal.valueOf(4.5));
        worklog.setDescription("Implemented core feature");
        worklogRepository.create(worklog);
    }

    @Test
    void getPersonalDashboard_shouldReturnCorrectMetrics_whenAuthenticated() throws Exception {
        mockMvc.perform(get("/api/dashboard/me")
                        .header("Authorization", "Bearer " + devToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.myOpenTasks").value(2)) // IN_PROGRESS + TODO (Overdue)
                .andExpect(jsonPath("$.data.myCompletedTasks").value(1)) // DONE (is_final)
                .andExpect(jsonPath("$.data.myOverdueTasks").value(1)) // Overdue Task
                .andExpect(jsonPath("$.data.totalLoggedHours").value(4.5));
    }

    @Test
    void getProjectDashboard_shouldReturnCorrectMetrics_whenUserIsPm() throws Exception {
        mockMvc.perform(get("/api/dashboard/project/" + project.getId())
                        .header("Authorization", "Bearer " + pmToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalTasks").value(3))
                .andExpect(jsonPath("$.data.taskByStatus.IN_PROGRESS").value(1))
                .andExpect(jsonPath("$.data.taskByStatus.DONE").value(1))
                .andExpect(jsonPath("$.data.taskByStatus.TODO").value(1))
                .andExpect(jsonPath("$.data.taskByPriority.CRITICAL").value(1))
                .andExpect(jsonPath("$.data.sprintProgress.sprintName").value("Sprint 1"))
                .andExpect(jsonPath("$.data.sprintProgress.totalTasks").value(2))
                .andExpect(jsonPath("$.data.sprintProgress.doneTasks").value(1))
                .andExpect(jsonPath("$.data.sprintProgress.percentComplete").value(50.0));
    }

    @Test
    void getProjectDashboard_shouldReturn200_whenUserIsAdmin() throws Exception {
        mockMvc.perform(get("/api/dashboard/project/" + project.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getProjectDashboard_shouldReturn403_whenUserIsDevOrOutsider() throws Exception {
        mockMvc.perform(get("/api/dashboard/project/" + project.getId())
                        .header("Authorization", "Bearer " + devToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/dashboard/project/" + project.getId())
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProjectDashboard_shouldReturn404_whenProjectNotFound() throws Exception {
        UUID randomProjectId = UUID.randomUUID();
        mockMvc.perform(get("/api/dashboard/project/" + randomProjectId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}
