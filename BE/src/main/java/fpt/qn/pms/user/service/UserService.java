package fpt.qn.pms.user.service;

import java.util.UUID;

import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.user.dto.CreateUserRequest;
import fpt.qn.pms.user.dto.UpdateUserRequest;
import fpt.qn.pms.user.dto.UpdateUserStatusRequest;
import fpt.qn.pms.user.dto.UserDto;

public interface UserService {

    PageResponse<UserDto> getUsers(String keyword, SysRole role, UserStatus status, int page, int size);

    UserDto getUserById(UUID id);

    UserDto createUser(CreateUserRequest request);

    UserDto updateUser(UUID id, UpdateUserRequest request);

    UserDto updateUserStatus(UUID id, UpdateUserStatusRequest request);
}
