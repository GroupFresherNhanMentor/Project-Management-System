package fpt.qn.pms.sprint.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.pms.common.dto.ApiResponse;
import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.jooq.enums.ProjectRole;
import fpt.qn.pms.jooq.enums.SprintStatus;
import fpt.qn.pms.security.annotation.RequireProjectRole;
import fpt.qn.pms.sprint.dto.CreateSprintRequest;
import fpt.qn.pms.sprint.dto.SprintDto;
import fpt.qn.pms.sprint.dto.UpdateSprintRequest;
import fpt.qn.pms.sprint.dto.UpdateSprintStatusRequest;
import fpt.qn.pms.sprint.service.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/projects/{projectId}/sprints")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@Tag(name = "Sprint", description = "Sprint management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SprintController {

    SprintService sprintService;

    @GetMapping
    @Operation(summary = "Get sprints by project", description = "Retrieve paginated sprints with optional keyword and status filters")
    public ResponseEntity<ApiResponse<PageResponse<SprintDto>>> getSprintsByProject(
            @Parameter(description = "Project ID") @PathVariable UUID projectId,
            @Parameter(description = "Search keyword (matches sprint name)") @RequestParam(required = false) String keyword,
            @Parameter(description = "Filter by status (PLANNED / ACTIVE / CLOSED)") @RequestParam(required = false) SprintStatus status,
            @Parameter(description = "Page number (zero-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        PageResponse<SprintDto> result = sprintService.getSprintsByProject(projectId, keyword, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(result, null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sprint by ID", description = "Retrieve a single sprint by its ID; available to ADMIN or active project member")
    public ResponseEntity<ApiResponse<SprintDto>> getSprintById(
            @Parameter(description = "Project ID") @PathVariable UUID projectId,
            @Parameter(description = "Sprint ID") @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(sprintService.getSprintById(projectId, id), null));
    }

    @PostMapping
    @RequireProjectRole(ProjectRole.PM)
    @Operation(summary = "Create sprint", description = "Create a new sprint (PM only)")
    public ResponseEntity<ApiResponse<SprintDto>> createSprint(
            @Parameter(description = "Project ID") @PathVariable UUID projectId,
            @Valid @RequestBody CreateSprintRequest request) {

        request.setProjectId(projectId);
        SprintDto created = sprintService.createSprint(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Sprint created successfully"));
    }

    @PutMapping("/{id}")
    @RequireProjectRole(ProjectRole.PM)
    @Operation(summary = "Update sprint", description = "Partial update — null fields are ignored (PM only)")
    public ResponseEntity<ApiResponse<SprintDto>> updateSprint(
            @Parameter(description = "Project ID") @PathVariable UUID projectId,
            @Parameter(description = "Sprint ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateSprintRequest request) {

        return ResponseEntity.ok(ApiResponse.success(sprintService.updateSprint(projectId, id, request), null));
    }

    @PatchMapping("/{id}/status")
    @RequireProjectRole(ProjectRole.PM)
    @Operation(summary = "Update sprint status", description = "Transition sprint status: PLANNED → ACTIVE → CLOSED (PM only). Only one ACTIVE sprint per project.")
    public ResponseEntity<ApiResponse<SprintDto>> updateSprintStatus(
            @Parameter(description = "Project ID") @PathVariable UUID projectId,
            @Parameter(description = "Sprint ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateSprintStatusRequest request) {

        return ResponseEntity.ok(ApiResponse.success(sprintService.updateSprintStatus(projectId, id, request), null));
    }
}
