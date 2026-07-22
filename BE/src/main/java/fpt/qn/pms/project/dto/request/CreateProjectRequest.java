package fpt.qn.pms.project.dto.request;

import java.time.LocalDate;

import fpt.qn.pms.jooq.enums.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Request body to create a project")
public class CreateProjectRequest {

    @NotBlank(message = "Project code is required")
    @Size(max = 20, message = "Project code must not exceed 20 characters")
    @Schema(example = "WEB", maxLength = 20)
    String projectCode;

    @NotBlank(message = "Project name is required")
    @Size(max = 200, message = "Project name must not exceed 200 characters")
    @Schema(example = "Web Portal Project", maxLength = 200)
    String projectName;

    @Size(max = 10000, message = "Description must not exceed 10000 characters")
    String description;

    @NotNull(message = "Start date is required")
    LocalDate startDate;

    @NotNull(message = "End date is required")
    LocalDate endDate;

    @NotNull(message = "Project status is required")
    @Schema(example = "PLANNING", allowableValues = {"PLANNING", "ACTIVE", "ON_HOLD", "COMPLETED"})
    ProjectStatus status;
}
