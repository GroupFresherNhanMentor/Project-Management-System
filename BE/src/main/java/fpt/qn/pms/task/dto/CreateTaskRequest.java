package fpt.qn.pms.task.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import fpt.qn.pms.jooq.enums.TaskPriority;
import fpt.qn.pms.jooq.enums.TaskType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 500, message = "Summary must not exceed 500 characters")
    String summary;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    String description;

    @NotNull(message = "Task type is required")
    TaskType taskType;

    @NotNull(message = "Task priority is required")
    TaskPriority priority;

    @NotNull(message = "Task status is required")
    UUID taskStatusId;

    UUID reporterId;

    UUID assigneeId;

    @DecimalMin(value = "0.01", message = "Story point must be greater than 0")
    @Digits(integer = 3, fraction = 2, message = "Story point must have at most 3 integer digits and 2 decimal places")
    BigDecimal storyPoint;

    @DecimalMin(value = "0.01", message = "Estimate hour must be greater than 0")
    @Digits(integer = 4, fraction = 2, message = "Estimate hour must have at most 4 integer digits and 2 decimal places")
    BigDecimal estimateHour;

    @FutureOrPresent(message = "Due date must be today or in the future")
    LocalDate dueDate;
}
