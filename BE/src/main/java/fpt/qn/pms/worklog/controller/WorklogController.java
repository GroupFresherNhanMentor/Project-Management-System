package fpt.qn.pms.worklog.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.pms.common.dto.ApiResponse;
import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.worklog.dto.CreateWorklogRequest;
import fpt.qn.pms.worklog.dto.UpdateWorklogRequest;
import fpt.qn.pms.worklog.dto.WorklogDto;
import fpt.qn.pms.worklog.service.WorklogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Worklog", description = "Endpoints for managing worklogs (FR-WLOG)")
public class WorklogController {

    WorklogService worklogService;

    @GetMapping("/api/tasks/{taskId}/worklogs")
    @Operation(summary = "Get list of worklogs for a Task")
    public ResponseEntity<ApiResponse<PageResponse<WorklogDto>>> getWorklogsByTask(
            @PathVariable UUID taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<WorklogDto> result = worklogService.getWorklogsByTask(taskId, page, size);
        return ResponseEntity.ok(ApiResponse.success(result, null));
    }

    @PostMapping("/api/tasks/{taskId}/worklogs")
    @Operation(summary = "Log time (Worklog) on a Task", description = "Constraints: Hour > 0 and Hour <= 24")
    public ResponseEntity<ApiResponse<WorklogDto>> createWorklog(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateWorklogRequest request) {
        WorklogDto created = worklogService.createWorklog(taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Worklog created successfully"));
    }

    @PutMapping("/api/worklogs/{id}")
    @Operation(summary = "Update Worklog", description = "Allowed only for the creator of the worklog")
    public ResponseEntity<ApiResponse<WorklogDto>> updateWorklog(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWorklogRequest request) {
        WorklogDto updated = worklogService.updateWorklog(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Worklog updated successfully"));
    }

    @DeleteMapping("/api/worklogs/{id}")
    @Operation(summary = "Delete Worklog record", description = "Allowed only for the creator of the worklog")
    public ResponseEntity<ApiResponse<Void>> deleteWorklog(@PathVariable UUID id) {
        worklogService.deleteWorklog(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Worklog deleted successfully"));
    }
}
