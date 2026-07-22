package fpt.qn.pms.task.service;

import static fpt.qn.pms.jooq.Tables.PROJECT_MEMBERS;
import static fpt.qn.pms.jooq.Tables.PROJECTS;
import static fpt.qn.pms.jooq.Tables.TASK_STATUSES;
import static fpt.qn.pms.jooq.Tables.TASK_WORKFLOW;
import static fpt.qn.pms.jooq.Tables.USERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.jooq.DSLContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import fpt.qn.pms.BaseIntegrationTest;
import fpt.qn.pms.jooq.enums.ProjectMemberStatus;
import fpt.qn.pms.jooq.enums.ProjectRole;
import fpt.qn.pms.jooq.enums.ProjectStatus;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.ProjectMembersRecord;
import fpt.qn.pms.jooq.tables.records.ProjectsRecord;
import fpt.qn.pms.jooq.tables.records.TaskStatusesRecord;
import fpt.qn.pms.jooq.tables.records.TaskWorkflowRecord;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.task.dto.CreateTaskWorkflowRequest;
import fpt.qn.pms.task.dto.TaskWorkflowDto;
import fpt.qn.pms.task.exception.TaskStatusNotFoundException;
import fpt.qn.pms.task.exception.TaskWorkflowAlreadyExistsException;
import fpt.qn.pms.task.exception.TaskWorkflowNotFoundException;
import fpt.qn.pms.task.exception.TaskWorkflowSelfLoopException;

class TaskWorkflowServiceTest extends BaseIntegrationTest {

    @Autowired
    TaskWorkflowService taskWorkflowService;

    @Autowired
    DSLContext dsl;

    UUID userId;
    UUID projectId;
    UUID statusA;
    UUID statusB;
    UUID statusC;

    @BeforeEach
    void setUp() {
        UsersRecord user = new UsersRecord();
        user.setEmployeeId("WF_EMP");
        user.setUsername("wf_tester");
        user.setFullName("Workflow Tester");
        user.setEmail("wf_tester@test.com");
        user.setPassword("$2a$10$dummyhash");
        user.setRole(SysRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        UsersRecord savedUser = dsl.insertInto(USERS).set(user).returning().fetchOne();
        userId = savedUser.getId();

        ProjectsRecord project = new ProjectsRecord();
        project.setProjectCode("WF-TEST");
        project.setProjectName("Workflow Test Project");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setStartDate(LocalDate.of(2026, 1, 1));
        project.setEndDate(LocalDate.of(2026, 12, 31));
        project.setCreatedBy(savedUser.getId());
        ProjectsRecord savedProject = dsl.insertInto(PROJECTS).set(project).returning().fetchOne();
        projectId = savedProject.getId();

        ProjectMembersRecord member = new ProjectMembersRecord();
        member.setProjectId(projectId);
        member.setUserId(savedUser.getId());
        member.setProjectRole(ProjectRole.PM);
        member.setStatus(ProjectMemberStatus.ACTIVE);
        dsl.insertInto(PROJECT_MEMBERS).set(member).execute();

        statusA = insertStatus("Todo");
        statusB = insertStatus("In Progress");
        statusC = insertStatus("Done");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(savedUser.getUsername(), null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private UUID insertStatus(String name) {
        TaskStatusesRecord r = new TaskStatusesRecord();
        r.setProjectId(projectId);
        r.setName(name);
        r.setColor("#6B7280");
        r.setIsInitial(false);
        r.setIsFinal(false);
        r.setIsActive(true);
        return dsl.insertInto(TASK_STATUSES).set(r).returning().fetchOne().getId();
    }

    private UUID insertTransition(UUID from, UUID to) {
        TaskWorkflowRecord r = new TaskWorkflowRecord();
        r.setFromStatusId(from);
        r.setToStatusId(to);
        return dsl.insertInto(TASK_WORKFLOW).set(r).returning().fetchOne().getId();
    }

    private CreateTaskWorkflowRequest req(UUID from, UUID to) {
        CreateTaskWorkflowRequest r = new CreateTaskWorkflowRequest();
        r.setFromStatusId(from);
        r.setToStatusId(to);
        return r;
    }

    // ── 1. getByProject ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getByProject - returns empty list when no transitions exist")
    void getByProject_empty() {
        List<TaskWorkflowDto> result = taskWorkflowService.getByProject(projectId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getByProject - returns all transitions belonging to the project")
    void getByProject_returnsTransitions() {
        insertTransition(statusA, statusB);
        insertTransition(statusB, statusC);

        List<TaskWorkflowDto> result = taskWorkflowService.getByProject(projectId);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(TaskWorkflowDto::getFromStatusId)
                .containsExactlyInAnyOrder(statusA, statusB);
    }

    @Test
    @DisplayName("getByProject - does not return transitions from another project")
    void getByProject_isolatesAcrossProjects() {
        // Create a second project with its own statuses and transition
        ProjectsRecord other = new ProjectsRecord();
        other.setProjectCode("OTHER");
        other.setProjectName("Other Project");
        other.setStatus(ProjectStatus.ACTIVE);
        other.setStartDate(LocalDate.of(2026, 1, 1));
        other.setEndDate(LocalDate.of(2026, 12, 31));
        other.setCreatedBy(dsl.selectFrom(USERS).limit(1).fetchOne().getId());
        UUID otherProjectId = dsl.insertInto(PROJECTS).set(other).returning().fetchOne().getId();

        TaskStatusesRecord s1 = new TaskStatusesRecord();
        s1.setProjectId(otherProjectId);
        s1.setName("X");
        s1.setColor("#000000");
        s1.setIsInitial(false);
        s1.setIsFinal(false);
        s1.setIsActive(true);
        UUID otherFrom = dsl.insertInto(TASK_STATUSES).set(s1).returning().fetchOne().getId();

        TaskStatusesRecord s2 = new TaskStatusesRecord();
        s2.setProjectId(otherProjectId);
        s2.setName("Y");
        s2.setColor("#000000");
        s2.setIsInitial(false);
        s2.setIsFinal(false);
        s2.setIsActive(true);
        UUID otherTo = dsl.insertInto(TASK_STATUSES).set(s2).returning().fetchOne().getId();

        insertTransition(otherFrom, otherTo);
        insertTransition(statusA, statusB); // belongs to main project

        List<TaskWorkflowDto> result = taskWorkflowService.getByProject(projectId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFromStatusId()).isEqualTo(statusA);
    }

    // ── 2. createAll ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("createAll - single transition created and returned with generated id")
    void createAll_single_success() {
        List<TaskWorkflowDto> result = taskWorkflowService.createAll(projectId, List.of(req(statusA, statusB)));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isNotNull();
        assertThat(result.get(0).getFromStatusId()).isEqualTo(statusA);
        assertThat(result.get(0).getToStatusId()).isEqualTo(statusB);
        assertThat(result.get(0).getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("createAll - multiple transitions inserted in one call")
    void createAll_bulk_success() {
        List<TaskWorkflowDto> result = taskWorkflowService.createAll(projectId,
                List.of(req(statusA, statusB), req(statusB, statusC), req(statusA, statusC)));

        assertThat(result).hasSize(3);
        assertThat(result).extracting(TaskWorkflowDto::getId).doesNotContainNull();
        assertThat(result).extracting(TaskWorkflowDto::getFromStatusId)
                .containsExactlyInAnyOrder(statusA, statusB, statusA);
    }

    @Test
    @DisplayName("createAll - throws TaskWorkflowSelfLoopException when from equals to")
    void createAll_selfLoop_throws() {
        assertThatThrownBy(() -> taskWorkflowService.createAll(projectId, List.of(req(statusA, statusA))))
                .isInstanceOf(TaskWorkflowSelfLoopException.class);
    }

    @Test
    @DisplayName("createAll - throws TaskStatusNotFoundException when fromStatusId not in project")
    void createAll_fromStatusNotInProject_throws() {
        UUID foreignStatus = UUID.randomUUID();

        assertThatThrownBy(() -> taskWorkflowService.createAll(projectId, List.of(req(foreignStatus, statusB))))
                .isInstanceOf(TaskStatusNotFoundException.class)
                .hasMessageContaining(foreignStatus.toString());
    }

    @Test
    @DisplayName("createAll - throws TaskStatusNotFoundException when toStatusId not in project")
    void createAll_toStatusNotInProject_throws() {
        UUID foreignStatus = UUID.randomUUID();

        assertThatThrownBy(() -> taskWorkflowService.createAll(projectId, List.of(req(statusA, foreignStatus))))
                .isInstanceOf(TaskStatusNotFoundException.class)
                .hasMessageContaining(foreignStatus.toString());
    }

    @Test
    @DisplayName("createAll - throws TaskWorkflowAlreadyExistsException when transition already exists in DB")
    void createAll_duplicateInDb_throws() {
        insertTransition(statusA, statusB);

        assertThatThrownBy(() -> taskWorkflowService.createAll(projectId, List.of(req(statusA, statusB))))
                .isInstanceOf(TaskWorkflowAlreadyExistsException.class);
    }

    @Test
    @DisplayName("createAll - throws TaskWorkflowAlreadyExistsException when same pair appears twice in the batch")
    void createAll_duplicateWithinBatch_throws() {
        assertThatThrownBy(() -> taskWorkflowService.createAll(projectId,
                List.of(req(statusA, statusB), req(statusA, statusB))))
                .isInstanceOf(TaskWorkflowAlreadyExistsException.class);
    }

    @Test
    @DisplayName("createAll - entire batch is rolled back when a later item is invalid")
    void createAll_rollsBackOnValidationFailure() {
        UUID foreignStatus = UUID.randomUUID();

        assertThatThrownBy(() -> taskWorkflowService.createAll(projectId,
                List.of(req(statusA, statusB), req(statusA, foreignStatus))))
                .isInstanceOf(TaskStatusNotFoundException.class);

        long count = dsl.fetchCount(TASK_WORKFLOW,
                TASK_WORKFLOW.FROM_STATUS_ID.eq(statusA).and(TASK_WORKFLOW.TO_STATUS_ID.eq(statusB)));
        assertThat(count).isZero();
    }

    // ── 3. delete ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("delete - removes the transition from the database")
    void delete_success() {
        UUID transitionId = insertTransition(statusA, statusB);

        taskWorkflowService.delete(projectId, transitionId);

        boolean stillExists = dsl.fetchExists(TASK_WORKFLOW, TASK_WORKFLOW.ID.eq(transitionId));
        assertThat(stillExists).isFalse();
    }

    @Test
    @DisplayName("delete - throws TaskWorkflowNotFoundException when id does not exist")
    void delete_notFound_throws() {
        UUID nonExistent = UUID.randomUUID();

        assertThatThrownBy(() -> taskWorkflowService.delete(projectId, nonExistent))
                .isInstanceOf(TaskWorkflowNotFoundException.class)
                .hasMessageContaining(nonExistent.toString());
    }

    @Test
    @DisplayName("delete - throws TaskWorkflowNotFoundException when transition belongs to another project")
    void delete_wrongProject_throws() {
        UUID transitionId = insertTransition(statusA, statusB);

        // Create a second project and make the same PM a member, so the aspect passes
        ProjectsRecord other = new ProjectsRecord();
        other.setProjectCode("OTHER2");
        other.setProjectName("Other Project");
        other.setStatus(ProjectStatus.ACTIVE);
        other.setStartDate(LocalDate.of(2026, 1, 1));
        other.setEndDate(LocalDate.of(2026, 12, 31));
        other.setCreatedBy(userId);
        UUID otherProjectId = dsl.insertInto(PROJECTS).set(other).returning().fetchOne().getId();

        ProjectMembersRecord m = new ProjectMembersRecord();
        m.setProjectId(otherProjectId);
        m.setUserId(userId);
        m.setProjectRole(ProjectRole.PM);
        m.setStatus(ProjectMemberStatus.ACTIVE);
        dsl.insertInto(PROJECT_MEMBERS).set(m).execute();

        // Now delete passes the role check but the transition belongs to the main project
        assertThatThrownBy(() -> taskWorkflowService.delete(otherProjectId, transitionId))
                .isInstanceOf(TaskWorkflowNotFoundException.class)
                .hasMessageContaining(transitionId.toString());
    }
}
