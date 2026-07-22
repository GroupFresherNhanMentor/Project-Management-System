package fpt.qn.pms.project.dto.response;

import java.time.LocalDate;
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
@Schema(description = "Project response data")
public class ProjectDto {

    UUID id;
    String projectCode;
    String projectName;
    String description;
    LocalDate startDate;
    LocalDate endDate;
    String status;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
