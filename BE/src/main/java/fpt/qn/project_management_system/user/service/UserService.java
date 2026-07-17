package fpt.qn.project_management_system.user.service;

import java.util.UUID;

import fpt.qn.project_management_system.common.dto.PageResponse;
import fpt.qn.project_management_system.jooq.enums.SysRole;
import fpt.qn.project_management_system.jooq.enums.UserStatus;
import fpt.qn.project_management_system.user.dto.CreateUserRequest;
import fpt.qn.project_management_system.user.dto.UpdateUserRequest;
import fpt.qn.project_management_system.user.dto.UpdateUserStatusRequest;
import fpt.qn.project_management_system.user.dto.UserDto;

public interface UserService {

    PageResponse<UserDto> getUsers(String keyword, SysRole role, UserStatus status, int page, int size);

    UserDto getUserById(UUID id);

    UserDto createUser(CreateUserRequest request);

    UserDto updateUser(UUID id, UpdateUserRequest request);

    UserDto updateUserStatus(UUID id, UpdateUserStatusRequest request);
}
