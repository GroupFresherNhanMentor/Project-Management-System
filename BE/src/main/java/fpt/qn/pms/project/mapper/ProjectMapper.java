package fpt.qn.pms.project.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import fpt.qn.pms.jooq.tables.records.ProjectsRecord;
import fpt.qn.pms.project.dto.request.CreateProjectRequest;
import fpt.qn.pms.project.dto.request.UpdateProjectRequest;
import fpt.qn.pms.project.dto.response.ProjectDto;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "status", expression = "java(record.getStatus().getLiteral())")
    ProjectDto toDto(ProjectsRecord record);

    List<ProjectDto> toDtoList(List<ProjectsRecord> records);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    ProjectsRecord toRecord(CreateProjectRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projectCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateRecord(@MappingTarget ProjectsRecord record, UpdateProjectRequest request);
}
