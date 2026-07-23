package fpt.qn.pms.security;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import fpt.qn.pms.jooq.enums.ProjectMemberStatus;
import fpt.qn.pms.jooq.enums.ProjectRole;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.tables.records.TasksRecord;
import fpt.qn.pms.projectmember.repository.ProjectMemberRepository;
import fpt.qn.pms.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("projectSecurityEvaluator")
@RequiredArgsConstructor
@Slf4j
public class ProjectSecurityEvaluator {

    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;

    public Optional<UserPrincipal> getCurrentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        if (auth.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public boolean isAdmin() {
        return getCurrentPrincipal()
                .map(principal -> principal.getAuthorities().stream()
                        .anyMatch(a -> SysRole.ADMIN.getLiteral().equals(a.getAuthority())))
                .orElse(false);
    }

    public boolean isMember(UUID projectId) {
        if (projectId == null)
            return false;
        return getCurrentPrincipal()
                .flatMap(p -> projectMemberRepository.findByProjectIdAndUserId(projectId, p.getId()))
                .map(member -> member.getStatus() == ProjectMemberStatus.ACTIVE)
                .orElse(false);

    }

    public boolean isPm(UUID projectId) {
        return getCurrentPrincipal()
                .flatMap(p -> projectMemberRepository.findByProjectIdAndUserId(projectId, p.getId()))
                .map(member -> member.getStatus() == ProjectMemberStatus.ACTIVE
                        && member.getProjectRole() == ProjectRole.PM)
                .orElse(false);
    }

    public boolean requireRole(UUID projectId, List<ProjectRole> roles) {
        if (projectId == null)
            return false;
        return getCurrentPrincipal()
                .flatMap(p -> projectMemberRepository.findByProjectIdAndUserId(projectId, p.getId()))
                .map(member -> member.getStatus() == ProjectMemberStatus.ACTIVE
                        && roles.contains(member.getProjectRole()))
                .orElse(false);
    }

    public boolean hasAccessToTask(UUID taskId) {
        Optional<TasksRecord> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty())
            return false;
        return isMember(taskOpt.get().getProjectId());
    }

    public boolean isPmOfTask(UUID taskId) {
        Optional<TasksRecord> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty())
            return false;
        return isPm(taskOpt.get().getProjectId());
    }
}
