package fpt.qn.pms.worklog.service;

import java.util.UUID;

import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.worklog.dto.CreateWorklogRequest;
import fpt.qn.pms.worklog.dto.UpdateWorklogRequest;
import fpt.qn.pms.worklog.dto.WorklogDto;
import fpt.qn.pms.worklog.dto.WorklogReportFilterDto;
import fpt.qn.pms.worklog.dto.WorklogReportItem;

public interface WorklogService {

    PageResponse<WorklogDto> getWorklogsByTask(UUID taskId, int page, int size);

    WorklogDto createWorklog(UUID taskId, CreateWorklogRequest request);

    WorklogDto updateWorklog(UUID id, UpdateWorklogRequest request);

    void deleteWorklog(UUID id);

    PageResponse<WorklogReportItem> getWorklogReport(WorklogReportFilterDto filter);
}
