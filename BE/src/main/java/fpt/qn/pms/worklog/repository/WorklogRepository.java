package fpt.qn.pms.worklog.repository;

import java.util.Optional;
import java.util.UUID;

import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.repository.Repository;
import fpt.qn.pms.jooq.tables.records.WorklogsRecord;
import fpt.qn.pms.worklog.dto.WorklogDto;
import fpt.qn.pms.worklog.dto.WorklogReportFilterDto;
import fpt.qn.pms.worklog.dto.WorklogReportItem;

public interface WorklogRepository extends Repository<WorklogsRecord> {

    PaginationResult<WorklogDto> findByTaskId(UUID taskId, int page, int size);

    Optional<WorklogDto> findDtoById(UUID id);

    PaginationResult<WorklogReportItem> getWorklogReport(WorklogReportFilterDto filter);
}
