package fpt.qn.pms.projectmember.mapper;

import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import fpt.qn.pms.jooq.tables.records.ProjectMembersRecord;
import fpt.qn.pms.projectmember.dto.request.AddProjectMemberRequest;
import fpt.qn.pms.projectmember.dto.response.ProjectMemberDto;
import fpt.qn.pms.projectmember.repository.projection.ProjectMemberDetails;

@Mapper(componentModel = "spring")
public interface ProjectMemberMapper {

    @Mapping(target = "projectRole", expression = "java(details.projectRole().getLiteral())")
    @Mapping(target = "status", expression = "java(details.status().getLiteral())")
    ProjectMemberDto toDto(ProjectMemberDetails details);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projectId", source = "projectId")
    @Mapping(target = "userId", source = "request.userId")
    @Mapping(target = "projectRole", source = "request.projectRole")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProjectMembersRecord toRecord(UUID projectId, AddProjectMemberRequest request);
}
