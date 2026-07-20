package fpt.qn.pms.task.repository;

import java.util.UUID;

import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.repository.Repository;
import fpt.qn.pms.jooq.tables.records.TasksRecord;
import fpt.qn.pms.task.dto.TaskSearchRequest;

public interface TaskRepository extends Repository<TasksRecord> {

    int getNextTaskNumber(UUID projectId);

    PaginationResult<TasksRecord> findAll(TaskSearchRequest request);
}
