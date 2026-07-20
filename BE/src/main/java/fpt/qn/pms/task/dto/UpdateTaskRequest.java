package fpt.qn.pms.task.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import fpt.qn.pms.jooq.enums.TaskStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateTaskRequest {

    TaskStatus status;

    String description;

    BigDecimal estimateHour;

    LocalDate dueDate;
}
