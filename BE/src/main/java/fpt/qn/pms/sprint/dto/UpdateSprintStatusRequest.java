package fpt.qn.pms.sprint.dto;

import fpt.qn.pms.jooq.enums.SprintStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Request body to update sprint status")
public class UpdateSprintStatusRequest {

    @NotNull
    @Schema(description = "New status", example = "ACTIVE")
    SprintStatus status;
}
