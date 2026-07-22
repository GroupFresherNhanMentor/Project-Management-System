package fpt.qn.pms.task.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import fpt.qn.pms.jooq.enums.TaskPriority;
import fpt.qn.pms.jooq.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateTaskRequest {

    @NotNull(message = "Task key is required")
    String taskKey;

    @NotNull(message = "Project ID is required")
    UUID projectId;

    UUID sprintId;

    @NotBlank(message = "Summary is required")
    String summary;

    String description;

    @NotNull(message = "Task type is required")
    TaskType taskType;

    @NotNull(message = "Task priority is required")
    TaskPriority priority;

    //người được phân công 
    UUID assigneeId;

    BigDecimal storyPoint;

    BigDecimal estimateHour;

    LocalDate dueDate;
}
