package fpt.qn.pms.dashboard.dto;

import java.math.BigDecimal;
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
public class DashboardPersonalResponse {
    long myOpenTasks;
    long myCompletedTasks;
    long myOverdueTasks;
    BigDecimal totalLoggedHours;
}
