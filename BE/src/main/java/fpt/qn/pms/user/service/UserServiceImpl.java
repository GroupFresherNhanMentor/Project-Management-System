package fpt.qn.pms.user.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.common.exception.AppException;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.user.dto.CreateUserRequest;
import fpt.qn.pms.user.dto.UpdateUserRequest;
import fpt.qn.pms.user.dto.UpdateUserStatusRequest;
import fpt.qn.pms.user.dto.UserDto;
import fpt.qn.pms.user.mapper.UserMapper;
import fpt.qn.pms.user.repository.UserRepository;
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
    @Transactional(readOnly = true)
    public PageResponse<UserDto> getUsers(String keyword, SysRole role, UserStatus status, int page, int size) {

        PaginationResult<UsersRecord> result = userRepository.findAll(keyword, role, status, page, size);
        return PageResponse.<UserDto>builder()
                .items(result.getItems().stream().map(userMapper::toDto).toList())
                .totalElements(result.getTotal())
                .totalPages((int) Math.ceil((double) result.getTotal() / size))
                .pageNumber(page)
                .pageSize(size)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new AppException("User not found"));
    }

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

        return userMapper.toDto(userRepository.create(record));
    }

    @Override
    @Transactional
    public UserDto updateUser(UUID id, UpdateUserRequest request) {
        UsersRecord record = userRepository.findById(id)
                .orElseThrow(() -> new AppException("User not found"));

        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email already exists");
        }

        userMapper.updateRecord(record, request);
        return userMapper.toDto(userRepository.update(record));
    }

    @Override
    @Transactional
    public UserDto updateUserStatus(UUID id, UpdateUserStatusRequest request) {
        UsersRecord record = userRepository.findById(id)
                .orElseThrow(() -> new AppException("User not found"));

        record.setStatus(request.getStatus());
        return userMapper.toDto(userRepository.update(record));
    }
}
