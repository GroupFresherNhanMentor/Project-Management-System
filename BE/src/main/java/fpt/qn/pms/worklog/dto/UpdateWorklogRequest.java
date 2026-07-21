package fpt.qn.pms.worklog.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
    @DecimalMin(value = "0.01", message = "Hour must be greater than 0")
    @DecimalMax(value = "24.0", message = "Hour must be less than or equal to 24")
    BigDecimal hour;

    String description;
}
