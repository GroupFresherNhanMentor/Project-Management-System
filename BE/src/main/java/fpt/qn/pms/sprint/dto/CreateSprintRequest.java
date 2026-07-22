package fpt.qn.pms.sprint.dto;

import java.time.LocalDate;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Request body to create a new sprint")
public class CreateSprintRequest {

    @Schema(hidden = true)
    UUID projectId;

    @NotBlank
    @Size(max = 200)
    @Schema(description = "Sprint name", example = "Sprint 1", maxLength = 200)
    String sprintName;

    @Size(max = 10000)
    @Schema(description = "Sprint goal", example = "Complete user authentication", maxLength = 10000)
    String goal;

    @NotNull
    @Schema(description = "Start date", example = "2026-07-20")
    LocalDate startDate;

    @NotNull
    @Schema(description = "End date", example = "2026-08-02")
    LocalDate endDate;
}
