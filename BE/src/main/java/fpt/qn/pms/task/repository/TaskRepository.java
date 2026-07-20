package fpt.qn.pms.task.repository;

import java.util.UUID;

import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.repository.Repository;
import fpt.qn.pms.jooq.enums.TaskPriority;
import fpt.qn.pms.jooq.enums.TaskStatus;
import fpt.qn.pms.jooq.tables.records.TasksRecord;

public interface TaskRepository extends Repository<TasksRecord> {

    int getNextTaskNumber(UUID projectId);

    PaginationResult<TasksRecord> findAll(UUID projectId, UUID sprintId, TaskStatus status, TaskPriority priority, UUID assigneeId, String keyword, int page, int size);
}
