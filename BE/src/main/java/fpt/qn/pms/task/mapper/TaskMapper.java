package fpt.qn.pms.task.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import fpt.qn.pms.jooq.tables.records.TasksRecord;
import fpt.qn.pms.task.dto.CreateTaskRequest;
import fpt.qn.pms.task.dto.TaskDto;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "taskType", expression = "java(record.getTaskType() != null ? record.getTaskType().getLiteral() : null)")
    @Mapping(target = "priority", expression = "java(record.getPriority() != null ? record.getPriority().getLiteral() : null)")
    @Mapping(target = "status", expression = "java(record.getStatus() != null ? record.getStatus().getLiteral() : null)")
    @Mapping(target = "assigneeName", ignore = true)
    @Mapping(target = "reporterName", ignore = true)
    TaskDto toDto(TasksRecord record);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "taskKey", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "reporterId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    TasksRecord toRecord(CreateTaskRequest request);
}
