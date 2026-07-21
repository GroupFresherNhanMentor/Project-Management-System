package fpt.qn.pms.user.helper;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.user.dto.request.CreateUserRequest;
import fpt.qn.pms.user.dto.response.UserDto;
import fpt.qn.pms.user.mapper.UserMapper;
import fpt.qn.pms.user.repository.UserRepository;
import fpt.qn.pms.user.util.UsernameGenerator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserCreationTransactionHelper {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    UsernameGenerator usernameGenerator;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserDto executeAttempt(CreateUserRequest request) {
        String username = usernameGenerator.generate(request.getFullName());

        UsersRecord record = userMapper.toRecord(request);
        record.setUsername(username);
        record.setPassword(passwordEncoder.encode(request.getPassword()));
        record.setEmployeeId("EMP-");
        record.setStatus(UserStatus.ACTIVE);

        UsersRecord saved = userRepository.create(record);
        saved.setEmployeeId("EMP-" + saved.getId());
        userRepository.update(saved);
        return userMapper.toDto(saved);
    }
}
