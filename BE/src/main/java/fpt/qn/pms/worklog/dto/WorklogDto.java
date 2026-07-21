package fpt.qn.pms.worklog.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorklogDto {
    UUID id;
    UUID taskId;
    LocalDate workDate;
    BigDecimal hour;
    String description;
    String createdBy;
}
