package fpt.qn.pms.dashboard.service;

import java.util.UUID;
import fpt.qn.pms.dashboard.dto.DashboardPersonalResponse;
import fpt.qn.pms.dashboard.dto.DashboardProjectResponse;

public interface DashboardService {
    DashboardPersonalResponse getPersonalDashboard(String username);
    DashboardProjectResponse getProjectDashboard(UUID projectId);
}
