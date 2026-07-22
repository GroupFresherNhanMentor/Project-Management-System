package fpt.qn.pms.task.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import fpt.qn.pms.common.repository.Repository;
import fpt.qn.pms.jooq.tables.records.TaskWorkflowRecord;

public interface TaskWorkflowRepository extends Repository<TaskWorkflowRecord> {

    List<TaskWorkflowRecord> findAllByProjectId(UUID projectId);

    boolean existsByFromStatusIdAndToStatusId(UUID fromStatusId, UUID toStatusId);

    List<TaskWorkflowRecord> createAll(List<TaskWorkflowRecord> records);

    Optional<TaskWorkflowRecord> findByIdAndProjectId(UUID id, UUID projectId);
}
