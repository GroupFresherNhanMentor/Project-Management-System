package fpt.qn.pms.dashboard.service;

import static fpt.qn.pms.jooq.Tables.PROJECTS;
import static fpt.qn.pms.jooq.Tables.SPRINTS;
import static fpt.qn.pms.jooq.Tables.TASKS;
import static fpt.qn.pms.jooq.Tables.TASK_STATUSES;
import static fpt.qn.pms.jooq.Tables.USERS;
import static fpt.qn.pms.jooq.Tables.WORKLOGS;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.pms.common.exception.AppException;
import fpt.qn.pms.dashboard.dto.DashboardAdminResponse;
import fpt.qn.pms.dashboard.dto.DashboardPersonalResponse;
import fpt.qn.pms.dashboard.dto.DashboardProjectResponse;
import fpt.qn.pms.dashboard.dto.SprintProgressDto;
import fpt.qn.pms.jooq.enums.ProjectStatus;
import fpt.qn.pms.jooq.enums.SprintStatus;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.TaskPriority;
import fpt.qn.pms.jooq.enums.TaskType;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.SprintsRecord;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardServiceImpl implements DashboardService {

    DSLContext dsl;

    @Override
    @Transactional(readOnly = true)
    public DashboardPersonalResponse getPersonalDashboard(String username) {
        UUID userId = dsl.select(USERS.ID)
                .from(USERS)
                .where(USERS.USERNAME.eq(username))
                .fetchOptional(USERS.ID)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found: " + username));

        // "open" = no final status assigned yet
        var isFinalStatus = TASKS.STATUS_ID.in(
                dsl.select(TASK_STATUSES.ID).from(TASK_STATUSES).where(TASK_STATUSES.IS_FINAL.isTrue()));

        Long openTasksCount = dsl.selectCount()
                .from(TASKS)
                .where(TASKS.ASSIGNEE_ID.eq(userId).and(isFinalStatus.not()))
                .fetchOne(0, Long.class);
        long openTasks = openTasksCount != null ? openTasksCount : 0L;

        Long completedTasksCount = dsl.selectCount()
                .from(TASKS)
                .where(TASKS.ASSIGNEE_ID.eq(userId).and(isFinalStatus))
                .fetchOne(0, Long.class);
        long completedTasks = completedTasksCount != null ? completedTasksCount : 0L;

        Long overdueTasksCount = dsl.selectCount()
                .from(TASKS)
                .where(TASKS.ASSIGNEE_ID.eq(userId)
                        .and(isFinalStatus.not())
                        .and(TASKS.DUE_DATE.lt(LocalDate.now())))
                .fetchOne(0, Long.class);
        long overdueTasks = overdueTasksCount != null ? overdueTasksCount : 0L;

        BigDecimal totalHours = dsl.select(DSL.sum(WORKLOGS.HOURS))
                .from(WORKLOGS)
                .where(WORKLOGS.USER_ID.eq(userId))
                .fetchOne(0, BigDecimal.class);

        if (totalHours == null) {
            totalHours = BigDecimal.ZERO;
        }

        return DashboardPersonalResponse.builder()
                .myOpenTasks(openTasks)
                .myCompletedTasks(completedTasks)
                .myOverdueTasks(overdueTasks)
                .totalLoggedHours(totalHours)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardProjectResponse getProjectDashboard(UUID projectId) {
        boolean projectExists = dsl.fetchExists(PROJECTS, PROJECTS.ID.eq(projectId));
        if (!projectExists) {
            throw new AppException(HttpStatus.NOT_FOUND, "Project not found with ID: " + projectId);
        }

        Long totalTasksCount = dsl.selectCount()
                .from(TASKS)
                .where(TASKS.PROJECT_ID.eq(projectId))
                .fetchOne(0, Long.class);
        long totalTasks = totalTasksCount != null ? totalTasksCount : 0L;

        // Group by status name via JOIN
        Map<String, Integer> taskByStatus = new HashMap<>();
        dsl.select(TASK_STATUSES.NAME, DSL.count())
                .from(TASKS)
                .leftJoin(TASK_STATUSES).on(TASKS.STATUS_ID.eq(TASK_STATUSES.ID))
                .where(TASKS.PROJECT_ID.eq(projectId))
                .groupBy(TASK_STATUSES.NAME)
                .fetch()
                .forEach(r -> {
                    String name = r.get(TASK_STATUSES.NAME);
                    Long countVal = r.get(1, Long.class);
                    if (name != null && countVal != null) {
                        taskByStatus.put(name, countVal.intValue());
                    }
                });

        // Thống kê Task theo Priority
        Map<String, Integer> taskByPriority = new HashMap<>();
        for (TaskPriority priority : TaskPriority.values()) {
            taskByPriority.put(priority.getLiteral(), 0);
        }
        dsl.select(TASKS.PRIORITY, DSL.count())
                .from(TASKS)
                .where(TASKS.PROJECT_ID.eq(projectId))
                .groupBy(TASKS.PRIORITY)
                .fetch()
                .forEach(r -> {
                    TaskPriority priority = r.get(TASKS.PRIORITY);
                    Long countVal = r.get(1, Long.class);
                    if (priority != null && countVal != null) {
                        taskByPriority.put(priority.getLiteral(), countVal.intValue());
                    }
                });

        BigDecimal totalHours = dsl.select(DSL.sum(WORKLOGS.HOURS))
                .from(WORKLOGS)
                .join(TASKS).on(WORKLOGS.TASK_ID.eq(TASKS.ID))
                .where(TASKS.PROJECT_ID.eq(projectId))
                .fetchOne(0, BigDecimal.class);

        if (totalHours == null) {
            totalHours = BigDecimal.ZERO;
        }

        Optional<SprintsRecord> activeSprintOpt = dsl.selectFrom(SPRINTS)
                .where(SPRINTS.PROJECT_ID.eq(projectId)
                        .and(SPRINTS.STATUS.eq(SprintStatus.ACTIVE)))
                .fetchOptional();

        SprintProgressDto sprintProgress = null;
        if (activeSprintOpt.isPresent()) {
            SprintsRecord activeSprint = activeSprintOpt.get();
            UUID sprintId = activeSprint.getId();

            Long sprintTotalCount = dsl.selectCount()
                    .from(TASKS)
                    .where(TASKS.SPRINT_ID.eq(sprintId))
                    .fetchOne(0, Long.class);
            long sprintTotalTasks = sprintTotalCount != null ? sprintTotalCount : 0L;

            Long sprintDoneCount = dsl.selectCount()
                    .from(TASKS)
                    .join(TASK_STATUSES).on(TASKS.STATUS_ID.eq(TASK_STATUSES.ID))
                    .where(TASKS.SPRINT_ID.eq(sprintId).and(TASK_STATUSES.IS_FINAL.isTrue()))
                    .fetchOne(0, Long.class);
            long sprintDoneTasks = sprintDoneCount != null ? sprintDoneCount : 0L;

            double percentComplete = 0.0;
            if (sprintTotalTasks > 0) {
                percentComplete = (sprintDoneTasks * 100.0) / sprintTotalTasks;
            }

            sprintProgress = SprintProgressDto.builder()
                    .sprintId(sprintId)
                    .sprintName(activeSprint.getSprintName())
                    .totalTasks(sprintTotalTasks)
                    .doneTasks(sprintDoneTasks)
                    .percentComplete(percentComplete)
                    .build();
        }

        return DashboardProjectResponse.builder()
                .totalTasks(totalTasks)
                .taskByStatus(taskByStatus)
                .taskByPriority(taskByPriority)
                .totalLoggedHours(totalHours)
                .sprintProgress(sprintProgress)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardAdminResponse getAdminDashboard() {
        Long totalUsersCount = dsl.selectCount().from(USERS).fetchOne(0, Long.class);
        long totalUsers = totalUsersCount != null ? totalUsersCount : 0L;

        Long activeUsersCount = dsl.selectCount()
                .from(USERS)
                .where(USERS.STATUS.eq(UserStatus.ACTIVE))
                .fetchOne(0, Long.class);
        long activeUsers = activeUsersCount != null ? activeUsersCount : 0L;

        Long lockedUsersCount = dsl.selectCount()
                .from(USERS)
                .where(USERS.STATUS.eq(UserStatus.LOCKED))
                .fetchOne(0, Long.class);
        long lockedUsers = lockedUsersCount != null ? lockedUsersCount : 0L;

        Long totalProjectsCount = dsl.selectCount().from(PROJECTS).fetchOne(0, Long.class);
        long totalProjects = totalProjectsCount != null ? totalProjectsCount : 0L;

        Long activeProjectsCount = dsl.selectCount()
                .from(PROJECTS)
                .where(PROJECTS.STATUS.eq(ProjectStatus.ACTIVE))
                .fetchOne(0, Long.class);
        long activeProjects = activeProjectsCount != null ? activeProjectsCount : 0L;

        Long totalTasksCount = dsl.selectCount().from(TASKS).fetchOne(0, Long.class);
        long totalTasks = totalTasksCount != null ? totalTasksCount : 0L;

        BigDecimal totalHours = dsl.select(DSL.sum(WORKLOGS.HOURS))
                .from(WORKLOGS)
                .fetchOne(0, BigDecimal.class);
        if (totalHours == null) {
            totalHours = BigDecimal.ZERO;
        }

        // Project Status Breakdown
        Map<String, Integer> projectByStatus = new HashMap<>();
        for (ProjectStatus status : ProjectStatus.values()) {
            projectByStatus.put(status.getLiteral(), 0);
        }
        dsl.select(PROJECTS.STATUS, DSL.count())
                .from(PROJECTS)
                .groupBy(PROJECTS.STATUS)
                .fetch()
                .forEach(r -> {
                    ProjectStatus status = r.get(PROJECTS.STATUS);
                    Long countVal = r.get(1, Long.class);
                    if (status != null && countVal != null) {
                        projectByStatus.put(status.getLiteral(), countVal.intValue());
                    }
                });

        // Task Type Breakdown
        Map<String, Integer> taskByType = new HashMap<>();
        for (TaskType type : TaskType.values()) {
            taskByType.put(type.getLiteral(), 0);
        }
        dsl.select(TASKS.TASK_TYPE, DSL.count())
                .from(TASKS)
                .groupBy(TASKS.TASK_TYPE)
                .fetch()
                .forEach(r -> {
                    TaskType type = r.get(TASKS.TASK_TYPE);
                    Long countVal = r.get(1, Long.class);
                    if (type != null && countVal != null) {
                        taskByType.put(type.getLiteral(), countVal.intValue());
                    }
                });

        // User System Role Breakdown
        Map<String, Integer> userByRole = new HashMap<>();
        for (SysRole role : SysRole.values()) {
            userByRole.put(role.getLiteral(), 0);
        }
        dsl.select(USERS.ROLE, DSL.count())
                .from(USERS)
                .groupBy(USERS.ROLE)
                .fetch()
                .forEach(r -> {
                    SysRole role = r.get(USERS.ROLE);
                    Long countVal = r.get(1, Long.class);
                    if (role != null && countVal != null) {
                        userByRole.put(role.getLiteral(), countVal.intValue());
                    }
                });

        return DashboardAdminResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .lockedUsers(lockedUsers)
                .totalProjects(totalProjects)
                .activeProjects(activeProjects)
                .totalTasks(totalTasks)
                .totalLoggedHours(totalHours)
                .projectByStatus(projectByStatus)
                .taskByType(taskByType)
                .userByRole(userByRole)
                .build();
    }
}
