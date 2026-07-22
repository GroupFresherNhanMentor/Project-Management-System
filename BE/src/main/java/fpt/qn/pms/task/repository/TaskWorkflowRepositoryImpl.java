package fpt.qn.pms.task.repository;

import static fpt.qn.pms.jooq.Tables.TASK_STATUSES;
import static fpt.qn.pms.jooq.Tables.TASK_WORKFLOW;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import fpt.qn.pms.common.repository.BaseRepository;
import fpt.qn.pms.jooq.tables.records.TaskWorkflowRecord;

@Repository
public class TaskWorkflowRepositoryImpl extends BaseRepository<TaskWorkflowRecord> implements TaskWorkflowRepository {

    public TaskWorkflowRepositoryImpl(DSLContext dsl) {
        super(dsl, TASK_WORKFLOW);
    }

    @Override
    public List<TaskWorkflowRecord> findAllByProjectId(UUID projectId) {
        return dsl.selectFrom(TASK_WORKFLOW)
                .where(TASK_WORKFLOW.FROM_STATUS_ID.in(
                        dsl.select(TASK_STATUSES.ID)
                                .from(TASK_STATUSES)
                                .where(TASK_STATUSES.PROJECT_ID.eq(projectId))))
                .orderBy(TASK_WORKFLOW.CREATED_AT.asc())
                .fetch();
    }

    @Override
    public boolean existsByFromStatusIdAndToStatusId(UUID fromStatusId, UUID toStatusId) {
        return dsl.fetchExists(TASK_WORKFLOW,
                TASK_WORKFLOW.FROM_STATUS_ID.eq(fromStatusId).and(TASK_WORKFLOW.TO_STATUS_ID.eq(toStatusId)));
    }

    @Override
    public List<TaskWorkflowRecord> createAll(List<TaskWorkflowRecord> records) {
        if (records.isEmpty()) {
            return List.of();
        }
        var step = dsl.insertInto(TASK_WORKFLOW, TASK_WORKFLOW.FROM_STATUS_ID, TASK_WORKFLOW.TO_STATUS_ID)
                .values(records.get(0).getFromStatusId(), records.get(0).getToStatusId());
        for (int i = 1; i < records.size(); i++) {
            step = step.values(records.get(i).getFromStatusId(), records.get(i).getToStatusId());
        }
        return step.returning().fetch().into(TaskWorkflowRecord.class);
    }

    @Override
    public Optional<TaskWorkflowRecord> findByIdAndProjectId(UUID id, UUID projectId) {
        return dsl.selectFrom(TASK_WORKFLOW)
                .where(TASK_WORKFLOW.ID.eq(id))
                .and(TASK_WORKFLOW.FROM_STATUS_ID.in(
                        dsl.select(TASK_STATUSES.ID)
                                .from(TASK_STATUSES)
                                .where(TASK_STATUSES.PROJECT_ID.eq(projectId))))
                .fetchOptional();
    }
}
