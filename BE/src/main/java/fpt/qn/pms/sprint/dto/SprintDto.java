package fpt.qn.pms.sprint.dto;

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
@Schema(description = "Sprint response data")
public class SprintDto {
    @Schema(description = "Sprint ID", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    UUID id;

    @Schema(description = "Project ID", example = "b2c3d4e5-f6a7-8901-bcde-f12345678901")
    UUID projectId;

    @Schema(description = "Sprint name", example = "Sprint 1")
    String sprintName;

    @Schema(description = "Sprint goal", example = "Complete user authentication")
    String goal;

    @Schema(description = "Start date", example = "2026-07-20")
    LocalDate startDate;

    @Schema(description = "End date", example = "2026-08-02")
    LocalDate endDate;

    @Schema(description = "Sprint status", example = "ACTIVE")
    String status;

    @Schema(description = "Creation timestamp", example = "2026-07-20T10:30:00+07:00")
    OffsetDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2026-07-20T10:30:00+07:00")
    OffsetDateTime updatedAt;
}
