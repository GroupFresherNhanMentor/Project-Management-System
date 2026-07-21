package fpt.qn.pms.project.service;

import static fpt.qn.pms.jooq.Tables.PROJECT_MEMBERS;
import static fpt.qn.pms.jooq.Tables.USERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import fpt.qn.pms.BaseIntegrationTest;
import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.jooq.enums.ProjectMemberStatus;
import fpt.qn.pms.jooq.enums.ProjectRole;
import fpt.qn.pms.jooq.enums.ProjectStatus;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.project.dto.request.CreateProjectRequest;
import fpt.qn.pms.project.dto.request.UpdateProjectRequest;
import fpt.qn.pms.project.dto.response.ProjectDto;
import fpt.qn.pms.project.exception.InvalidProjectDateRangeException;
import fpt.qn.pms.project.exception.ProjectAccessDeniedException;
import fpt.qn.pms.project.exception.ProjectCodeAlreadyExistsException;

class ProjectServiceTest extends BaseIntegrationTest {

    @Autowired
    ProjectService projectService;

    @Autowired
    DSLContext dsl;

    UsersRecord admin;
    UsersRecord regularUser;
    String codeSuffix;

    @BeforeEach
    void setUp() {
        codeSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        admin = insertUser("project-admin", SysRole.ADMIN);
        regularUser = insertUser("project-member", SysRole.USER);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createProject_shouldNormalizeCodeAndSetAuditUser() {
        authenticate(admin.getUsername());

        ProjectDto created = projectService.createProject(
                createRequest(" web" + codeSuffix.toLowerCase() + " ", "Web Project"));

        assertThat(created.getProjectCode()).isEqualTo("WEB" + codeSuffix);
        assertThat(created.getStatus()).isEqualTo("PLANNING");
        assertThat(created.getCreatedAt()).isNotNull();
    }

    @Test
    void createProject_shouldRejectDuplicateCodeIgnoringCase() {
        authenticate(admin.getUsername());
        String code = "WEB" + codeSuffix;
        projectService.createProject(createRequest(code, "First"));

        assertThatThrownBy(() -> projectService.createProject(createRequest(code.toLowerCase(), "Second")))
                .isInstanceOf(ProjectCodeAlreadyExistsException.class);
    }

    @Test
    void createProject_shouldRejectInvalidDateRange() {
        authenticate(admin.getUsername());
        CreateProjectRequest request = createRequest("DATE" + codeSuffix, "Invalid Dates");
        request.setStartDate(LocalDate.of(2026, 8, 1));
        request.setEndDate(LocalDate.of(2026, 7, 1));

        assertThatThrownBy(() -> projectService.createProject(request))
                .isInstanceOf(InvalidProjectDateRangeException.class);
    }

    @Test
    void getProjects_shouldReturnOnlyActiveMembershipsForUser() {
        authenticate(admin.getUsername());
        ProjectDto visible = projectService.createProject(createRequest("VISIBLE" + codeSuffix, "Visible"));
        ProjectDto hidden = projectService.createProject(createRequest("HIDDEN" + codeSuffix, "Hidden"));
        insertMembership(visible.getId(), regularUser.getId(), ProjectMemberStatus.ACTIVE);
        insertMembership(hidden.getId(), regularUser.getId(), ProjectMemberStatus.INACTIVE);

        authenticate(regularUser.getUsername());
        PageResponse<ProjectDto> page = projectService.getProjects(null, null, 0, 20);

        assertThat(page.getItems()).extracting(ProjectDto::getId)
                .contains(visible.getId())
                .doesNotContain(hidden.getId());
    }

    @Test
    void getProjectById_shouldRejectUserWithoutActiveMembership() {
        authenticate(admin.getUsername());
        ProjectDto project = projectService.createProject(createRequest("PRIVATE" + codeSuffix, "Private"));

        authenticate(regularUser.getUsername());
        assertThatThrownBy(() -> projectService.getProjectById(project.getId()))
                .isInstanceOf(ProjectAccessDeniedException.class);
    }

    @Test
    void updateProject_shouldKeepCodeImmutable() {
        authenticate(admin.getUsername());
        String projectCode = "KEEP" + codeSuffix;
        ProjectDto project = projectService.createProject(createRequest(projectCode, "Old Name"));
        UpdateProjectRequest request = UpdateProjectRequest.builder()
                .projectName("New Name")
                .description("Updated")
                .startDate(LocalDate.of(2026, 7, 1))
                .endDate(LocalDate.of(2027, 1, 31))
                .status(ProjectStatus.ACTIVE)
                .build();

        ProjectDto updated = projectService.updateProject(project.getId(), request);

        assertThat(updated.getProjectCode()).isEqualTo(projectCode);
        assertThat(updated.getProjectName()).isEqualTo("New Name");
        assertThat(updated.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void getProjects_shouldRejectMissingAuthenticationWithoutMockFallback() {
        assertThatThrownBy(() -> projectService.getProjects(null, null, 0, 20))
                .isInstanceOf(ProjectAccessDeniedException.class);
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

    private void insertMembership(UUID projectId, UUID userId, ProjectMemberStatus status) {
        dsl.insertInto(PROJECT_MEMBERS)
                .set(PROJECT_MEMBERS.PROJECT_ID, projectId)
                .set(PROJECT_MEMBERS.USER_ID, userId)
                .set(PROJECT_MEMBERS.PROJECT_ROLE, ProjectRole.DEV)
                .set(PROJECT_MEMBERS.STATUS, status)
                .execute();
    }

    private void authenticate(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, List.of()));
    }
}
