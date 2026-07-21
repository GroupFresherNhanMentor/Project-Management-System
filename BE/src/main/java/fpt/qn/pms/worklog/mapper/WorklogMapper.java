package fpt.qn.pms.worklog.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import fpt.qn.pms.jooq.tables.records.WorklogsRecord;
import fpt.qn.pms.worklog.dto.CreateWorklogRequest;
import fpt.qn.pms.worklog.dto.WorklogDto;

@Mapper(componentModel = "spring")
public interface WorklogMapper {

    @Mapping(target = "hour", source = "hours")
    @Mapping(target = "createdBy", ignore = true)
    WorklogDto toDto(WorklogsRecord record);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "taskId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "hours", source = "hour")
    @Mapping(target = "createdAt", ignore = true)
    WorklogsRecord toRecord(CreateWorklogRequest request);
}
