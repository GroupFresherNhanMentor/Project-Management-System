package fpt.qn.pms.task.repository.resultModel;

import fpt.qn.pms.jooq.tables.records.TasksRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TaskResult {
    TasksRecord task;
    String assigneeName;
    String reporterName;
    String statusName;
    String statusColor;
}
