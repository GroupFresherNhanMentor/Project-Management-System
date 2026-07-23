package fpt.qn.pms.dashboard.dto;

import java.math.BigDecimal;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardAdminResponse {
    long totalUsers;
    long activeUsers;
    long lockedUsers;
    long totalProjects;
    long activeProjects;
    long totalTasks;
    BigDecimal totalLoggedHours;
    Map<String, Integer> projectByStatus;
    Map<String, Integer> taskByType;
    Map<String, Integer> userByRole;
}
