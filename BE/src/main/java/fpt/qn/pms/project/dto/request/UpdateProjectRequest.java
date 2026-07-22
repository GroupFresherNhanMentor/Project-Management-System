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
@Schema(description = "Request body to update a project. Project code is immutable.")
public class UpdateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 200, message = "Project name must not exceed 200 characters")
    String projectName;

    @Size(max = 10000, message = "Description must not exceed 10000 characters")
    String description;

    @NotNull(message = "Start date is required")
    LocalDate startDate;

    @NotNull(message = "End date is required")
    LocalDate endDate;

    @NotNull(message = "Project status is required")
    ProjectStatus status;
}
