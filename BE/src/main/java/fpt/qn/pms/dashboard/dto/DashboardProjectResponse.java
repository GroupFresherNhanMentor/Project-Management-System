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
public class DashboardProjectResponse {
    long totalTasks;
    Map<String, Integer> taskByStatus;
    Map<String, Integer> taskByPriority;
    BigDecimal totalLoggedHours;
    SprintProgressDto sprintProgress;
}
