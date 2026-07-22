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

    @Schema(description = "ID của dự án")
    UUID projectId;

    @Schema(description = "ID của người dùng")
    UUID userId;

    @Schema(description = "Tên đăng nhập (username) của người dùng")
    String username;

    @Schema(description = "Ngày bắt đầu lọc worklog (YYYY-MM-DD)")
    LocalDate fromDate;

    @Schema(description = "Ngày kết thúc lọc worklog (YYYY-MM-DD)")
    LocalDate toDate;
}
