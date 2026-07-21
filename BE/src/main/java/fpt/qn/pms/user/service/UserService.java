package fpt.qn.pms.user.service;

import java.util.UUID;

import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.user.dto.request.CreateUserRequest;
import fpt.qn.pms.user.dto.request.UpdateCurrentUserRequest;
import fpt.qn.pms.user.dto.request.UpdateUserRequest;
import fpt.qn.pms.user.dto.request.UpdateUserStatusRequest;
import fpt.qn.pms.user.dto.response.CreateUserResponse;
import fpt.qn.pms.user.dto.response.UserDto;

public interface UserService {

    PageResponse<UserDto> getUsers(String keyword, SysRole role, UserStatus status, int page, int size);

    UserDto getUserById(UUID id);

    CreateUserResponse createUser(CreateUserRequest request);

    UserDto updateUser(UUID id, UpdateUserRequest request);

    UserDto updateUserStatus(UUID id, UpdateUserStatusRequest request);

    UserDto getCurrentUser();

    UserDto updateCurrentUser(UpdateCurrentUserRequest request);
}
