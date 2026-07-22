package fpt.qn.pms.task.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateTaskWorkflowRequest {

    @NotNull
    UUID fromStatusId;

    @NotNull
    UUID toStatusId;
}
