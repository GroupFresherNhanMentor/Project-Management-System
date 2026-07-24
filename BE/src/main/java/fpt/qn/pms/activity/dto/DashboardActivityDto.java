package fpt.qn.pms.activity.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import fpt.qn.pms.jooq.enums.ActivityAction;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DashboardActivityDto {
    UUID id;
    UUID taskId;
    String taskKey;
    UUID userId;
    String userName;
    ActivityAction action;
    String message;
    OffsetDateTime createdTime;
}
