package fpt.qn.pms.task.repository;

import static fpt.qn.pms.jooq.Tables.TASKS;

import java.util.List;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.repository.BaseRepository;
import fpt.qn.pms.jooq.tables.records.TasksRecord;
import fpt.qn.pms.task.dto.TaskSearchRequest;

@Repository
public class TaskRepositoryImpl extends BaseRepository<TasksRecord> implements TaskRepository {

    public TaskRepositoryImpl(DSLContext dsl) {
        super(dsl, TASKS);
    }

    @Override
    public int getNextTaskNumber(UUID projectId) {
        Integer count = dsl.selectCount()
                .from(TASKS)
                .where(TASKS.PROJECT_ID.eq(projectId))
                .fetchOne(0, Integer.class);
        return count == null ? 1 : count + 1;
    }

    @Override
    public boolean existsAssignedTaskByProjectIdAndAssigneeId(UUID projectId, UUID assigneeId) {
        return dsl.fetchExists(TASKS,
                TASKS.PROJECT_ID.eq(projectId)
                        .and(TASKS.ASSIGNEE_ID.eq(assigneeId)));
    }

    @Override
    public PaginationResult<TasksRecord> findAll(TaskSearchRequest request) {
        Condition condition = DSL.noCondition();

        if (request.getProjectId() != null) {
            condition = condition.and(TASKS.PROJECT_ID.eq(request.getProjectId()));
        }
        if (request.getSprintId() != null) {
            condition = condition.and(TASKS.SPRINT_ID.eq(request.getSprintId()));
        }
        if (request.getStatus() != null) {
            condition = condition.and(TASKS.STATUS.eq(request.getStatus()));
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

        List<TasksRecord> items = dsl.selectFrom(TASKS)
                .where(condition)
                .orderBy(TASKS.CREATED_AT.desc())
                .limit(size)
                .offset((long) page * size)
                .fetch();

        return new PaginationResult<>(total, items);
    }
}
