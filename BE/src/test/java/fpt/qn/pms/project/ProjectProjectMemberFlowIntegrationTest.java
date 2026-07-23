package fpt.qn.pms.project;

import static fpt.qn.pms.jooq.Tables.PROJECT_MEMBERS;
import static fpt.qn.pms.jooq.Tables.USERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import fpt.qn.pms.BaseIntegrationTest;
import fpt.qn.pms.jooq.enums.ProjectRole;
import fpt.qn.pms.jooq.enums.ProjectStatus;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.project.dto.request.CreateProjectRequest;
import fpt.qn.pms.projectmember.dto.request.AddProjectMemberRequest;
import fpt.qn.pms.security.JwtTokenProvider;
import tools.jackson.databind.ObjectMapper;

class ProjectProjectMemberFlowIntegrationTest extends BaseIntegrationTest {

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
    String adminToken;
    String projectManagerToken;
    String developerToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        admin = insertUser("flow-admin", SysRole.ADMIN);
        projectManager = insertUser("flow-pm", SysRole.USER);
        developer = insertUser("flow-dev", SysRole.USER);
        adminToken = token(admin);
        projectManagerToken = token(projectManager);
        developerToken = token(developer);
    }

    @Test
    void projectAndMemberFlow_shouldAllowNoPmOnCreateAndRestrictViewsToAdminOrPm()
            throws Exception {
        String projectCode = "FLOW-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        CreateProjectRequest createRequest = CreateProjectRequest.builder()
                .projectCode(projectCode)
                .projectName("Project member integration flow")
                .description("Created without a project manager")
                .startDate(LocalDate.of(2026, 7, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .status(ProjectStatus.PLANNING)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID projectId = UUID.fromString(objectMapper.readTree(
                createResult.getResponse().getContentAsString()).get("data").get("id").asText());
        assertThat(dsl.fetchCount(
                PROJECT_MEMBERS, PROJECT_MEMBERS.PROJECT_ID.eq(projectId))).isZero();

        addMember(projectId, projectManager.getId(), ProjectRole.PM);
        addMember(projectId, developer.getId(), ProjectRole.DEV);

        mockMvc.perform(get("/api/projects")
                        .param("keyword", projectCode)
                        .header("Authorization", "Bearer " + projectManagerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(projectId.toString()));

        mockMvc.perform(get("/api/projects/{projectId}", projectId)
                        .header("Authorization", "Bearer " + projectManagerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(projectId.toString()));

        mockMvc.perform(get("/api/projects/{projectId}/members", projectId)
                        .header("Authorization", "Bearer " + projectManagerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2));

        mockMvc.perform(get("/api/projects")
                        .param("keyword", projectCode)
                        .header("Authorization", "Bearer " + developerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));

        mockMvc.perform(get("/api/projects/{projectId}", projectId)
                        .header("Authorization", "Bearer " + developerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(projectId.toString()));

        mockMvc.perform(get("/api/projects/{projectId}/members", projectId)
                        .header("Authorization", "Bearer " + developerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2));

        mockMvc.perform(get("/api/projects/{projectId}", projectId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    private void addMember(UUID projectId, UUID userId, ProjectRole projectRole) throws Exception {
        AddProjectMemberRequest request = AddProjectMemberRequest.builder()
                .userId(userId)
                .projectRole(projectRole)
                .build();

        mockMvc.perform(post("/api/projects/{projectId}/members", projectId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
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
}
