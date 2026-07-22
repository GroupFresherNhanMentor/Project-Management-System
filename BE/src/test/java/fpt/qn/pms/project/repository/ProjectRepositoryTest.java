package fpt.qn.pms.project.repository;

import static fpt.qn.pms.jooq.Tables.PROJECT_MEMBERS;
import static fpt.qn.pms.jooq.Tables.PROJECTS;
import static fpt.qn.pms.jooq.Tables.USERS;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.UUID;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fpt.qn.pms.BaseIntegrationTest;
import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.jooq.enums.ProjectMemberStatus;
import fpt.qn.pms.jooq.enums.ProjectRole;
import fpt.qn.pms.jooq.enums.ProjectStatus;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.ProjectsRecord;

class ProjectRepositoryTest extends BaseIntegrationTest {

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    DSLContext dsl;

    UUID userId;
    UUID activeProjectId;
    UUID planningProjectId;

    @BeforeEach
    void setUp() {
        userId = insertUser();
        activeProjectId = insertProject("ALPHA", "Alpha Project", ProjectStatus.ACTIVE);
        planningProjectId = insertProject("BETA", "Beta Project", ProjectStatus.PLANNING);
        insertMembership(activeProjectId, ProjectRole.PM, ProjectMemberStatus.ACTIVE);
        insertMembership(planningProjectId, ProjectRole.PM, ProjectMemberStatus.INACTIVE);
    }

    @Test
    void existsByProjectCodeIgnoreCase_shouldBeCaseInsensitive() {
        String storedCode = dsl.select(PROJECTS.PROJECT_CODE)
                .from(PROJECTS)
                .where(PROJECTS.ID.eq(activeProjectId))
                .fetchOne(PROJECTS.PROJECT_CODE);

        assertThat(projectRepository.existsByProjectCodeIgnoreCase(storedCode.toLowerCase())).isTrue();
        assertThat(projectRepository.existsByProjectCodeIgnoreCase("missing")).isFalse();
    }

    @Test
    void findAll_shouldFilterByKeywordAndStatus() {
        PaginationResult<ProjectsRecord> result = projectRepository
                .findAll("Alpha", ProjectStatus.ACTIVE, null, 0, 20);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getItems()).extracting(ProjectsRecord::getId).containsExactly(activeProjectId);
    }

    @Test
    void findAll_shouldScopeUserToActiveProjectManagerMemberships() {
        PaginationResult<ProjectsRecord> result = projectRepository.findAll(null, null, userId, 0, 20);

        assertThat(result.getItems()).extracting(ProjectsRecord::getId)
                .contains(activeProjectId)
                .doesNotContain(planningProjectId);
    }

    @Test
    void findAll_shouldExcludeActiveNonProjectManagerMembership() {
        UUID developerProjectId = insertProject("DEV", "Developer Project", ProjectStatus.ACTIVE);
        insertMembership(developerProjectId, ProjectRole.DEV, ProjectMemberStatus.ACTIVE);

        PaginationResult<ProjectsRecord> result = projectRepository.findAll(null, null, userId, 0, 20);

        assertThat(result.getItems()).extracting(ProjectsRecord::getId)
                .doesNotContain(developerProjectId);
    }

    @Test
    void findAll_shouldApplyPagination() {
        PaginationResult<ProjectsRecord> result = projectRepository.findAll(null, null, null, 0, 1);

        assertThat(result.getTotal()).isGreaterThanOrEqualTo(2);
        assertThat(result.getItems()).hasSize(1);
    }

    private UUID insertUser() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return dsl.insertInto(USERS)
                .set(USERS.EMPLOYEE_ID, "PROJECT-EMP-" + suffix)
                .set(USERS.USERNAME, "project-user-" + suffix)
                .set(USERS.FULL_NAME, "Project User")
                .set(USERS.EMAIL, "project-user-" + suffix + "@test.com")
                .set(USERS.PASSWORD, "$2a$10$dummyhash")
                .set(USERS.ROLE, SysRole.USER)
                .set(USERS.STATUS, UserStatus.ACTIVE)
                .returning(USERS.ID)
                .fetchOne(USERS.ID);
    }

    private UUID insertProject(String marker, String name, ProjectStatus status) {
        String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return dsl.insertInto(PROJECTS)
                .set(PROJECTS.PROJECT_CODE, marker + "-" + suffix)
                .set(PROJECTS.PROJECT_NAME, name)
                .set(PROJECTS.START_DATE, LocalDate.of(2026, 7, 1))
                .set(PROJECTS.END_DATE, LocalDate.of(2026, 12, 31))
                .set(PROJECTS.STATUS, status)
                .returning(PROJECTS.ID)
                .fetchOne(PROJECTS.ID);
    }

    private void insertMembership(UUID projectId, ProjectRole role, ProjectMemberStatus status) {
        dsl.insertInto(PROJECT_MEMBERS)
                .set(PROJECT_MEMBERS.PROJECT_ID, projectId)
                .set(PROJECT_MEMBERS.USER_ID, userId)
                .set(PROJECT_MEMBERS.PROJECT_ROLE, role)
                .set(PROJECT_MEMBERS.STATUS, status)
                .execute();
    }
}
