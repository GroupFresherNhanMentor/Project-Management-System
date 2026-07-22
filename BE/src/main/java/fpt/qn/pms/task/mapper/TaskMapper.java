package fpt.qn.pms.task.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import fpt.qn.pms.jooq.tables.records.TasksRecord;
import fpt.qn.pms.task.dto.CreateTaskRequest;
import fpt.qn.pms.task.dto.TaskDto;
import fpt.qn.pms.task.repository.resultModel.TaskResult;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "id", source = "task.id")
    @Mapping(target = "taskKey", source = "task.taskKey")
    @Mapping(target = "projectId", source = "task.projectId")
    @Mapping(target = "sprintId", source = "task.sprintId")
    @Mapping(target = "summary", source = "task.summary")
    @Mapping(target = "description", source = "task.description")
    @Mapping(target = "taskType", expression = "java(result.getTask().getTaskType() != null ? result.getTask().getTaskType().getLiteral() : null)")
    @Mapping(target = "priority", expression = "java(result.getTask().getPriority() != null ? result.getTask().getPriority().getLiteral() : null)")
    @Mapping(target = "statusId", source = "task.statusId")
    @Mapping(target = "assigneeId", source = "task.assigneeId")
    @Mapping(target = "reporterId", source = "task.reporterId")
    @Mapping(target = "storyPoint", source = "task.storyPoint")
    @Mapping(target = "estimateHour", source = "task.estimateHour")
    @Mapping(target = "dueDate", source = "task.dueDate")
    @Mapping(target = "createdAt", source = "task.createdAt")
    @Mapping(target = "assigneeName", source = "assigneeName")
    @Mapping(target = "reporterName", source = "reporterName")
    @Mapping(target = "statusName", source = "statusName")
    @Mapping(target = "statusColor", source = "statusColor")
    TaskDto toDto(TaskResult result);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "taskKey", ignore = true)
    @Mapping(target = "statusId", ignore = true)
    @Mapping(target = "reporterId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    TasksRecord toRecord(CreateTaskRequest request);
}
