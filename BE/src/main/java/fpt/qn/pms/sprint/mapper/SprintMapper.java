package fpt.qn.pms.sprint.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import fpt.qn.pms.jooq.tables.records.SprintsRecord;
import fpt.qn.pms.sprint.dto.CreateSprintRequest;
import fpt.qn.pms.sprint.dto.SprintDto;
import fpt.qn.pms.sprint.dto.UpdateSprintRequest;

@Mapper(componentModel = "spring")
public interface SprintMapper {

    @Mapping(target = "status", expression = "java(record.getStatus().getLiteral())")
    SprintDto toDto(SprintsRecord record);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    SprintsRecord toRecord(CreateSprintRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateRecord(@MappingTarget SprintsRecord record, UpdateSprintRequest request);
}
