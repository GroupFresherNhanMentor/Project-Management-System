package fpt.qn.pms.task.repository;

import static fpt.qn.pms.jooq.Tables.TASK_STATUSES;
import static fpt.qn.pms.jooq.Tables.TASKS;
import static fpt.qn.pms.jooq.Tables.USERS;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.repository.BaseRepository;
import fpt.qn.pms.jooq.tables.Users;
import fpt.qn.pms.jooq.tables.records.TasksRecord;
import fpt.qn.pms.task.dto.TaskSearchRequest;
import fpt.qn.pms.task.repository.resultModel.TaskResult;

@Repository
public class TaskRepositoryImpl extends BaseRepository<TasksRecord> implements TaskRepository {

    private static final Users ASSIGNEE = USERS.as("assignee");
    private static final Users REPORTER = USERS.as("reporter");

    public TaskRepositoryImpl(DSLContext dsl) {
        super(dsl, TASKS);
    }

    @Override
    public boolean existsAssignedTaskByProjectIdAndAssigneeId(UUID projectId, UUID assigneeId) {
        return dsl.fetchExists(TASKS,
                TASKS.PROJECT_ID.eq(projectId)
                        .and(TASKS.ASSIGNEE_ID.eq(assigneeId)));
    }

    @Override
    public Optional<TaskResult> findDetailById(UUID id) {
        return dsl.select(TASKS.fields())
                .select(ASSIGNEE.FULL_NAME, REPORTER.FULL_NAME, TASK_STATUSES.NAME, TASK_STATUSES.COLOR)
                .from(TASKS)
                .leftJoin(ASSIGNEE).on(TASKS.ASSIGNEE_ID.eq(ASSIGNEE.ID))
                .leftJoin(REPORTER).on(TASKS.REPORTER_ID.eq(REPORTER.ID))
                .leftJoin(TASK_STATUSES).on(TASKS.STATUS_ID.eq(TASK_STATUSES.ID))
                .where(TASKS.ID.eq(id))
                .fetchOptional(r -> new TaskResult(
                        r.into(TASKS),
                        r.get(ASSIGNEE.FULL_NAME),
                        r.get(REPORTER.FULL_NAME),
                        r.get(TASK_STATUSES.NAME),
                        r.get(TASK_STATUSES.COLOR)
                ));
    }

    @Override
    public PaginationResult<TaskResult> findAll(TaskSearchRequest request) {
        Condition condition = DSL.noCondition();

        if (request.getProjectId() != null) {
            condition = condition.and(TASKS.PROJECT_ID.eq(request.getProjectId()));
        }
        if (request.getSprintId() != null) {
            condition = condition.and(TASKS.SPRINT_ID.eq(request.getSprintId()));
        }
        if (request.getStatusId() != null) {
            condition = condition.and(TASKS.STATUS_ID.eq(request.getStatusId()));
        }
        if (request.getPriority() != null) {
            condition = condition.and(TASKS.PRIORITY.eq(request.getPriority()));
        }
        if (request.getAssigneeId() != null) {
            condition = condition.and(TASKS.ASSIGNEE_ID.eq(request.getAssigneeId()));
        }
        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            String pattern = "%" + request.getKeyword().toLowerCase() + "%";
            condition = condition.and(
                    TASKS.TASK_KEY.likeIgnoreCase(pattern)
                            .or(TASKS.SUMMARY.likeIgnoreCase(pattern))
            );
        }

        long total = dsl.fetchCount(TASKS, condition);

        int page = request.getPage();
        int size = request.getSize();

        List<TaskResult> items = dsl.select(TASKS.fields())
                .select(ASSIGNEE.FULL_NAME, REPORTER.FULL_NAME, TASK_STATUSES.NAME, TASK_STATUSES.COLOR)
                .from(TASKS)
                .leftJoin(ASSIGNEE).on(TASKS.ASSIGNEE_ID.eq(ASSIGNEE.ID))
                .leftJoin(REPORTER).on(TASKS.REPORTER_ID.eq(REPORTER.ID))
                .leftJoin(TASK_STATUSES).on(TASKS.STATUS_ID.eq(TASK_STATUSES.ID))
                .where(condition)
                .orderBy(TASKS.CREATED_AT.desc())
                .limit(size)
                .offset((long) page * size)
                .fetch(r -> new TaskResult(
                        r.into(TASKS),
                        r.get(ASSIGNEE.FULL_NAME),
                        r.get(REPORTER.FULL_NAME),
                        r.get(TASK_STATUSES.NAME),
                        r.get(TASK_STATUSES.COLOR)
                ));

        return new PaginationResult<>(total, items);
    }
}
