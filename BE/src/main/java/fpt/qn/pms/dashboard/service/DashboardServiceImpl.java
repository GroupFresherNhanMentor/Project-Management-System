package fpt.qn.pms.dashboard.service;

import static fpt.qn.pms.jooq.Tables.PROJECTS;
import static fpt.qn.pms.jooq.Tables.SPRINTS;
import static fpt.qn.pms.jooq.Tables.TASKS;
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
import fpt.qn.pms.jooq.enums.TaskStatus;
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

        Long openTasksCount = dsl.selectCount()
                .from(TASKS)
                .where(TASKS.ASSIGNEE_ID.eq(userId)
                        .and(TASKS.STATUS.ne(TaskStatus.DONE)))
                .fetchOne(0, Long.class);
        long openTasks = openTasksCount != null ? openTasksCount : 0L;

        Long completedTasksCount = dsl.selectCount()
                .from(TASKS)
                .where(TASKS.ASSIGNEE_ID.eq(userId)
                        .and(TASKS.STATUS.eq(TaskStatus.DONE)))
                .fetchOne(0, Long.class);
        long completedTasks = completedTasksCount != null ? completedTasksCount : 0L;

        Long overdueTasksCount = dsl.selectCount()
                .from(TASKS)
                .where(TASKS.ASSIGNEE_ID.eq(userId)
                        .and(TASKS.STATUS.ne(TaskStatus.DONE))
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

        // Thống kê Task theo Status
        Map<String, Integer> taskByStatus = new HashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            taskByStatus.put(status.getLiteral(), 0);
        }
        dsl.select(TASKS.STATUS, DSL.count())
                .from(TASKS)
                .where(TASKS.PROJECT_ID.eq(projectId))
                .groupBy(TASKS.STATUS)
                .fetch()
                .forEach(r -> {
                    TaskStatus status = r.get(TASKS.STATUS);
                    Long countVal = r.get(1, Long.class);
                    if (status != null && countVal != null) {
                        taskByStatus.put(status.getLiteral(), countVal.intValue());
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

        // Tổng số giờ log của Project
        BigDecimal totalHours = dsl.select(DSL.sum(WORKLOGS.HOURS))
                .from(WORKLOGS)
                .join(TASKS).on(WORKLOGS.TASK_ID.eq(TASKS.ID))
                .where(TASKS.PROJECT_ID.eq(projectId))
                .fetchOne(0, BigDecimal.class);

        if (totalHours == null) {
            totalHours = BigDecimal.ZERO;
        }

        // Tiến độ Active Sprint
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
                    .where(TASKS.SPRINT_ID.eq(sprintId)
                            .and(TASKS.STATUS.eq(TaskStatus.DONE)))
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
