package fpt.qn.pms.security.aspect;

import static fpt.qn.pms.jooq.Tables.PROJECT_MEMBERS;
import static fpt.qn.pms.jooq.Tables.TASKS;
import static fpt.qn.pms.jooq.Tables.USERS;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.jooq.DSLContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import fpt.qn.pms.common.exception.AppException;
import fpt.qn.pms.jooq.enums.ProjectRole;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.security.annotation.RequireProjectRole;
import fpt.qn.pms.security.annotation.RequireProjectRoles;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Aspect
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectRoleAspect {

    DSLContext dsl;

    @Before("@annotation(requireProjectRole)")
    public void checkSingleRole(JoinPoint joinPoint, RequireProjectRole requireProjectRole) {
        validateRoles(joinPoint, new RequireProjectRole[]{requireProjectRole});
    }

    @Before("@annotation(requireProjectRoles)")
    public void checkMultipleRoles(JoinPoint joinPoint, RequireProjectRoles requireProjectRoles) {
        validateRoles(joinPoint, requireProjectRoles.value());
    }

    private void validateRoles(JoinPoint joinPoint, RequireProjectRole[] roleAnnotations) {
        UUID projectId = extractProjectId(joinPoint);
        if (projectId == null) {
            throw new IllegalArgumentException("Unable to determine Project ID for authorization check");
        }

        UsersRecord currentUser = getCurrentUser();

        // AND logic across repeated annotations
        for (RequireProjectRole annotation : roleAnnotations) {
            ProjectRole[] allowedRoles = annotation.value();

            // OR logic within the values array of a single annotation
            boolean hasAnyRole = dsl.fetchExists(
                    PROJECT_MEMBERS,
                    PROJECT_MEMBERS.PROJECT_ID.eq(projectId)
                            .and(PROJECT_MEMBERS.USER_ID.eq(currentUser.getId()))
                            .and(PROJECT_MEMBERS.PROJECT_ROLE.in(allowedRoles))
            );

            if (!hasAnyRole) {
                throw new AccessDeniedException("Access denied: Required role matching " 
                        + Arrays.toString(allowedRoles) + " in project " + projectId);
            }
        }
    }

    private UsersRecord getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            UsersRecord mockUser = dsl.selectFrom(USERS).limit(1).fetchOne();
            if (mockUser == null) {
                throw new AppException("No users found in database to mock authentication");
            }
            return mockUser;
        }
        String username = auth.getName();
        return dsl.selectFrom(USERS)
                .where(USERS.USERNAME.eq(username))
                .fetchOptional()
                .orElseThrow(() -> new AppException("Current user not found"));
    }

    private UUID extractProjectId(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return null;
        }

        // 1. Try to find getProjectId() method on any DTO object argument
        for (Object arg : args) {
            if (arg != null) {
                try {
                    Method method = arg.getClass().getMethod("getProjectId");
                    Object result = method.invoke(arg);
                    if (result instanceof UUID uuid) {
                        return uuid;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        // 2. If an argument is UUID
        for (Object arg : args) {
            if (arg instanceof UUID uuid) {
                // Check if this UUID corresponds to a task, lookup its project_id
                UUID projectId = dsl.select(TASKS.PROJECT_ID)
                        .from(TASKS)
                        .where(TASKS.ID.eq(uuid))
                        .fetchOne(TASKS.PROJECT_ID);

                if (projectId != null) {
                    return projectId;
                }
                // Otherwise assume it is directly a projectId
                return uuid;
            }
        }

        return null;
    }
}
