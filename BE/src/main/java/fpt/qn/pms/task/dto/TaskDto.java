package fpt.qn.pms.task.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskDto {
    UUID id;
    String taskKey;
    UUID projectId;
    UUID sprintId;
    String summary;
    String description;
    String taskType;
    String priority;
    String status;
    UUID assigneeId;
    String assigneeName;
    UUID reporterId;
    String reporterName;
    BigDecimal storyPoint;
    BigDecimal estimateHour;
    LocalDate dueDate;
    OffsetDateTime createdAt;
}
