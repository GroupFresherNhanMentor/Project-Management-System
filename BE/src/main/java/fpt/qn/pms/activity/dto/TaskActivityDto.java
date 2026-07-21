package fpt.qn.pms.activity.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import fpt.qn.pms.jooq.enums.ActivityAction;
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
public class TaskActivityDto {
    UUID id;
    UUID taskId;
    UUID userId;
    String userName;
    ActivityAction action;
    String oldValue;
    String newValue;
    OffsetDateTime createdTime;
}
