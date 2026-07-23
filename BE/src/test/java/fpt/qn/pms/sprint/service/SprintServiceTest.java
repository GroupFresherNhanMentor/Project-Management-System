package fpt.qn.pms.sprint.service;

import static fpt.qn.pms.jooq.Tables.PROJECTS;
import static fpt.qn.pms.jooq.Tables.USERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.UUID;

import java.util.List;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import fpt.qn.pms.security.UserPrincipal;

import fpt.qn.pms.BaseIntegrationTest;
import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.common.exception.AppException;
import fpt.qn.pms.jooq.enums.ProjectStatus;
import fpt.qn.pms.jooq.enums.SprintStatus;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.ProjectsRecord;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.sprint.dto.CreateSprintRequest;
import fpt.qn.pms.sprint.dto.SprintDto;
import fpt.qn.pms.sprint.dto.UpdateSprintRequest;
import fpt.qn.pms.sprint.dto.UpdateSprintStatusRequest;

class SprintServiceTest extends BaseIntegrationTest {

    @Autowired
    SprintService sprintService;

    @Autowired
    DSLContext dsl;

    UUID projectId;
    UsersRecord savedUser;

    @BeforeEach
    void setUp() {
        savedUser = new UsersRecord();
        savedUser.setEmployeeId("SPRINT_EMP");
        savedUser.setUsername("sprint_tester");
        savedUser.setFullName("Sprint Tester");
        savedUser.setEmail("sprint_tester@test.com");
        savedUser.setPassword("$2a$10$dummyhash");
        savedUser.setRole(SysRole.ADMIN);
        savedUser.setStatus(UserStatus.ACTIVE);
        savedUser = dsl.insertInto(USERS)
                .set(savedUser)
                .returning()
                .fetchOne();

        UserPrincipal principal = UserPrincipal.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .password(null)
                .enabled(true)
                .authorities(List.of(new SimpleGrantedAuthority(SysRole.ADMIN.getLiteral())))
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        ProjectsRecord project = new ProjectsRecord();
        project.setProjectCode("SPRINT-TEST");
        project.setProjectName("Sprint Test Project");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setStartDate(LocalDate.of(2026, 7, 1));
        project.setEndDate(LocalDate.of(2026, 12, 31));
        project.setCreatedBy(savedUser.getId());
        ProjectsRecord savedProject = dsl.insertInto(PROJECTS)
                .set(project)
                .returning()
                .fetchOne();

        projectId = savedProject.getId();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createSprint_shouldReturnMappedDto() {
        CreateSprintRequest req = buildCreateRequest("Sprint 1");

        SprintDto dto = sprintService.createSprint(req);

        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getProjectId()).isEqualTo(projectId);
        assertThat(dto.getSprintName()).isEqualTo("Sprint 1");
        assertThat(dto.getGoal()).isEqualTo("Goal 1");
        assertThat(dto.getStartDate()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(dto.getEndDate()).isEqualTo(LocalDate.of(2026, 7, 15));
        assertThat(dto.getStatus()).isEqualTo("PLANNED");
        assertThat(dto.getCreatedAt()).isNotNull();
    }

    @Test
    void createSprint_shouldDefaultStatusToPlanned() {
        CreateSprintRequest req = buildCreateRequest("Sprint 2");

        SprintDto dto = sprintService.createSprint(req);

        assertThat(dto.getStatus()).isEqualTo("PLANNED");
    }

    @Test
    void createSprint_shouldThrow_whenEndDateBeforeStartDate() {
        CreateSprintRequest req = buildCreateRequest("Sprint 3");
        req.setStartDate(LocalDate.of(2026, 7, 15));
        req.setEndDate(LocalDate.of(2026, 7, 1));

        assertThatThrownBy(() -> sprintService.createSprint(req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("end_date");
    }

    @Test
    void createSprint_shouldAllowSameStartAndEndDate() {
        CreateSprintRequest req = buildCreateRequest("Sprint 4");
        req.setStartDate(LocalDate.of(2026, 7, 1));
        req.setEndDate(LocalDate.of(2026, 7, 1));

        SprintDto dto = sprintService.createSprint(req);

        assertThat(dto.getId()).isNotNull();
    }

    @Test
    void getSprintById_shouldReturnDto_whenExists() {
        SprintDto created = sprintService.createSprint(buildCreateRequest("Sprint 5"));

        SprintDto found = sprintService.getSprintById(projectId, created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getSprintName()).isEqualTo("Sprint 5");
    }

    @Test
    void getSprintById_shouldThrow_whenNotExists() {
        assertThatThrownBy(() -> sprintService.getSprintById(projectId, UUID.randomUUID()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Sprint not found");
    }

    @Test
    void getSprintById_shouldThrow_whenWrongProjectId() {
        SprintDto created = sprintService.createSprint(buildCreateRequest("Sprint 5b"));

        assertThatThrownBy(() -> sprintService.getSprintById(UUID.randomUUID(), created.getId()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Sprint not found");
    }

    @Test
    void getSprintsByProject_shouldReturnPageResponse() {
        sprintService.createSprint(buildCreateRequest("Sprint 6"));
        sprintService.createSprint(buildCreateRequest("Sprint 7"));

        PageResponse<SprintDto> page = sprintService.getSprintsByProject(projectId, null, null, 0, 10);

        assertThat(page.getItems()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
        assertThat(page.getPageNumber()).isZero();
        assertThat(page.getPageSize()).isEqualTo(10);
        assertThat(page.getTotalPages()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void getSprintsByProject_shouldRespectPagination() {
        sprintService.createSprint(buildCreateRequest("Sprint 8"));
        sprintService.createSprint(buildCreateRequest("Sprint 9"));
        sprintService.createSprint(buildCreateRequest("Sprint 10"));

        PageResponse<SprintDto> page = sprintService.getSprintsByProject(projectId, null, null, 0, 2);

        assertThat(page.getItems()).hasSize(2);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(3);
        assertThat(page.getTotalPages()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void getSprintsByProject_shouldReturnEmpty_whenNoSprints() {
        PageResponse<SprintDto> page = sprintService.getSprintsByProject(projectId, null, null, 0, 10);
        assertThat(page.getItems()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }

    @Test
    void updateSprint_shouldUpdateFields() {
        SprintDto created = sprintService.createSprint(buildCreateRequest("Sprint 11"));

        UpdateSprintRequest req = new UpdateSprintRequest();
        req.setSprintName("Updated Sprint");
        req.setGoal("Updated goal");

        SprintDto updated = sprintService.updateSprint(projectId, created.getId(), req);

        assertThat(updated.getSprintName()).isEqualTo("Updated Sprint");
        assertThat(updated.getGoal()).isEqualTo("Updated goal");
        assertThat(updated.getStartDate()).isEqualTo(created.getStartDate());
        assertThat(updated.getEndDate()).isEqualTo(created.getEndDate());
    }

    @Test
    void updateSprint_shouldIgnoreNullFields() {
        SprintDto created = sprintService.createSprint(buildCreateRequest("Sprint 12"));

        UpdateSprintRequest req = new UpdateSprintRequest();

        SprintDto updated = sprintService.updateSprint(projectId, created.getId(), req);

        assertThat(updated.getSprintName()).isEqualTo("Sprint 12");
        assertThat(updated.getGoal()).isEqualTo("Goal 12");
    }

    @Test
    void updateSprint_shouldThrow_whenNotExists() {
        UpdateSprintRequest req = new UpdateSprintRequest();
        req.setSprintName("Whatever");

        assertThatThrownBy(() -> sprintService.updateSprint(projectId, UUID.randomUUID(), req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Sprint not found");
    }

    @Test
    void updateSprintStatus_shouldTransitionPlannedToActive() {
        SprintDto created = sprintService.createSprint(buildCreateRequest("Sprint 13"));

        UpdateSprintStatusRequest req = new UpdateSprintStatusRequest();
        req.setStatus(SprintStatus.ACTIVE);

        SprintDto updated = sprintService.updateSprintStatus(projectId, created.getId(), req);

        assertThat(updated.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void updateSprintStatus_shouldTransitionActiveToClosed() {
        SprintDto created = sprintService.createSprint(buildCreateRequest("Sprint 14"));
        UpdateSprintStatusRequest activateReq = new UpdateSprintStatusRequest();
        activateReq.setStatus(SprintStatus.ACTIVE);
        sprintService.updateSprintStatus(projectId, created.getId(), activateReq);

        UpdateSprintStatusRequest closeReq = new UpdateSprintStatusRequest();
        closeReq.setStatus(SprintStatus.CLOSED);

        SprintDto updated = sprintService.updateSprintStatus(projectId, created.getId(), closeReq);

        assertThat(updated.getStatus()).isEqualTo("CLOSED");
    }

    @Test
    void updateSprintStatus_shouldThrow_whenInvalidTransitionPlannedToClosed() {
        SprintDto created = sprintService.createSprint(buildCreateRequest("Sprint 15"));

        UpdateSprintStatusRequest req = new UpdateSprintStatusRequest();
        req.setStatus(SprintStatus.CLOSED);

        assertThatThrownBy(() -> sprintService.updateSprintStatus(projectId, created.getId(), req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void updateSprintStatus_shouldThrow_whenClosedToActive() {
        SprintDto created = sprintService.createSprint(buildCreateRequest("Sprint 16"));
        UpdateSprintStatusRequest activateReq = new UpdateSprintStatusRequest();
        activateReq.setStatus(SprintStatus.ACTIVE);
        sprintService.updateSprintStatus(projectId, created.getId(), activateReq);
        UpdateSprintStatusRequest closeReq = new UpdateSprintStatusRequest();
        closeReq.setStatus(SprintStatus.CLOSED);
        sprintService.updateSprintStatus(projectId, created.getId(), closeReq);

        UpdateSprintStatusRequest reactivateReq = new UpdateSprintStatusRequest();
        reactivateReq.setStatus(SprintStatus.ACTIVE);

        assertThatThrownBy(() -> sprintService.updateSprintStatus(projectId, created.getId(), reactivateReq))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void updateSprintStatus_shouldThrow_whenNotExists() {
        UpdateSprintStatusRequest req = new UpdateSprintStatusRequest();
        req.setStatus(SprintStatus.ACTIVE);

        assertThatThrownBy(() -> sprintService.updateSprintStatus(projectId, UUID.randomUUID(), req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Sprint not found");
    }

    @Test
    void updateSprintStatus_shouldThrow_whenMultipleActive() {
        SprintDto sprint1 = sprintService.createSprint(buildCreateRequest("Sprint 17a"));
        UpdateSprintStatusRequest activate1 = new UpdateSprintStatusRequest();
        activate1.setStatus(SprintStatus.ACTIVE);
        sprintService.updateSprintStatus(projectId, sprint1.getId(), activate1);

        SprintDto sprint2 = sprintService.createSprint(buildCreateRequest("Sprint 17b"));

        UpdateSprintStatusRequest activate2 = new UpdateSprintStatusRequest();
        activate2.setStatus(SprintStatus.ACTIVE);

        assertThatThrownBy(() -> sprintService.updateSprintStatus(projectId, sprint2.getId(), activate2))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("active sprint");
    }

    private CreateSprintRequest buildCreateRequest(String name) {
        CreateSprintRequest req = new CreateSprintRequest();
        req.setProjectId(projectId);
        req.setSprintName(name);
        req.setGoal("Goal " + name.replaceAll(".*?(\\d+)$", "$1"));
        req.setStartDate(LocalDate.of(2026, 7, 1));
        req.setEndDate(LocalDate.of(2026, 7, 15));
        return req;
    }
}
