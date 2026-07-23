package fpt.qn.pms.projectmember;

import static fpt.qn.pms.jooq.Tables.PROJECT_MEMBERS;
import static fpt.qn.pms.jooq.Tables.PROJECTS;
import static fpt.qn.pms.jooq.Tables.TASKS;
import static fpt.qn.pms.jooq.Tables.USERS;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import fpt.qn.pms.BaseIntegrationTest;
import fpt.qn.pms.jooq.enums.ProjectMemberStatus;
import fpt.qn.pms.jooq.enums.ProjectRole;
import fpt.qn.pms.jooq.enums.ProjectStatus;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.TaskPriority;
import fpt.qn.pms.jooq.enums.TaskType;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.projectmember.dto.request.AddProjectMemberRequest;
import fpt.qn.pms.security.JwtTokenProvider;
import tools.jackson.databind.ObjectMapper;

class ProjectMemberControllerIntegrationTest extends BaseIntegrationTest {

    MockMvc mockMvc;

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    DSLContext dsl;

    UsersRecord admin;
    UsersRecord projectManager;
    UsersRecord developer;
    UsersRecord candidate;
    UUID projectId;
    UUID projectManagerMemberId;
    UUID developerMemberId;
    String adminToken;
    String pmToken;
    String developerToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        admin = insertUser("member-api-admin", SysRole.ADMIN);
        projectManager = insertUser("member-api-pm", SysRole.USER);
        developer = insertUser("member-api-dev", SysRole.USER);
        candidate = insertUser("member-api-candidate", SysRole.USER);
        projectId = insertProject("MEMAPI");
        projectManagerMemberId = insertMembership(
                projectId, projectManager.getId(), ProjectRole.PM, ProjectMemberStatus.ACTIVE);
        developerMemberId = insertMembership(
                projectId, developer.getId(), ProjectRole.DEV, ProjectMemberStatus.ACTIVE);
        adminToken = token(admin);
        pmToken = token(projectManager);
        developerToken = token(developer);
    }

    @Test
    void getMembers_shouldReturnPageForActiveProjectManager() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/members", projectId)
                        .header("Authorization", "Bearer " + pmToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.items.length()").value(2));
    }

    @Test
    void getMembers_shouldFilterByKeyword() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/members", projectId)
                        .param("keyword", projectManager.getEmail())
                        .header("Authorization", "Bearer " + pmToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.items[0].userId")
                        .value(projectManager.getId().toString()));
    }

    @Test
    void getMembers_shouldReturn200ForActiveDeveloper() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/members", projectId)
                        .header("Authorization", "Bearer " + developerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getCurrentMember_shouldReturnAuthenticatedMembership() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/members/me", projectId)
                        .header("Authorization", "Bearer " + pmToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(projectManager.getId().toString()))
                .andExpect(jsonPath("$.data.projectRole").value("PM"));
    }

    @Test
    void getMemberCandidates_shouldReturnAvailableUsersForProjectManager() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/members/candidates", projectId)
                        .param("keyword", "member-api-candidate")
                        .header("Authorization", "Bearer " + pmToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(candidate.getId().toString()))
                .andExpect(jsonPath("$.data.items[0].fullName").value(candidate.getFullName()));
    }

    @Test
    void getMemberCandidates_shouldReturn403ForDeveloper() throws Exception {
        mockMvc.perform(get("/api/projects/{projectId}/members/candidates", projectId)
                        .header("Authorization", "Bearer " + developerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void addMember_shouldReturn201ForAdministrator() throws Exception {
        AddProjectMemberRequest request = AddProjectMemberRequest.builder()
                .userId(candidate.getId())
                .projectRole(ProjectRole.TESTER)
                .build();

        mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.userId").value(candidate.getId().toString()))
                .andExpect(jsonPath("$.data.projectRole").value("TESTER"));
    }

    @Test
    void addMember_shouldReturn403ForDeveloper() throws Exception {
        AddProjectMemberRequest request = AddProjectMemberRequest.builder()
                .userId(candidate.getId())
                .projectRole(ProjectRole.DEV)
                .build();

        mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                        .header("Authorization", "Bearer " + developerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void addMember_shouldReturn409ForLockedUser() throws Exception {
        dsl.update(USERS)
                .set(USERS.STATUS, UserStatus.LOCKED)
                .where(USERS.ID.eq(candidate.getId()))
                .execute();
        AddProjectMemberRequest request = AddProjectMemberRequest.builder()
                .userId(candidate.getId())
                .projectRole(ProjectRole.TESTER)
                .build();

        mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Only active users can be added to a project"));
    }

    @Test
    void removeMember_shouldReturn204AndSoftDeleteForProjectManager() throws Exception {
        mockMvc.perform(delete("/api/projects/{projectId}/members/{memberId}", projectId, developerMemberId)
                        .header("Authorization", "Bearer " + pmToken))
                .andExpect(status().isNoContent());

        ProjectMemberStatus status = dsl.select(PROJECT_MEMBERS.STATUS)
                .from(PROJECT_MEMBERS)
                .where(PROJECT_MEMBERS.ID.eq(developerMemberId))
                .fetchOne(PROJECT_MEMBERS.STATUS);
        org.assertj.core.api.Assertions.assertThat(status).isEqualTo(ProjectMemberStatus.INACTIVE);
    }

    @Test
    void removeMember_shouldReturn403WhenProjectManagerRemovesSelf() throws Exception {
        mockMvc.perform(delete("/api/projects/{projectId}/members/{memberId}",
                        projectId, projectManagerMemberId)
                        .header("Authorization", "Bearer " + pmToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You cannot remove yourself from the project"));
    }

    @Test
    void removeMember_shouldAllowAdministratorToRemoveProjectManager() throws Exception {
        UsersRecord otherProjectManager = insertUser("member-api-removable-pm", SysRole.USER);
        insertMembership(projectId, otherProjectManager.getId(), ProjectRole.PM, ProjectMemberStatus.ACTIVE);

        mockMvc.perform(delete("/api/projects/{projectId}/members/{memberId}",
                        projectId, projectManagerMemberId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeMember_shouldReturn204WhenAdministratorRemovesLastProjectManager() throws Exception {
        mockMvc.perform(delete("/api/projects/{projectId}/members/{memberId}",
                        projectId, projectManagerMemberId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        ProjectMemberStatus memberStatus = dsl.select(PROJECT_MEMBERS.STATUS)
                .from(PROJECT_MEMBERS)
                .where(PROJECT_MEMBERS.ID.eq(projectManagerMemberId))
                .fetchOne(PROJECT_MEMBERS.STATUS);
        org.assertj.core.api.Assertions.assertThat(memberStatus)
                .isEqualTo(ProjectMemberStatus.INACTIVE);
    }

    @Test
    void removeMember_shouldReturn409WhenMemberHasAssignedTasks() throws Exception {
        insertTask(developer.getId());

        mockMvc.perform(delete("/api/projects/{projectId}/members/{memberId}",
                        projectId, developerMemberId)
                        .header("Authorization", "Bearer " + pmToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Cannot remove a member who is assigned to tasks. "
                                + "Transfer or unassign the tasks first"));
    }

    @Test
    void removeMember_shouldReturn403WhenAdministratorRemovesAdministrator() throws Exception {
        UsersRecord otherAdmin = insertUser("member-api-other-admin", SysRole.ADMIN);
        UUID otherAdminMemberId = insertMembership(
                projectId, otherAdmin.getId(), ProjectRole.PM, ProjectMemberStatus.ACTIVE);

        mockMvc.perform(delete("/api/projects/{projectId}/members/{memberId}",
                        projectId, otherAdminMemberId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value("An administrator cannot be removed from a project"));
    }

    @Test
    void removeMember_shouldReturn404ForMemberFromAnotherProject() throws Exception {
        UUID otherProjectId = insertProject("OTHERAPI");

        mockMvc.perform(delete("/api/projects/{projectId}/members/{memberId}",
                        otherProjectId, developerMemberId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    private String token(UsersRecord user) {
        return jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRole().getLiteral());
    }

    private UsersRecord insertUser(String marker, SysRole role) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return dsl.insertInto(USERS)
                .set(USERS.EMPLOYEE_ID, marker.toUpperCase() + "-" + suffix)
                .set(USERS.USERNAME, marker + "-" + suffix)
                .set(USERS.FULL_NAME, marker)
                .set(USERS.EMAIL, marker + "-" + suffix + "@test.com")
                .set(USERS.PASSWORD, "$2a$10$dummyhash")
                .set(USERS.ROLE, role)
                .set(USERS.STATUS, UserStatus.ACTIVE)
                .returning()
                .fetchOne();
    }

    private UUID insertProject(String marker) {
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return dsl.insertInto(PROJECTS)
                .set(PROJECTS.PROJECT_CODE, marker + "-" + suffix)
                .set(PROJECTS.PROJECT_NAME, marker + " Project")
                .set(PROJECTS.START_DATE, LocalDate.of(2026, 7, 1))
                .set(PROJECTS.END_DATE, LocalDate.of(2026, 12, 31))
                .set(PROJECTS.STATUS, ProjectStatus.ACTIVE)
                .returning(PROJECTS.ID)
                .fetchOne(PROJECTS.ID);
    }

    private UUID insertMembership(
            UUID targetProjectId, UUID userId, ProjectRole role, ProjectMemberStatus status) {
        return dsl.insertInto(PROJECT_MEMBERS)
                .set(PROJECT_MEMBERS.PROJECT_ID, targetProjectId)
                .set(PROJECT_MEMBERS.USER_ID, userId)
                .set(PROJECT_MEMBERS.PROJECT_ROLE, role)
                .set(PROJECT_MEMBERS.STATUS, status)
                .returning(PROJECT_MEMBERS.ID)
                .fetchOne(PROJECT_MEMBERS.ID);
    }

    private void insertTask(UUID assigneeId) {
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        dsl.insertInto(TASKS)
                .set(TASKS.TASK_KEY, "MEMAPI-" + suffix)
                .set(TASKS.PROJECT_ID, projectId)
                .set(TASKS.SUMMARY, "Member assignment API test task")
                .set(TASKS.TASK_TYPE, TaskType.TASK)
                .set(TASKS.PRIORITY, TaskPriority.MEDIUM)
                .set(TASKS.ASSIGNEE_ID, assigneeId)
                .set(TASKS.REPORTER_ID, projectManager.getId())
                .set(TASKS.CREATED_BY, projectManager.getId())
                .execute();
    }
}
