package fpt.qn.pms.projectmember.service;

import static fpt.qn.pms.jooq.Tables.PROJECT_MEMBERS;
import static fpt.qn.pms.jooq.Tables.PROJECTS;
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
import fpt.qn.pms.projectmember.dto.request.AddProjectMemberRequest;
import fpt.qn.pms.projectmember.dto.response.ProjectMemberCandidateDto;
import fpt.qn.pms.projectmember.dto.response.ProjectMemberDto;
import fpt.qn.pms.projectmember.exception.ProjectMemberAccessDeniedException;
import fpt.qn.pms.projectmember.exception.ProjectMemberAlreadyActiveException;
import fpt.qn.pms.projectmember.exception.ProjectMemberNotFoundException;

class ProjectMemberServiceTest extends BaseIntegrationTest {

    @Autowired
    ProjectMemberService projectMemberService;

    @Autowired
    DSLContext dsl;

    UsersRecord admin;
    UsersRecord projectManager;
    UsersRecord developer;
    UsersRecord candidate;
    UUID projectId;
    UUID developerMemberId;

    @BeforeEach
    void setUp() {
        admin = insertUser("member-admin", SysRole.ADMIN);
        projectManager = insertUser("member-pm", SysRole.USER);
        developer = insertUser("member-dev", SysRole.USER);
        candidate = insertUser("member-candidate", SysRole.USER);
        projectId = insertProject("MEMBER");
        insertMembership(projectId, projectManager.getId(), ProjectRole.PM, ProjectMemberStatus.ACTIVE);
        developerMemberId = insertMembership(
                projectId, developer.getId(), ProjectRole.DEV, ProjectMemberStatus.ACTIVE);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMembers_shouldAllowActiveMemberAndReturnPage() {
        authenticate(developer.getUsername());

        PageResponse<ProjectMemberDto> result = projectMemberService.getMembers(projectId, 0, 20);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getItems()).extracting(ProjectMemberDto::getUserId)
                .containsExactlyInAnyOrder(projectManager.getId(), developer.getId());
    }

    @Test
    void getMemberCandidates_shouldAllowProjectManagerAndExcludeActiveMembers() {
        authenticate(projectManager.getUsername());

        PageResponse<ProjectMemberCandidateDto> result = projectMemberService
                .getMemberCandidates(projectId, "candidate", 0, 20);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getItems()).extracting(ProjectMemberCandidateDto::getId)
                .containsExactly(candidate.getId());
    }

    @Test
    void addMember_shouldCreateActiveMembershipForAdministrator() {
        authenticate(admin.getUsername());

        ProjectMemberDto created = projectMemberService.addMember(
                projectId, request(candidate.getId(), ProjectRole.TESTER));

        assertThat(created.getUserId()).isEqualTo(candidate.getId());
        assertThat(created.getProjectRole()).isEqualTo("TESTER");
        assertThat(created.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void addMember_shouldRejectDuplicateActiveMembership() {
        authenticate(projectManager.getUsername());

        assertThatThrownBy(() -> projectMemberService.addMember(
                projectId, request(developer.getId(), ProjectRole.TESTER)))
                .isInstanceOf(ProjectMemberAlreadyActiveException.class);
    }

    @Test
    void addMember_shouldReactivateInactiveMembershipAndUpdateRole() {
        UUID inactiveMemberId = insertMembership(
                projectId, candidate.getId(), ProjectRole.DEV, ProjectMemberStatus.INACTIVE);
        authenticate(projectManager.getUsername());

        ProjectMemberDto reactivated = projectMemberService.addMember(
                projectId, request(candidate.getId(), ProjectRole.PM));

        assertThat(reactivated.getId()).isEqualTo(inactiveMemberId);
        assertThat(reactivated.getProjectRole()).isEqualTo("PM");
        assertThat(reactivated.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void removeMember_shouldSoftDeleteMembership() {
        authenticate(projectManager.getUsername());

        projectMemberService.removeMember(projectId, developerMemberId);

        assertThat(dsl.select(PROJECT_MEMBERS.STATUS)
                .from(PROJECT_MEMBERS)
                .where(PROJECT_MEMBERS.ID.eq(developerMemberId))
                .fetchOne(PROJECT_MEMBERS.STATUS))
                .isEqualTo(ProjectMemberStatus.INACTIVE);
    }

    @Test
    void removeMember_shouldRejectMemberFromAnotherProject() {
        UUID otherProjectId = insertProject("OTHER");
        authenticate(admin.getUsername());

        assertThatThrownBy(() -> projectMemberService.removeMember(otherProjectId, developerMemberId))
                .isInstanceOf(ProjectMemberNotFoundException.class);
    }

    @Test
    void addMember_shouldRejectActiveDeveloperWithoutPmRole() {
        authenticate(developer.getUsername());

        assertThatThrownBy(() -> projectMemberService.addMember(
                projectId, request(candidate.getId(), ProjectRole.DEV)))
                .isInstanceOf(ProjectMemberAccessDeniedException.class);
    }

    private AddProjectMemberRequest request(UUID userId, ProjectRole role) {
        return AddProjectMemberRequest.builder().userId(userId).projectRole(role).build();
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

    private void authenticate(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, List.of()));
    }
}
