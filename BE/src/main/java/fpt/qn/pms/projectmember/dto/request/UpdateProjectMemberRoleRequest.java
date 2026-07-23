package fpt.qn.pms.projectmember.dto.request;

import fpt.qn.pms.jooq.enums.ProjectRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Request body for changing a project member's role")
public class UpdateProjectMemberRoleRequest {

    @NotNull(message = "Project role is required")
    @Schema(description = "New role within the project", example = "DEV", allowableValues = {"PM", "DEV", "TESTER"})
    ProjectRole projectRole;
}
