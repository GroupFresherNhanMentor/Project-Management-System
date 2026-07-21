package fpt.qn.pms.worklog.dto;

import java.time.LocalDate;
import java.util.UUID;

import fpt.qn.pms.common.dto.PageRequest;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorklogReportFilterDto extends PageRequest {
    UUID project;
    UUID user;
    LocalDate fromDate;
    LocalDate toDate;
}
