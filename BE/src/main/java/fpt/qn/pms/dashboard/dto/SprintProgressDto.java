package fpt.qn.pms.dashboard.dto;

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
public class SprintProgressDto {
    UUID sprintId;
    String sprintName;
    long totalTasks;
    long doneTasks;
    double percentComplete;
}
