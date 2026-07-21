package fpt.qn.pms.sprint.dto;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Request body to update a sprint (partial — null fields are ignored)")
public class UpdateSprintRequest {

    @Size(max = 200)
    @Schema(description = "Sprint name", example = "Sprint 1 Updated", maxLength = 200)
    String sprintName;

    @Size(max = 10000)
    @Schema(description = "Sprint goal", example = "Updated goal", maxLength = 10000)
    String goal;

    @Schema(description = "Start date", example = "2026-07-21")
    LocalDate startDate;

    @Schema(description = "End date", example = "2026-08-03")
    LocalDate endDate;
}
