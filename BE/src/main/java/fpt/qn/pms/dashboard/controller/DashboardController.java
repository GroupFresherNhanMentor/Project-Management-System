package fpt.qn.pms.dashboard.controller;

import java.security.Principal;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fpt.qn.pms.common.dto.ApiResponse;
import fpt.qn.pms.dashboard.dto.DashboardPersonalResponse;
import fpt.qn.pms.dashboard.dto.DashboardProjectResponse;
import fpt.qn.pms.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Dashboard", description = "Endpoints for project and personal dashboards (FR-DASH)")
public class DashboardController {

    DashboardService dashboardService;

    @GetMapping("/me")
    public ApiResponse<DashboardPersonalResponse> getPersonalDashboard(Principal principal) {
        DashboardPersonalResponse response = dashboardService.getPersonalDashboard(principal.getName());
        return ApiResponse.success(response, "Retrieve personal dashboard successfully");
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("@projectSecurityEvaluator.isPm(#projectId) or hasRole('ADMIN')")
    public ApiResponse<DashboardProjectResponse> getProjectDashboard(@PathVariable UUID projectId) {
        DashboardProjectResponse response = dashboardService.getProjectDashboard(projectId);
        return ApiResponse.success(response, "Retrieve project dashboard successfully");
    }
}
