package fpt.qn.pms.worklog.dto;

import java.time.LocalDate;
import java.util.UUID;

import fpt.qn.pms.common.dto.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorklogReportFilterDto extends PageRequest {

    @Schema(description = "Project ID")
    UUID project;

    @Schema(description = "User ID")
    UUID user;

    @Schema(description = "Filter from date (YYYY-MM-DD)")
    LocalDate fromDate;

    @Schema(description = "Filter to date (YYYY-MM-DD)")
    LocalDate toDate;
}
