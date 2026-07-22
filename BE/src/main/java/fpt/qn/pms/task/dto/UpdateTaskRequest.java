package fpt.qn.pms.task.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateTaskRequest {

    UUID statusId;

    String description;

    BigDecimal estimateHour;

    LocalDate dueDate;
}
