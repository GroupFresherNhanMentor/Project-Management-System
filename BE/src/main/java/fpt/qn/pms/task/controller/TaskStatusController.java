package fpt.qn.pms.task.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.pms.common.dto.ApiResponse;
import fpt.qn.pms.task.dto.CreateTaskStatusRequest;
import fpt.qn.pms.task.dto.TaskStatusDto;
import fpt.qn.pms.task.dto.UpdateTaskStatusRequest;
import fpt.qn.pms.task.service.TaskStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/projects/{projectId}/task-statuses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Task Statuses", description = "Endpoints for managing per-project task status definitions")
public class TaskStatusController {

    TaskStatusService taskStatusService;

    @GetMapping
    @Operation(summary = "Get all task statuses for a project", description = "Optionally filter by isInitial and/or isActive. Omit parameters to return all statuses.")
    public ResponseEntity<ApiResponse<List<TaskStatusDto>>> getByProject(
            @PathVariable UUID projectId,
            @RequestParam(required = false) Boolean isInitial,
            @RequestParam(required = false) Boolean isActive) {
        return ResponseEntity.ok(ApiResponse.success(taskStatusService.getByProject(projectId, isInitial, isActive), null));
    }

    @PostMapping
    @Operation(summary = "Create a new task status", description = "Adds a custom status to the project's workflow.")
    public ResponseEntity<ApiResponse<TaskStatusDto>> create(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTaskStatusRequest request) {
        TaskStatusDto created = taskStatusService.create(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Task status created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task status")
    public ResponseEntity<ApiResponse<TaskStatusDto>> update(
            @PathVariable UUID projectId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(taskStatusService.update(projectId, id, request), null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task status", description = "Removes the status from the project. Fails if tasks are still using it.")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID projectId,
            @PathVariable UUID id) {
        taskStatusService.delete(projectId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Task status deleted successfully"));
    }
}
