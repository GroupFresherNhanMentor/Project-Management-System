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
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActivityController {

    ActivityService activityService;

    @GetMapping("/{taskId}/activities")
    @PreAuthorize("@projectSecurityEvaluator.hasAccessToTask(#taskId) or hasRole('ADMIN')")
    public ApiResponse<PageResponse<TaskActivityDto>> getActivitiesByTaskId(
            @PathVariable UUID taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<TaskActivityDto> response = activityService.getActivitiesByTaskId(taskId, page, size);
        return ApiResponse.success(response, "Retrieve task activity history successfully");
    }
}
