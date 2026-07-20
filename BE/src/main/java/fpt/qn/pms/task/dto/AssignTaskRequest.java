package fpt.qn.pms.task.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AssignTaskRequest {

    @NotNull(message = "Assignee ID is required")
    UUID assigneeId;
}
