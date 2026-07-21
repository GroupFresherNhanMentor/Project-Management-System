package fpt.qn.pms.projectmember.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import fpt.qn.pms.jooq.enums.ProjectRole;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.projectmember.exception.ProjectMemberAccessDeniedException;
import fpt.qn.pms.projectmember.repository.ProjectMemberRepository;
import fpt.qn.pms.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProjectMemberAuthorizationServiceTest {

    @Mock
    ProjectMemberRepository projectMemberRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ProjectMemberAuthorizationService authorizationService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void assertCanManageMembers_shouldAllowActiveAdministrator() {
        UUID projectId = UUID.randomUUID();
        UsersRecord admin = user("admin", SysRole.ADMIN, UserStatus.ACTIVE);
        authenticate("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        UsersRecord result = authorizationService.assertCanManageMembers(projectId);

        assertThat(result).isSameAs(admin);
        verify(projectMemberRepository, never())
                .existsActiveByProjectIdAndUserIdAndRole(projectId, admin.getId(), ProjectRole.PM);
    }

    @Test
    void assertCanManageMembers_shouldAllowActiveProjectManager() {
        UUID projectId = UUID.randomUUID();
        UsersRecord projectManager = user("pm", SysRole.USER, UserStatus.ACTIVE);
        authenticate("pm");
        when(userRepository.findByUsername("pm")).thenReturn(Optional.of(projectManager));
        when(projectMemberRepository.existsActiveByProjectIdAndUserIdAndRole(
                projectId, projectManager.getId(), ProjectRole.PM)).thenReturn(true);

        assertThat(authorizationService.assertCanManageMembers(projectId)).isSameAs(projectManager);
    }

    @Test
    void assertCanManageMembers_shouldRejectInactiveProjectManagerMembership() {
        UUID projectId = UUID.randomUUID();
        UsersRecord projectManager = user("pm", SysRole.USER, UserStatus.ACTIVE);
        authenticate("pm");
        when(userRepository.findByUsername("pm")).thenReturn(Optional.of(projectManager));
        when(projectMemberRepository.existsActiveByProjectIdAndUserIdAndRole(
                projectId, projectManager.getId(), ProjectRole.PM)).thenReturn(false);

        assertThatThrownBy(() -> authorizationService.assertCanManageMembers(projectId))
                .isInstanceOf(ProjectMemberAccessDeniedException.class);
    }

    @Test
    void assertCanManageMembers_shouldRejectLockedAdministrator() {
        UUID projectId = UUID.randomUUID();
        UsersRecord admin = user("admin", SysRole.ADMIN, UserStatus.LOCKED);
        authenticate("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> authorizationService.assertCanManageMembers(projectId))
                .isInstanceOf(ProjectMemberAccessDeniedException.class);
    }

    @Test
    void assertCanManageMembers_shouldRejectMissingAuthenticationWithoutMockFallback() {
        assertThatThrownBy(() -> authorizationService.assertCanManageMembers(UUID.randomUUID()))
                .isInstanceOf(ProjectMemberAccessDeniedException.class);
    }

    @Test
    void assertCanViewMembers_shouldAllowActiveProjectMember() {
        UUID projectId = UUID.randomUUID();
        UsersRecord member = user("member", SysRole.USER, UserStatus.ACTIVE);
        authenticate("member");
        when(userRepository.findByUsername("member")).thenReturn(Optional.of(member));
        when(projectMemberRepository.existsActiveByProjectIdAndUserId(projectId, member.getId()))
                .thenReturn(true);

        assertThat(authorizationService.assertCanViewMembers(projectId)).isSameAs(member);
    }

    @Test
    void assertCanViewMembers_shouldRejectNonMember() {
        UUID projectId = UUID.randomUUID();
        UsersRecord user = user("user", SysRole.USER, UserStatus.ACTIVE);
        authenticate("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(projectMemberRepository.existsActiveByProjectIdAndUserId(projectId, user.getId()))
                .thenReturn(false);

        assertThatThrownBy(() -> authorizationService.assertCanViewMembers(projectId))
                .isInstanceOf(ProjectMemberAccessDeniedException.class);
    }

    private void authenticate(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, List.of()));
    }

    private UsersRecord user(String username, SysRole role, UserStatus status) {
        UsersRecord record = new UsersRecord();
        record.setId(UUID.randomUUID());
        record.setUsername(username);
        record.setRole(role);
        record.setStatus(status);
        return record;
    }
}
