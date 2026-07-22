package fpt.qn.pms.task.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import fpt.qn.pms.jooq.tables.records.TaskWorkflowRecord;
import fpt.qn.pms.task.dto.CreateTaskWorkflowRequest;
import fpt.qn.pms.task.dto.TaskWorkflowDto;

@Mapper(componentModel = "spring")
public interface TaskWorkflowMapper {

    TaskWorkflowDto toDto(TaskWorkflowRecord record);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    TaskWorkflowRecord toRecord(CreateTaskWorkflowRequest request);
}
