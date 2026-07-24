package fpt.qn.pms.activity.controller;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.pms.activity.dto.TaskActivityDto;
import fpt.qn.pms.activity.service.ActivityService;
import fpt.qn.pms.common.dto.ApiResponse;
import fpt.qn.pms.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import fpt.qn.pms.activity.dto.DashboardActivityDto;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Activity", description = "Endpoints for managing task activity logs (FR-ACT)")
public class ActivityController {

    ActivityService activityService;

    @GetMapping("/{taskId}/activities")
    @Operation(summary = "Get task activities", description = "Retrieve paginated activity logs for a specific task")
    @PreAuthorize("@projectSecurityEvaluator.hasAccessToTask(#taskId) or hasAuthority('ADMIN')")
    public ApiResponse<PageResponse<TaskActivityDto>> getActivitiesByTaskId(
            @PathVariable UUID taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<TaskActivityDto> response = activityService.getActivitiesByTaskId(taskId, page, size);
        return ApiResponse.success(response, "Retrieve task activity history successfully");
    }

    @GetMapping("/activities/recent")
    @Operation(summary = "Get recent activities for dashboard", description = "Retrieve latest activity logs system-wide (for ADMIN) or for a specific project")
    public ApiResponse<List<DashboardActivityDto>> getRecentActivities(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(defaultValue = "10") int limit) {
        List<DashboardActivityDto> response = activityService.getRecentActivities(projectId, limit);
        return ApiResponse.success(response, "Retrieve recent activity history successfully");
    }
}
