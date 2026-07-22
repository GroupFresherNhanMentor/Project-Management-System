package fpt.qn.pms.projectmember.dto.request;

import java.util.UUID;

import fpt.qn.pms.jooq.enums.ProjectRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Request body for adding a user to a project")
public class AddProjectMemberRequest {

    @NotNull(message = "User ID is required")
    @Schema(description = "User ID", example = "00000000-0000-0000-0000-000000000002")
    UUID userId;

    @NotNull(message = "Project role is required")
    @Schema(description = "Role within the project", example = "DEV", allowableValues = {"PM", "DEV", "TESTER"})
    ProjectRole projectRole;
}
