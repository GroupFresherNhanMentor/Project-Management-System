package fpt.qn.pms.task.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.pms.common.dto.ApiResponse;
import fpt.qn.pms.task.dto.CreateTaskWorkflowRequest;
import fpt.qn.pms.task.dto.TaskWorkflowDto;
import fpt.qn.pms.task.service.TaskWorkflowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/projects/{projectId}/task-workflow")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Task Workflow", description = "Endpoints for managing allowed status transitions per project")
public class TaskWorkflowController {

    TaskWorkflowService taskWorkflowService;

    @GetMapping
    @Operation(summary = "Get all workflow transitions for a project")
    public ResponseEntity<ApiResponse<List<TaskWorkflowDto>>> getByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success(taskWorkflowService.getByProject(projectId), null));
    }

    @PostMapping
    @Operation(summary = "Create workflow transitions", description = "Defines one or more allowed status transitions within a project in a single request.")
    public ResponseEntity<ApiResponse<List<TaskWorkflowDto>>> create(
            @PathVariable UUID projectId,
            @RequestBody List<@Valid CreateTaskWorkflowRequest> requests) {
        List<TaskWorkflowDto> created = taskWorkflowService.createAll(projectId, requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Workflow transitions created successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a workflow transition")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID projectId,
            @PathVariable UUID id) {
        taskWorkflowService.delete(projectId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Workflow transition deleted successfully"));
    }
}
