package fpt.qn.pms.projectmember.repository;

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
import fpt.qn.pms.jooq.tables.records.ProjectMembersRecord;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.projectmember.repository.projection.ProjectMemberDetails;

class ProjectMemberRepositoryTest extends BaseIntegrationTest {

    @Autowired
    ProjectMemberRepository projectMemberRepository;

    @Autowired
    DSLContext dsl;

    UUID projectId;
    UUID activePmUserId;
    UUID activeDevUserId;
    UUID inactivePmUserId;
    UUID activePmMemberId;

    @BeforeEach
    void setUp() {
        activePmUserId = insertUser("PM");
        activeDevUserId = insertUser("DEV");
        inactivePmUserId = insertUser("OLD_PM");
        projectId = insertProject();
        activePmMemberId = insertMember(activePmUserId, ProjectRole.PM, ProjectMemberStatus.ACTIVE);
        insertMember(activeDevUserId, ProjectRole.DEV, ProjectMemberStatus.ACTIVE);
        insertMember(inactivePmUserId, ProjectRole.PM, ProjectMemberStatus.INACTIVE);
    }

    @Test
    void findByProjectIdAndUserId_shouldReturnMembership() {
        ProjectMembersRecord record = projectMemberRepository
                .findByProjectIdAndUserId(projectId, activePmUserId)
                .orElseThrow();

        assertThat(record.getId()).isEqualTo(activePmMemberId);
        assertThat(record.getProjectRole()).isEqualTo(ProjectRole.PM);
        assertThat(record.getStatus()).isEqualTo(ProjectMemberStatus.ACTIVE);
    }

    @Test
    void findDetailsByIdAndProjectId_shouldJoinUserInformation() {
        ProjectMemberDetails details = projectMemberRepository
                .findDetailsByIdAndProjectId(activePmMemberId, projectId)
                .orElseThrow();

        assertThat(details.userId()).isEqualTo(activePmUserId);
        assertThat(details.employeeId()).startsWith("EMP-PM-");
        assertThat(details.userFullName()).isEqualTo("PM User");
        assertThat(details.email()).contains("pm-");
        assertThat(details.systemRole()).isEqualTo(SysRole.USER);
        assertThat(details.projectRole()).isEqualTo(ProjectRole.PM);
    }

    @Test
    void findDetailsByIdAndProjectId_shouldReturnEmptyForDifferentProject() {
        assertThat(projectMemberRepository.findDetailsByIdAndProjectId(activePmMemberId, UUID.randomUUID()))
                .isEmpty();
    }

    @Test
    void findAllByProjectId_shouldReturnPaginatedDetails() {
        PaginationResult<ProjectMemberDetails> result = projectMemberRepository
                .findAllByProjectId(projectId, null, 0, 2);

        assertThat(result.getTotal()).isEqualTo(3);
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems()).allSatisfy(item -> assertThat(item.projectId()).isEqualTo(projectId));
    }

    @Test
    void findAvailableUsers_shouldExcludeActiveMembersAndIncludeInactiveMembers() {
        PaginationResult<UsersRecord> result = projectMemberRepository
                .findAvailableUsers(projectId, "OLD_PM", 0, 20);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getItems()).extracting(UsersRecord::getId)
                .containsExactly(inactivePmUserId)
                .doesNotContain(activePmUserId, activeDevUserId);
    }

    @Test
    void activeChecks_shouldIgnoreInactiveMemberships() {
        assertThat(projectMemberRepository.existsActiveByProjectIdAndUserId(projectId, activeDevUserId)).isTrue();
        assertThat(projectMemberRepository.existsActiveByProjectIdAndUserId(projectId, inactivePmUserId)).isFalse();
        assertThat(projectMemberRepository.existsActiveByProjectIdAndUserIdAndRole(
                projectId, activePmUserId, ProjectRole.PM)).isTrue();
        assertThat(projectMemberRepository.existsActiveByProjectIdAndUserIdAndRole(
                projectId, inactivePmUserId, ProjectRole.PM)).isFalse();
    }

    @Test
    void countActiveByProjectIdAndRole_shouldExcludeInactiveProjectManagers() {
        assertThat(projectMemberRepository.countActiveByProjectIdAndRole(projectId, ProjectRole.PM)).isEqualTo(1);
    }

    private UUID insertUser(String marker) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return dsl.insertInto(USERS)
                .set(USERS.EMPLOYEE_ID, "EMP-" + marker + "-" + suffix)
                .set(USERS.USERNAME, marker.toLowerCase() + "-" + suffix)
                .set(USERS.FULL_NAME, marker + " User")
                .set(USERS.EMAIL, marker.toLowerCase() + "-" + suffix + "@test.com")
                .set(USERS.PASSWORD, "$2a$10$dummyhash")
                .set(USERS.ROLE, SysRole.USER)
                .set(USERS.STATUS, UserStatus.ACTIVE)
                .returning(USERS.ID)
                .fetchOne(USERS.ID);
    }

    private UUID insertProject() {
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return dsl.insertInto(PROJECTS)
                .set(PROJECTS.PROJECT_CODE, "PM-" + suffix)
                .set(PROJECTS.PROJECT_NAME, "Project Member Test")
                .set(PROJECTS.START_DATE, LocalDate.of(2026, 7, 1))
                .set(PROJECTS.END_DATE, LocalDate.of(2026, 12, 31))
                .set(PROJECTS.STATUS, ProjectStatus.ACTIVE)
                .returning(PROJECTS.ID)
                .fetchOne(PROJECTS.ID);
    }

    private UUID insertMember(UUID userId, ProjectRole role, ProjectMemberStatus status) {
        return dsl.insertInto(PROJECT_MEMBERS)
                .set(PROJECT_MEMBERS.PROJECT_ID, projectId)
                .set(PROJECT_MEMBERS.USER_ID, userId)
                .set(PROJECT_MEMBERS.PROJECT_ROLE, role)
                .set(PROJECT_MEMBERS.STATUS, status)
                .returning(PROJECT_MEMBERS.ID)
                .fetchOne(PROJECT_MEMBERS.ID);
    }
}
