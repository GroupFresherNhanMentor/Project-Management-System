package fpt.qn.pms.task.dto;

import java.util.UUID;

import fpt.qn.pms.common.dto.PageRequest;
import fpt.qn.pms.jooq.enums.TaskPriority;
import fpt.qn.pms.jooq.enums.TaskStatus;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@Data
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskSearchRequest extends PageRequest {
    UUID project;
    UUID sprint;
    TaskStatus status;
    TaskPriority priority;
    UUID assignee;
    String keyword;
}
