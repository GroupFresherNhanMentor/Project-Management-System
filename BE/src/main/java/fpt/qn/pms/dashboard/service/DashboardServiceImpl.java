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
import fpt.qn.pms.dashboard.dto.DashboardPersonalResponse;
import fpt.qn.pms.dashboard.dto.DashboardProjectResponse;
import fpt.qn.pms.dashboard.dto.SprintProgressDto;
import fpt.qn.pms.jooq.enums.SprintStatus;
import fpt.qn.pms.jooq.enums.TaskPriority;
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
}
