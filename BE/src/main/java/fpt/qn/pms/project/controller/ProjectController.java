package fpt.qn.pms.project.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.pms.common.dto.ApiResponse;
import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.jooq.enums.ProjectStatus;
import fpt.qn.pms.project.dto.request.CreateProjectRequest;
import fpt.qn.pms.project.dto.request.UpdateProjectRequest;
import fpt.qn.pms.project.dto.response.ProjectDto;
import fpt.qn.pms.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@Tag(name = "Project", description = "Project management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    ProjectService projectService;

    @GetMapping
    @Operation(summary = "Get projects", description = "ADMIN sees all projects; USER sees active memberships only")
    public ResponseEntity<ApiResponse<PageResponse<ProjectDto>>> getProjects(
            @Parameter(description = "Matches project code or name") @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        PageResponse<ProjectDto> result = projectService.getProjects(keyword, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(result, null));
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "Get project details", description = "Available to ADMIN or an active project member")
    public ResponseEntity<ApiResponse<ProjectDto>> getProjectById(@PathVariable UUID projectId) {
        return ResponseEntity.ok(ApiResponse.success(projectService.getProjectById(projectId), null));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Create project", description = "System role ADMIN only")
    public ResponseEntity<ApiResponse<ProjectDto>> createProject(
            @Valid @RequestBody CreateProjectRequest request) {
        ProjectDto created = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Project created successfully"));
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update project", description = "System role ADMIN only; project code is immutable")
    public ResponseEntity<ApiResponse<ProjectDto>> updateProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                projectService.updateProject(projectId, request), "Project updated successfully"));
    }
}
