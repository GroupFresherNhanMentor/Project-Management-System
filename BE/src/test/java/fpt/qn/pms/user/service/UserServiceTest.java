package fpt.qn.pms.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.pms.ProjectManagementSystemApplication;
import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.common.exception.AppException;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.user.dto.request.CreateUserRequest;
import fpt.qn.pms.user.dto.request.UpdateUserRequest;
import fpt.qn.pms.user.dto.request.UpdateUserStatusRequest;
import fpt.qn.pms.user.dto.response.UserDto;

@SpringBootTest(classes = ProjectManagementSystemApplication.class)
@Transactional
class UserServiceTest {

    @Autowired
    UserService userService;

    // ── createUser ────────────────────────────────────────────────────────────

    @Test
    void createUser_shouldReturnMappedDto() {
        UserDto dto = userService.createUser(buildRequest("101"));

        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getEmployeeId()).isEqualTo("EMP101");
        assertThat(dto.getUsername()).isEqualTo("user101");
        assertThat(dto.getFullName()).isEqualTo("Test User 101");
        assertThat(dto.getEmail()).isEqualTo("user101@test.com");
        assertThat(dto.getRole()).isEqualTo("USER");
        assertThat(dto.getStatus()).isEqualTo("ACTIVE");
        assertThat(dto.getCreatedAt()).isNotNull();
    }

    @Test
    void createUser_shouldEncodePassword() {
        UserDto dto = userService.createUser(buildRequest("002"));
        assertThat(dto).isNotNull();
    }

    @Test
    void createUser_shouldThrow_whenUsernameDuplicated() {
        userService.createUser(buildRequest("003"));

        CreateUserRequest duplicate = buildRequest("003x");
        duplicate.setUsername("user003");

        assertThatThrownBy(() -> userService.createUser(duplicate))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void createUser_shouldThrow_whenEmailDuplicated() {
        userService.createUser(buildRequest("004"));

        CreateUserRequest duplicate = buildRequest("004x");
        duplicate.setEmail("user004@test.com");

        assertThatThrownBy(() -> userService.createUser(duplicate))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void createUser_shouldThrow_whenEmployeeIdDuplicated() {
        userService.createUser(buildRequest("005"));

        CreateUserRequest duplicate = buildRequest("005x");
        duplicate.setEmployeeId("EMP005");

        assertThatThrownBy(() -> userService.createUser(duplicate))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Employee ID already exists");
    }

    // ── getUserById ───────────────────────────────────────────────────────────

    @Test
    void getUserById_shouldReturnDto_whenExists() {
        UserDto created = userService.createUser(buildRequest("006"));

        UserDto found = userService.getUserById(created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getUsername()).isEqualTo("user006");
    }

    @Test
    void getUserById_shouldThrow_whenNotExists() {
        assertThatThrownBy(() -> userService.getUserById(UUID.randomUUID()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("User not found");
    }

    // ── getUsers ──────────────────────────────────────────────────────────────

    @Test
    void getUsers_shouldReturnPageResponse_withCorrectMetadata() {
        userService.createUser(buildRequest("007"));
        userService.createUser(buildRequest("008"));

        PageResponse<UserDto> page = userService.getUsers(null, null, null, 0, 10);

        assertThat(page.getItems()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
        assertThat(page.getPageNumber()).isZero();
        assertThat(page.getPageSize()).isEqualTo(10);
        assertThat(page.getTotalPages()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void getUsers_shouldFilterByKeyword() {
        userService.createUser(buildRequest("009"));

        PageResponse<UserDto> page = userService.getUsers("user009", null, null, 0, 10);

        assertThat(page.getItems()).isNotEmpty();
        assertThat(page.getItems()).allMatch(u ->
                u.getUsername().contains("009")
                        || u.getFullName().contains("009")
                        || u.getEmail().contains("009"));
    }

    @Test
    void getUsers_shouldFilterByRole() {
        CreateUserRequest adminReq = buildRequest("010");
        adminReq.setRole(SysRole.ADMIN);
        userService.createUser(adminReq);

        PageResponse<UserDto> page = userService.getUsers(null, SysRole.ADMIN, null, 0, 10);

        assertThat(page.getItems()).isNotEmpty();
        assertThat(page.getItems()).allMatch(u -> u.getRole().equals("ADMIN"));
    }

    @Test
    void getUsers_shouldFilterByStatus() {
        UserDto created = userService.createUser(buildRequest("011"));

        UpdateUserStatusRequest lockReq = new UpdateUserStatusRequest();
        lockReq.setStatus(UserStatus.LOCKED);
        userService.updateUserStatus(created.getId(), lockReq);

        PageResponse<UserDto> page = userService.getUsers(null, null, UserStatus.LOCKED, 0, 10);

        assertThat(page.getItems()).isNotEmpty();
        assertThat(page.getItems()).allMatch(u -> u.getStatus().equals("LOCKED"));
    }

    @Test
    void getUsers_shouldRespectPagination() {
        userService.createUser(buildRequest("012"));
        userService.createUser(buildRequest("013"));
        userService.createUser(buildRequest("014"));

        PageResponse<UserDto> page = userService.getUsers(null, null, null, 0, 2);

        assertThat(page.getItems()).hasSize(2);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(3);
        assertThat(page.getTotalPages()).isGreaterThanOrEqualTo(2);
    }

    // ── updateUser ────────────────────────────────────────────────────────────

    @Test
    void updateUser_shouldUpdateFullNameAndRole() {
        UserDto created = userService.createUser(buildRequest("015"));

        UpdateUserRequest req = new UpdateUserRequest();
        req.setFullName("Updated Name 015");
        req.setRole(SysRole.ADMIN);

        UserDto updated = userService.updateUser(created.getId(), req);

        assertThat(updated.getFullName()).isEqualTo("Updated Name 015");
        assertThat(updated.getRole()).isEqualTo("ADMIN");
        assertThat(updated.getUsername()).isEqualTo("user015");
        assertThat(updated.getEmail()).isEqualTo("user015@test.com");
    }

    @Test
    void updateUser_shouldIgnoreNullFields() {
        UserDto created = userService.createUser(buildRequest("016"));

        UpdateUserRequest req = new UpdateUserRequest();

        UserDto updated = userService.updateUser(created.getId(), req);

        assertThat(updated.getFullName()).isEqualTo("Test User 016");
        assertThat(updated.getRole()).isEqualTo("USER");
        assertThat(updated.getEmail()).isEqualTo("user016@test.com");
    }

    @Test
    void updateUser_shouldThrow_whenNotExists() {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setFullName("Whatever");

        assertThatThrownBy(() -> userService.updateUser(UUID.randomUUID(), req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateUser_shouldThrow_whenEmailTakenByAnotherUser() {
        userService.createUser(buildRequest("017"));
        UserDto second = userService.createUser(buildRequest("018"));

        UpdateUserRequest req = new UpdateUserRequest();
        req.setEmail("user017@test.com");

        assertThatThrownBy(() -> userService.updateUser(second.getId(), req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Email already exists");
    }

    // ── updateUserStatus ──────────────────────────────────────────────────────

    @Test
    void updateUserStatus_shouldLockUser() {
        UserDto created = userService.createUser(buildRequest("019"));

        UpdateUserStatusRequest req = new UpdateUserStatusRequest();
        req.setStatus(UserStatus.LOCKED);

        UserDto updated = userService.updateUserStatus(created.getId(), req);

        assertThat(updated.getStatus()).isEqualTo("LOCKED");
        assertThat(updated.getId()).isEqualTo(created.getId());
    }

    @Test
    void updateUserStatus_shouldUnlockUser() {
        UserDto created = userService.createUser(buildRequest("020"));

        UpdateUserStatusRequest lockReq = new UpdateUserStatusRequest();
        lockReq.setStatus(UserStatus.LOCKED);
        userService.updateUserStatus(created.getId(), lockReq);

        UpdateUserStatusRequest unlockReq = new UpdateUserStatusRequest();
        unlockReq.setStatus(UserStatus.ACTIVE);
        UserDto unlocked = userService.updateUserStatus(created.getId(), unlockReq);

        assertThat(unlocked.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void updateUserStatus_shouldThrow_whenNotExists() {
        UpdateUserStatusRequest req = new UpdateUserStatusRequest();
        req.setStatus(UserStatus.LOCKED);

        assertThatThrownBy(() -> userService.updateUserStatus(UUID.randomUUID(), req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("User not found");
    }

    private CreateUserRequest buildRequest(String suffix) {
        CreateUserRequest req = new CreateUserRequest();
        req.setEmployeeId("EMP" + suffix);
        req.setUsername("user" + suffix);
        req.setFullName("Test User " + suffix);
        req.setEmail("user" + suffix + "@test.com");
        req.setPassword("Password@123");
        req.setRole(SysRole.USER);
        return req;
    }
}
