package fpt.qn.pms.projectmember.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Schema(description = "Project member response data")
public class ProjectMemberDto {

    UUID id;
    UUID projectId;
    UUID userId;
    String employeeId;
    String userFullName;
    String email;
    String projectRole;
    String status;
    OffsetDateTime createdAt;
}
