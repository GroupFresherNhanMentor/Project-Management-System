package fpt.qn.pms.worklog.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorklogReportItem {
    UUID id;
    UUID taskId;
    String taskKey;
    String taskSummary;
    UUID userId;
    String userName;
    LocalDate workDate;
    BigDecimal hour;
    String description;
}
