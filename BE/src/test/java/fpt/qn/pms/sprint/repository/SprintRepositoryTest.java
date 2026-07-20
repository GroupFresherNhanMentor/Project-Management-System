package fpt.qn.pms.sprint.repository;

import static fpt.qn.pms.jooq.Tables.PROJECTS;
import static fpt.qn.pms.jooq.Tables.USERS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.jooq.enums.ProjectStatus;
import fpt.qn.pms.jooq.enums.SprintStatus;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.ProjectsRecord;
import fpt.qn.pms.jooq.tables.records.SprintsRecord;
import fpt.qn.pms.jooq.tables.records.UsersRecord;

@SpringBootTest
@Testcontainers
@Transactional
class SprintRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine");

    @Autowired
    SprintRepository sprintRepository;

    @Autowired
    DSLContext dsl;

    UUID projectId;

    @BeforeEach
    void setUp() {
        UsersRecord user = new UsersRecord();
        user.setEmployeeId("REPO_EMP");
        user.setUsername("repo_tester");
        user.setFullName("Repo Tester");
        user.setEmail("repo_tester@test.com");
        user.setPassword("$2a$10$dummyhash");
        user.setRole(SysRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        UsersRecord savedUser = dsl.insertInto(USERS)
                .set(user)
                .returning()
                .fetchOne();

        ProjectsRecord project = new ProjectsRecord();
        project.setProjectCode("REPO-TEST");
        project.setProjectName("Repo Test Project");
        project.setStatus(ProjectStatus.ACTIVE);
        project.setStartDate(java.time.LocalDate.of(2026, 7, 1));
        project.setEndDate(java.time.LocalDate.of(2026, 12, 31));
        project.setCreatedBy(savedUser.getId());
        ProjectsRecord savedProject = dsl.insertInto(PROJECTS)
                .set(project)
                .returning()
                .fetchOne();

        projectId = savedProject.getId();
    }

    @Test
    void create_shouldPersistAndReturnRecord() {
        SprintsRecord saved = sprintRepository.create(buildSprint("Sprint 001"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSprintName()).isEqualTo("Sprint 001");
        assertThat(saved.getProjectId()).isEqualTo(projectId);
        assertThat(saved.getStatus()).isEqualTo(SprintStatus.PLANNED);
    }

    @Test
    void findById_shouldReturnRecord_whenExists() {
        SprintsRecord created = sprintRepository.create(buildSprint("Sprint 002"));

        Optional<SprintsRecord> found = sprintRepository.findById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getSprintName()).isEqualTo("Sprint 002");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        assertThat(sprintRepository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findAll_shouldReturnSprintsByProject() {
        sprintRepository.create(buildSprint("Sprint 003"));
        sprintRepository.create(buildSprint("Sprint 004"));

        PaginationResult<SprintsRecord> result = sprintRepository.findAll(projectId, null, null, 0, 20);

        assertThat(result.getTotal()).isGreaterThanOrEqualTo(2);
        assertThat(result.getItems()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void findAll_shouldRespectPagination() {
        sprintRepository.create(buildSprint("Sprint 005"));
        sprintRepository.create(buildSprint("Sprint 006"));
        sprintRepository.create(buildSprint("Sprint 007"));

        PaginationResult<SprintsRecord> result = sprintRepository.findAll(projectId, null, null, 0, 2);

        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getTotal()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void findAll_shouldFilterByKeyword() {
        sprintRepository.create(buildSprint("UniqueSprintName"));

        PaginationResult<SprintsRecord> result = sprintRepository.findAll(projectId, "Unique", null, 0, 20);

        assertThat(result.getItems()).isNotEmpty();
        assertThat(result.getItems()).allMatch(r -> r.getSprintName().contains("Unique"));
    }

    @Test
    void findAll_shouldFilterByStatus() {
        SprintsRecord activeSprint = buildSprint("Sprint 008");
        activeSprint.setStatus(SprintStatus.ACTIVE);
        sprintRepository.create(activeSprint);

        PaginationResult<SprintsRecord> result = sprintRepository.findAll(projectId, null, SprintStatus.ACTIVE, 0, 20);

        assertThat(result.getItems()).isNotEmpty();
        assertThat(result.getItems()).allMatch(r -> r.getStatus() == SprintStatus.ACTIVE);
    }

    @Test
    void findAll_shouldReturnEmpty_whenNoProject() {
        PaginationResult<SprintsRecord> result = sprintRepository.findAll(UUID.randomUUID(), null, null, 0, 20);
        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotal()).isZero();
    }

    @Test
    void existsActiveByProjectId_shouldReturnTrue_whenExists() {
        SprintsRecord sprint = buildSprint("Sprint 009");
        sprint.setStatus(SprintStatus.ACTIVE);
        sprintRepository.create(sprint);

        assertThat(sprintRepository.existsActiveByProjectId(projectId)).isTrue();
    }

    @Test
    void existsActiveByProjectId_shouldReturnFalse_whenNoneActive() {
        sprintRepository.create(buildSprint("Sprint 010"));

        assertThat(sprintRepository.existsActiveByProjectId(projectId)).isFalse();
    }

    @Test
    void existsActiveByProjectId_shouldReturnFalse_whenNoProject() {
        assertThat(sprintRepository.existsActiveByProjectId(UUID.randomUUID())).isFalse();
    }

    @Test
    void update_shouldModifyFields() {
        SprintsRecord created = sprintRepository.create(buildSprint("Sprint 011"));

        SprintsRecord fetched = sprintRepository.findById(created.getId()).orElseThrow();
        fetched.setSprintName("Updated Sprint");
        fetched.setStatus(SprintStatus.ACTIVE);
        sprintRepository.update(fetched);

        SprintsRecord updated = sprintRepository.findById(created.getId()).orElseThrow();
        assertThat(updated.getSprintName()).isEqualTo("Updated Sprint");
        assertThat(updated.getStatus()).isEqualTo(SprintStatus.ACTIVE);
    }

    @Test
    void deleteById_shouldRemoveRecord() {
        SprintsRecord created = sprintRepository.create(buildSprint("Sprint 012"));

        sprintRepository.deleteById(created.getId());

        assertThat(sprintRepository.findById(created.getId())).isEmpty();
    }

    @Test
    void deleteById_shouldDoNothing_whenNotExists() {
        sprintRepository.deleteById(UUID.randomUUID());
    }

    private SprintsRecord buildSprint(String name) {
        SprintsRecord record = new SprintsRecord();
        record.setProjectId(projectId);
        record.setSprintName(name);
        record.setGoal("Test goal");
        record.setStartDate(java.time.LocalDate.of(2026, 7, 1));
        record.setEndDate(java.time.LocalDate.of(2026, 7, 15));
        record.setStatus(SprintStatus.PLANNED);
        return record;
    }
}
