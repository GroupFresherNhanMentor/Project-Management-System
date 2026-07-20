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
import fpt.qn.pms.jooq.enums.TaskPriority;
import fpt.qn.pms.jooq.enums.TaskStatus;
import fpt.qn.pms.jooq.tables.records.TasksRecord;

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
    public PaginationResult<TasksRecord> findAll(UUID projectId, UUID sprintId, TaskStatus status,
                                                 TaskPriority priority, UUID assigneeId, String keyword,
                                                 int page, int size) {
        Condition condition = DSL.noCondition();

        if (projectId != null) {
            condition = condition.and(TASKS.PROJECT_ID.eq(projectId));
        }
        if (sprintId != null) {
            condition = condition.and(TASKS.SPRINT_ID.eq(sprintId));
        }
        if (status != null) {
            condition = condition.and(TASKS.STATUS.eq(status));
        }
        if (priority != null) {
            condition = condition.and(TASKS.PRIORITY.eq(priority));
        }
        if (assigneeId != null) {
            condition = condition.and(TASKS.ASSIGNEE_ID.eq(assigneeId));
        }
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.toLowerCase() + "%";
            condition = condition.and(
                    TASKS.TASK_KEY.likeIgnoreCase(pattern)
                            .or(TASKS.SUMMARY.likeIgnoreCase(pattern))
            );
        }

        long total = dsl.fetchCount(TASKS, condition);

        List<TasksRecord> items = dsl.selectFrom(TASKS)
                .where(condition)
                .orderBy(TASKS.CREATED_AT.desc())
                .limit(size)
                .offset((long) page * size)
                .fetch();

        return new PaginationResult<>(total, items);
    }
}
