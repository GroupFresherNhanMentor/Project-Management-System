package fpt.qn.pms.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import fpt.qn.pms.jooq.enums.ProjectMemberStatus;
import fpt.qn.pms.jooq.enums.ProjectRole;
import fpt.qn.pms.jooq.tables.records.TasksRecord;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.projectmember.repository.ProjectMemberRepository;
import fpt.qn.pms.task.repository.TaskRepository;
import fpt.qn.pms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component("projectSecurityEvaluator")
@RequiredArgsConstructor
public class ProjectSecurityEvaluator {

    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;

    public Optional<UUID> getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return Optional.empty();
        }
        if (auth.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.ofNullable(principal.getId());
        }
        if (auth.getPrincipal() instanceof Jwt jwt) {
            String username = jwt.getSubject();
            return userRepository.findByUsername(username).map(UsersRecord::getId);
        }
        return Optional.empty();
    }

    public boolean isMember(UUID projectId) {
        return getCurrentUserId()
                .flatMap(userId -> projectMemberRepository.findByProjectIdAndUserId(projectId, userId))
                .map(member -> member.getStatus() == ProjectMemberStatus.ACTIVE)
                .orElse(false);
    }

    public boolean isPm(UUID projectId) {
        return getCurrentUserId()
                .flatMap(userId -> projectMemberRepository.findByProjectIdAndUserId(projectId, userId))
                .map(member -> member.getStatus() == ProjectMemberStatus.ACTIVE 
                        && member.getProjectRole() == ProjectRole.PM)
                .orElse(false);
    }

    public boolean hasAccessToTask(UUID taskId) {
        Optional<TasksRecord> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return false;
        }
        return isMember(taskOpt.get().getProjectId());
    }

    public boolean isPmOfTask(UUID taskId) {
        Optional<TasksRecord> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return false;
        }
        return isPm(taskOpt.get().getProjectId());
    }
}
