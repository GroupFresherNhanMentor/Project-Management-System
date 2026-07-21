package fpt.qn.pms.worklog.controller;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.pms.common.dto.ApiResponse;
import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.worklog.dto.WorklogReportFilterDto;
import fpt.qn.pms.worklog.dto.WorklogReportItem;
import fpt.qn.pms.worklog.service.WorklogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Reports", description = "Endpoints for generating system reports (FR-WLOG-04)")
public class WorklogReportController {

    WorklogService worklogService;

    @GetMapping("/worklog")
    @Operation(summary = "Get Worklog Report (FR-WLOG-04)")
    public ResponseEntity<ApiResponse<PageResponse<WorklogReportItem>>> getWorklogReport(
            @RequestParam(required = false) UUID project,
            @RequestParam(required = false) UUID user,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        WorklogReportFilterDto filter = new WorklogReportFilterDto();
        filter.setProject(project);
        filter.setUser(user);
        filter.setFromDate(fromDate);
        filter.setToDate(toDate);
        filter.setPage(page);
        filter.setSize(size);

        PageResponse<WorklogReportItem> result = worklogService.getWorklogReport(filter);
        return ResponseEntity.ok(ApiResponse.success(result, null));
    }
}
