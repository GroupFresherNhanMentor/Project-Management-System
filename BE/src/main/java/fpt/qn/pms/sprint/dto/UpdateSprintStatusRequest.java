package fpt.qn.pms.sprint.dto;

import fpt.qn.pms.jooq.enums.SprintStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateSprintStatusRequest {

    @NotNull
    SprintStatus status;
}
