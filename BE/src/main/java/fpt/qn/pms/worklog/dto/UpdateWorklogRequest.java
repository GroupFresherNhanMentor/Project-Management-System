package fpt.qn.pms.worklog.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import fpt.qn.pms.worklog.validator.ValidWorklogHour;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateWorklogRequest {

    @NotNull(message = "Work date is required")
    LocalDate workDate;

    @NotNull(message = "Hour is required")
    @ValidWorklogHour
    BigDecimal hour;

    String description;
}
