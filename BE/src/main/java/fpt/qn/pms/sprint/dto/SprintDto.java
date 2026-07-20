package fpt.qn.pms.sprint.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

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
public class SprintDto {
    UUID id;
    UUID projectId;
    String sprintName;
    String goal;
    LocalDate startDate;
    LocalDate endDate;
    String status;
    OffsetDateTime createdAt;
}
