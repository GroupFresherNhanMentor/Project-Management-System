package fpt.qn.pms.activity.repository;

import java.util.UUID;

import fpt.qn.pms.activity.dto.TaskActivityDto;
import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.repository.Repository;
import fpt.qn.pms.jooq.tables.records.TaskActivitiesRecord;

public interface ActivityRepository extends Repository<TaskActivitiesRecord> {
    PaginationResult<TaskActivityDto> findByTaskId(UUID taskId, int page, int size);
}
