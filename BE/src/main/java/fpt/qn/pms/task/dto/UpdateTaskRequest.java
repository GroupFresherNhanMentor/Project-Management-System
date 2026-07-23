package fpt.qn.pms.task.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import fpt.qn.pms.jooq.enums.TaskPriority;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateTaskRequest {

    UUID statusId;

    TaskPriority priority;

    String description;

    BigDecimal estimateHour;

    LocalDate dueDate;
}
