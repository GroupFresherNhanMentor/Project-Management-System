package fpt.qn.pms.task.controller;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.pms.common.dto.ApiResponse;
import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.task.dto.AssignTaskRequest;
import fpt.qn.pms.task.dto.CreateTaskRequest;
import fpt.qn.pms.task.dto.TaskDto;
import fpt.qn.pms.task.dto.TaskSearchRequest;
import fpt.qn.pms.task.dto.UpdateTaskRequest;
import fpt.qn.pms.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Tasks", description = "Endpoints for managing project tasks (FR-TASK)")
public class TaskController {

    TaskService taskService;

    @PostMapping
    @PreAuthorize("@projectSecurityEvaluator.requireRole(#request.projectId, {T(fpt.qn.pms.jooq.enums.ProjectRole).PM}) or hasAuthority('ADMIN')")
    @Operation(summary = "Create a new Task", description = "Requires PM role in project. Sets status to the project's initial status.")
    public ResponseEntity<ApiResponse<TaskDto>> createTask(@Valid @RequestBody CreateTaskRequest request) {
        TaskDto created = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Task created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@projectSecurityEvaluator.hasAccessToTask(#id) or hasAuthority('ADMIN')")
    @Operation(summary = "Get Task details by ID")
    public ResponseEntity<ApiResponse<TaskDto>> getTaskById(@PathVariable UUID id) {
        TaskDto task = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success(task, null));
    }

    @GetMapping("/search")
    @PreAuthorize("@projectSecurityEvaluator.isMember(#request.projectId) or hasAuthority('ADMIN')")
    @Operation(summary = "Search Tasks with pagination and multi-criteria filters")
    public ResponseEntity<ApiResponse<PageResponse<TaskDto>>> searchTasks(
            @Valid @ParameterObject TaskSearchRequest request) {
        PageResponse<TaskDto> result = taskService.searchTasks(request);
        return ResponseEntity.ok(ApiResponse.success(result, null));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@projectSecurityEvaluator.hasAccessToTask(#id) or hasAuthority('ADMIN')")
    @Operation(summary = "Update Task information", description = "PM can update all fields and bypass workflow; members are subject to workflow restrictions.")
    public ResponseEntity<ApiResponse<TaskDto>> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request) {
        TaskDto updated = taskService.updateTask(id, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Task updated successfully"));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("@projectSecurityEvaluator.isPmOfTask(#id) or hasAuthority('ADMIN')")
    @Operation(summary = "Assign Task to project member", description = "Requires PM role in project.")
    public ResponseEntity<ApiResponse<TaskDto>> assignTask(
            @PathVariable UUID id,
            @Valid @RequestBody AssignTaskRequest request) {
        TaskDto assigned = taskService.assignTask(id, request);
        return ResponseEntity.ok(ApiResponse.success(assigned, "Task assigned successfully"));
    }
}
