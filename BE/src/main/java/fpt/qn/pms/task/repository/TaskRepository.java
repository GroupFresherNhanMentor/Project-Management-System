package fpt.qn.pms.task.repository;

import java.util.UUID;

import java.util.Optional;

import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.repository.Repository;
import fpt.qn.pms.jooq.tables.records.TasksRecord;
import fpt.qn.pms.task.dto.TaskSearchRequest;
import fpt.qn.pms.task.repository.resultModel.TaskResult;

public interface TaskRepository extends Repository<TasksRecord> {

    boolean existsAssignedTaskByProjectIdAndAssigneeId(UUID projectId, UUID assigneeId);

    boolean existsByTaskKey(String taskKey);

    Optional<TaskResult> findDetailById(UUID id);

    PaginationResult<TaskResult> findAll(TaskSearchRequest request);
}
