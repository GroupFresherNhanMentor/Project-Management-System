package fpt.qn.pms.task.dto;

import java.util.UUID;

import fpt.qn.pms.common.dto.PageRequest;
import fpt.qn.pms.jooq.enums.TaskPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskSearchRequest extends PageRequest {

    @Schema(description = "ID của dự án")
    UUID projectId;

    @Schema(description = "ID của sprint")
    UUID sprintId;

    @Schema(description = "ID của trạng thái task (UUID từ bảng task_statuses)")
    UUID statusId;

    @Schema(description = "Độ ưu tiên task (LOW, MEDIUM, HIGH, URGENT)")
    TaskPriority priority;

    @Schema(description = "ID của người được gán task")
    UUID assigneeId;

    @Schema(description = "Từ khóa tìm kiếm (lọc không phân biệt hoa/thường theo các trường: taskKey, summary)", example = "WEB-1")
    String keyword;
}
