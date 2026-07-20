package fpt.qn.pms.user.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.exception.AppException;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.user.dto.request.CreateUserRequest;
import fpt.qn.pms.user.dto.request.UpdateUserRequest;
import fpt.qn.pms.user.dto.request.UpdateUserStatusRequest;
import fpt.qn.pms.user.dto.response.UserDto;
import fpt.qn.pms.user.mapper.UserMapper;
import fpt.qn.pms.user.repository.UserRepository;
import fpt.qn.pms.user.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email already exists");
        }
        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new AppException("Employee ID already exists");
        }

        UsersRecord record = userMapper.toRecord(request);
        record.setPassword(passwordEncoder.encode(request.getPassword()));
        record.setStatus(UserStatus.ACTIVE);

        UsersRecord saved = userRepository.create(record);
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(UUID id) {
        UsersRecord record = userRepository.findById(id)
                .orElseThrow(() -> new AppException("User not found"));
        return userMapper.toDto(record);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserDto> getUsers(String keyword, SysRole role, UserStatus status, int page, int size) {
        PaginationResult<UsersRecord> result = userRepository.findAll(keyword, role, status, page, size);
        List<UserDto> items = userMapper.toDtoList(result.getItems());
        return PageResponse.of(items, page, size, result.getTotal());
    }

    @Override
    @Transactional
    public UserDto updateUser(UUID id, UpdateUserRequest request) {
        UsersRecord record = userRepository.findById(id)
                .orElseThrow(() -> new AppException("User not found"));

        if (request.getEmail() != null && userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new AppException("Email already exists");
        }

        userMapper.updateRecord(record, request);
        userRepository.update(record);
        return userMapper.toDto(record);
    }

    @Override
    @Transactional
    public UserDto updateUserStatus(UUID id, UpdateUserStatusRequest request) {
        UsersRecord record = userRepository.findById(id)
                .orElseThrow(() -> new AppException("User not found"));

        record.setStatus(request.getStatus());
        userRepository.update(record);
        return userMapper.toDto(record);
    }
}
