package fpt.qn.pms.task.dto;

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
public class TaskStatusDto {
    UUID id;
    UUID projectId;
    String name;
    String color;
    Boolean isInitial;
    Boolean isFinal;
    Boolean isActive;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
