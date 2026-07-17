package fpt.qn.project_management_system.user.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import fpt.qn.project_management_system.jooq.tables.records.UsersRecord;
import fpt.qn.project_management_system.user.dto.CreateUserRequest;
import fpt.qn.project_management_system.user.dto.UpdateUserRequest;
import fpt.qn.project_management_system.user.dto.UserDto;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", expression = "java(record.getRole().getLiteral())")
    @Mapping(target = "status", expression = "java(record.getStatus().getLiteral())")
    UserDto toDto(UsersRecord record);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UsersRecord toRecord(CreateUserRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateRecord(@MappingTarget UsersRecord record, UpdateUserRequest request);
}
