package fpt.qn.pms.project;

import static fpt.qn.pms.jooq.Tables.PROJECT_MEMBERS;
import static fpt.qn.pms.jooq.Tables.PROJECTS;
import static fpt.qn.pms.jooq.Tables.USERS;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import tools.jackson.databind.ObjectMapper;

import fpt.qn.pms.BaseIntegrationTest;
import fpt.qn.pms.jooq.enums.ProjectMemberStatus;
import fpt.qn.pms.jooq.enums.ProjectRole;
import fpt.qn.pms.jooq.enums.ProjectStatus;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.project.dto.request.CreateProjectRequest;
import fpt.qn.pms.project.dto.request.UpdateProjectRequest;
import fpt.qn.pms.security.JwtTokenProvider;

class ProjectControllerIntegrationTest extends BaseIntegrationTest {

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
    UsersRecord user;
    String adminToken;
    String userToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        admin = insertUser("project-api-admin", SysRole.ADMIN);
        user = insertUser("project-api-user", SysRole.USER);
        adminToken = jwtTokenProvider.generateAccessToken(admin.getUsername(), SysRole.ADMIN.getLiteral());
        userToken = jwtTokenProvider.generateAccessToken(user.getUsername(), SysRole.USER.getLiteral());
    }

    @Test
    void createProject_shouldReturn201ForAdministrator() throws Exception {
        CreateProjectRequest request = createRequest("APIWEB", "API Web Project");

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectCode").value("APIWEB"));
    }

    @Test
    void createProject_shouldReturn403ForRegularUser() throws Exception {
        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest("DENIED", "Denied"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProjects_shouldReturnOnlyActiveMembershipsForRegularUser() throws Exception {
        UUID visibleProjectId = insertProject("VISIBLE", "Visible Project");
        UUID hiddenProjectId = insertProject("HIDDEN", "Hidden Project");
        insertMembership(visibleProjectId, user.getId(), ProjectMemberStatus.ACTIVE);
        insertMembership(hiddenProjectId, user.getId(), ProjectMemberStatus.INACTIVE);

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(visibleProjectId.toString()));
    }

    @Test
    void getProjectById_shouldReturn403ForNonMember() throws Exception {
        UUID projectId = insertProject("PRIVATE", "Private Project");

        mockMvc.perform(get("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateProject_shouldUpdateFieldsAndKeepCode() throws Exception {
        UUID projectId = insertProject("IMMUTABLE", "Old Name");
        UpdateProjectRequest request = UpdateProjectRequest.builder()
                .projectName("Updated Name")
                .description("Updated")
                .startDate(LocalDate.of(2026, 7, 1))
                .endDate(LocalDate.of(2027, 1, 31))
                .status(ProjectStatus.ACTIVE)
                .build();

        mockMvc.perform(put("/api/projects/" + projectId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.projectCode").value("IMMUTABLE"))
                .andExpect(jsonPath("$.data.projectName").value("Updated Name"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    private CreateProjectRequest createRequest(String code, String name) {
        return CreateProjectRequest.builder()
                .projectCode(code)
                .projectName(name)
                .description("Description")
                .startDate(LocalDate.of(2026, 7, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .status(ProjectStatus.PLANNING)
                .build();
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

    private UUID insertProject(String code, String name) {
        return dsl.insertInto(PROJECTS)
                .set(PROJECTS.PROJECT_CODE, code)
                .set(PROJECTS.PROJECT_NAME, name)
                .set(PROJECTS.START_DATE, LocalDate.of(2026, 7, 1))
                .set(PROJECTS.END_DATE, LocalDate.of(2026, 12, 31))
                .set(PROJECTS.STATUS, ProjectStatus.PLANNING)
                .set(PROJECTS.CREATED_BY, admin.getId())
                .set(PROJECTS.UPDATED_BY, admin.getId())
                .returning(PROJECTS.ID)
                .fetchOne(PROJECTS.ID);
    }

    private void insertMembership(UUID projectId, UUID userId, ProjectMemberStatus status) {
        dsl.insertInto(PROJECT_MEMBERS)
                .set(PROJECT_MEMBERS.PROJECT_ID, projectId)
                .set(PROJECT_MEMBERS.USER_ID, userId)
                .set(PROJECT_MEMBERS.PROJECT_ROLE, ProjectRole.DEV)
                .set(PROJECT_MEMBERS.STATUS, status)
                .execute();
    }
}
