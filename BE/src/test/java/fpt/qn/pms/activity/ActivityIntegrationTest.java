package fpt.qn.pms.activity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import fpt.qn.pms.BaseIntegrationTest;
import fpt.qn.pms.activity.event.TaskActivityEvent;
import fpt.qn.pms.activity.repository.ActivityRepository;
import fpt.qn.pms.jooq.enums.ActivityAction;
import fpt.qn.pms.jooq.enums.ProjectMemberStatus;
import fpt.qn.pms.jooq.enums.ProjectRole;
import fpt.qn.pms.jooq.enums.ProjectStatus;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.TaskPriority;
import fpt.qn.pms.jooq.enums.TaskStatus;
import fpt.qn.pms.jooq.enums.TaskType;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.ProjectMembersRecord;
import fpt.qn.pms.jooq.tables.records.ProjectsRecord;
import fpt.qn.pms.jooq.tables.records.TasksRecord;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.project.repository.ProjectRepository;
import fpt.qn.pms.projectmember.repository.ProjectMemberRepository;
import fpt.qn.pms.security.JwtTokenProvider;
import fpt.qn.pms.task.repository.TaskRepository;
import fpt.qn.pms.user.repository.UserRepository;

class ActivityIntegrationTest extends BaseIntegrationTest {

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
    TaskRepository taskRepository;

    @Autowired
    ActivityRepository activityRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    String adminToken;
    String memberToken;
    String outsiderToken;

    UsersRecord adminUser;
    UsersRecord memberUser;
    UsersRecord outsiderUser;

    ProjectsRecord project;
    TasksRecord task;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // 1. Seed Admin
        adminUser = userRepository.findByUsername("act_admin").orElseGet(() -> {
            UsersRecord record = new UsersRecord();
            record.setEmployeeId("ACT_EMP_001");
            record.setUsername("act_admin");
            record.setFullName("Act Admin");
            record.setEmail("act_admin@pms.com");
            record.setPassword(passwordEncoder.encode("password"));
            record.setRole(SysRole.ADMIN);
            record.setStatus(UserStatus.ACTIVE);
            return userRepository.create(record);
        });

        // 2. Seed Member User
        memberUser = userRepository.findByUsername("act_member").orElseGet(() -> {
            UsersRecord record = new UsersRecord();
            record.setEmployeeId("ACT_EMP_002");
            record.setUsername("act_member");
            record.setFullName("Act Member");
            record.setEmail("act_member@pms.com");
            record.setPassword(passwordEncoder.encode("password"));
            record.setRole(SysRole.USER);
            record.setStatus(UserStatus.ACTIVE);
            return userRepository.create(record);
        });

        // 3. Seed Outsider User
        outsiderUser = userRepository.findByUsername("act_outsider").orElseGet(() -> {
            UsersRecord record = new UsersRecord();
            record.setEmployeeId("ACT_EMP_003");
            record.setUsername("act_outsider");
            record.setFullName("Act Outsider");
            record.setEmail("act_outsider@pms.com");
            record.setPassword(passwordEncoder.encode("password"));
            record.setRole(SysRole.USER);
            record.setStatus(UserStatus.ACTIVE);
            return userRepository.create(record);
        });

        // 4. Seed Tokens
        adminToken = jwtTokenProvider.generateAccessToken(adminUser.getUsername(), SysRole.ADMIN.getLiteral());
        memberToken = jwtTokenProvider.generateAccessToken(memberUser.getUsername(), SysRole.USER.getLiteral());
        outsiderToken = jwtTokenProvider.generateAccessToken(outsiderUser.getUsername(), SysRole.USER.getLiteral());

        // 5. Seed Project
        ProjectsRecord projectRecord = new ProjectsRecord();
        projectRecord.setProjectCode("ACTPRJ_" + UUID.randomUUID().toString().substring(0, 6));
        projectRecord.setProjectName("Activity Test Project");
        projectRecord.setStartDate(LocalDate.now());
        projectRecord.setEndDate(LocalDate.now().plusMonths(1));
        projectRecord.setStatus(ProjectStatus.ACTIVE);
        project = projectRepository.create(projectRecord);

        // 6. Assign Member to Project
        ProjectMembersRecord memberRecord = new ProjectMembersRecord();
        memberRecord.setProjectId(project.getId());
        memberRecord.setUserId(memberUser.getId());
        memberRecord.setProjectRole(ProjectRole.DEV);
        memberRecord.setStatus(ProjectMemberStatus.ACTIVE);
        projectMemberRepository.create(memberRecord);

        // 7. Seed Task
        TasksRecord taskRecord = new TasksRecord();
        taskRecord.setTaskKey("ACT-" + System.currentTimeMillis() % 100000);
        taskRecord.setProjectId(project.getId());
        taskRecord.setSummary("Activity Test Task");
        taskRecord.setTaskType(TaskType.TASK);
        taskRecord.setPriority(TaskPriority.MEDIUM);
        taskRecord.setStatus(TaskStatus.TODO);
        taskRecord.setReporterId(adminUser.getId());
        task = taskRepository.create(taskRecord);
    }

    @Test
    void getActivitiesByTaskId_shouldReturnPagedActivities_whenUserIsMember() throws Exception {
        // Trigger event
        eventPublisher.publishEvent(new TaskActivityEvent(
                task.getId(),
                memberUser.getId(),
                ActivityAction.TASK_CREATED,
                null,
                "Task Created"
        ));

        mockMvc.perform(get("/api/tasks/" + task.getId() + "/activities")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].action").value("TASK_CREATED"))
                .andExpect(jsonPath("$.data.items[0].userName").value("act_member"));
    }

    @Test
    void getActivitiesByTaskId_shouldReturnActivities_whenAdminTokenProvided() throws Exception {
        eventPublisher.publishEvent(new TaskActivityEvent(
                task.getId(),
                adminUser.getId(),
                ActivityAction.STATUS_CHANGED,
                "TODO",
                "IN_PROGRESS"
        ));

        mockMvc.perform(get("/api/tasks/" + task.getId() + "/activities")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items[0].action").value("STATUS_CHANGED"))
                .andExpect(jsonPath("$.data.items[0].oldValue").value("TODO"))
                .andExpect(jsonPath("$.data.items[0].newValue").value("IN_PROGRESS"));
    }

    @Test
    void getActivitiesByTaskId_shouldReturn403_whenUserIsNotMember() throws Exception {
        mockMvc.perform(get("/api/tasks/" + task.getId() + "/activities")
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getActivitiesByTaskId_shouldReturn404_whenTaskDoesNotExist() throws Exception {
        UUID randomTaskId = UUID.randomUUID();
        mockMvc.perform(get("/api/tasks/" + randomTaskId + "/activities")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}
