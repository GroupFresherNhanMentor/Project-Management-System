package fpt.qn.pms.projectmember.service;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.pms.jooq.enums.ProjectRole;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.projectmember.exception.ProjectMemberAccessDeniedException;
import fpt.qn.pms.projectmember.repository.ProjectMemberRepository;
import fpt.qn.pms.user.exception.UserNotFoundException;
import fpt.qn.pms.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectMemberAuthorizationService {

    ProjectMemberRepository projectMemberRepository;
    UserRepository userRepository;

    @Transactional(readOnly = true)
    public UsersRecord assertCanViewMembers(UUID projectId) {
        UsersRecord currentUser = requireActiveCurrentUser();
        if (currentUser.getRole() == SysRole.ADMIN
                || projectMemberRepository.existsActiveByProjectIdAndUserId(projectId, currentUser.getId())) {
            return currentUser;
        }
        throw new ProjectMemberAccessDeniedException();
    }

    @Transactional(readOnly = true)
    public UsersRecord assertCanManageMembers(UUID projectId) {
        UsersRecord currentUser = requireActiveCurrentUser();

        if (currentUser.getRole() == SysRole.ADMIN) {
            return currentUser;
        }

        boolean isActiveProjectManager = projectMemberRepository
                .existsActiveByProjectIdAndUserIdAndRole(projectId, currentUser.getId(), ProjectRole.PM);

        if (!isActiveProjectManager) {
            throw new ProjectMemberAccessDeniedException();
        }

        return currentUser;
    }

    private UsersRecord requireActiveCurrentUser() {
        UsersRecord currentUser = getCurrentUser();
        if (currentUser.getStatus() != UserStatus.ACTIVE) {
            throw new ProjectMemberAccessDeniedException();
        }
        return currentUser;
    }

    private UsersRecord getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ProjectMemberAccessDeniedException();
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException());
    }
}
