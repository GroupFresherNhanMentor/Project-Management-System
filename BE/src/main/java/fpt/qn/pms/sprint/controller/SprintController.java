package fpt.qn.pms.sprint.controller;

import java.util.UUID;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.pms.common.dto.ApiResponse;
import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.jooq.enums.SprintStatus;
import fpt.qn.pms.sprint.dto.CreateSprintRequest;
import fpt.qn.pms.sprint.dto.SprintDto;
import fpt.qn.pms.sprint.dto.UpdateSprintRequest;
import fpt.qn.pms.sprint.dto.UpdateSprintStatusRequest;
import fpt.qn.pms.sprint.service.SprintService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/projects/{projectId}/sprints")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SprintController {

    SprintService sprintService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SprintDto>>> getSprintsByProject(
            @PathVariable UUID projectId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) SprintStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageResponse<SprintDto> result = sprintService.getSprintsByProject(projectId, keyword, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(result, null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SprintDto>> getSprintById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(sprintService.getSprintById(id), null));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<SprintDto>> createSprint(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateSprintRequest request) {

        request.setProjectId(projectId);
        SprintDto created = sprintService.createSprint(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Sprint created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<SprintDto>> updateSprint(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSprintRequest request) {

        return ResponseEntity.ok(ApiResponse.success(sprintService.updateSprint(id, request), null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<SprintDto>> updateSprintStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSprintStatusRequest request) {

        return ResponseEntity.ok(ApiResponse.success(sprintService.updateSprintStatus(id, request), null));
    }
}
