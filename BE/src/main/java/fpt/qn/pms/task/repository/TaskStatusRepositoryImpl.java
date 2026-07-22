package fpt.qn.pms.task.repository;

import static fpt.qn.pms.jooq.Tables.TASK_STATUSES;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.pms.common.repository.BaseRepository;
import fpt.qn.pms.jooq.tables.records.TaskStatusesRecord;

@Repository
public class TaskStatusRepositoryImpl extends BaseRepository<TaskStatusesRecord> implements TaskStatusRepository {

    public TaskStatusRepositoryImpl(DSLContext dsl) {
        super(dsl, TASK_STATUSES);
    }

    @Override
    public List<TaskStatusesRecord> findAllByProjectId(UUID projectId, Boolean isInitial, Boolean isActive) {
        Condition condition = TASK_STATUSES.PROJECT_ID.eq(projectId);
        if (isInitial != null) {
            condition = condition.and(TASK_STATUSES.IS_INITIAL.eq(isInitial));
        }
        if (isActive != null) {
            condition = condition.and(TASK_STATUSES.IS_ACTIVE.eq(isActive));
        }
        return dsl.selectFrom(TASK_STATUSES)
                .where(condition)
                .orderBy(TASK_STATUSES.CREATED_AT.asc())
                .fetch();
    }

    @Override
    public boolean existsByProjectIdAndName(UUID projectId, String name) {
        return dsl.fetchExists(TASK_STATUSES,
                TASK_STATUSES.PROJECT_ID.eq(projectId).and(TASK_STATUSES.NAME.equalIgnoreCase(name)));
    }

    @Override
    public boolean existsByProjectIdAndNameAndIdNot(UUID projectId, String name, UUID excludeId) {
        return dsl.fetchExists(TASK_STATUSES,
                TASK_STATUSES.PROJECT_ID.eq(projectId)
                        .and(TASK_STATUSES.NAME.equalIgnoreCase(name))
                        .and(TASK_STATUSES.ID.ne(excludeId)));
    }

    @Override
    public Optional<TaskStatusesRecord> findInitialByProjectId(UUID projectId) {
        return dsl.selectFrom(TASK_STATUSES)
                .where(TASK_STATUSES.PROJECT_ID.eq(projectId))
                .and(TASK_STATUSES.IS_INITIAL.isTrue())
                .and(TASK_STATUSES.IS_ACTIVE.isTrue())
                .fetchOptional();
    }

    @Override
    public void softDeleteById(UUID id) {
        dsl.update(TASK_STATUSES)
                .set(TASK_STATUSES.IS_ACTIVE, false)
                .where(TASK_STATUSES.ID.eq(id))
                .execute();
    }
}
