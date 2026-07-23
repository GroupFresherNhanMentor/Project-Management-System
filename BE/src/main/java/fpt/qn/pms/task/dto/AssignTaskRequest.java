package fpt.qn.pms.task.dto;

import java.util.UUID;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignTaskRequest {

    UUID assigneeId; // null means unassign
}
