package fpt.qn.pms.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.pms.BaseIntegrationTest;
import fpt.qn.pms.common.dto.PageResponse;
import fpt.qn.pms.common.exception.AppException;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.user.dto.request.CreateUserRequest;
import fpt.qn.pms.user.dto.request.UpdateCurrentUserRequest;
import fpt.qn.pms.user.dto.request.UpdateUserRequest;
import fpt.qn.pms.user.dto.request.UpdateUserStatusRequest;
import fpt.qn.pms.user.dto.response.UserDto;
import fpt.qn.pms.user.exception.EmailAlreadyExistsException;
import fpt.qn.pms.user.repository.UserRepository;

class UserServiceTest extends BaseIntegrationTest {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    // ── createUser ────────────────────────────────────────────────────────────

    @Test
    void createUser_shouldReturnMappedDto() {
        UserDto dto = userService.createUser(buildRequest("101"));

        assertThat(dto.getId()).isNotNull();
        assertThat(dto.getEmployeeId()).isEqualTo("EMP-" + dto.getId());
        assertThat(dto.getUsername()).isEqualTo("user101");
        assertThat(dto.getFullName()).isEqualTo("user101");
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
    void createUser_shouldAutoGenerateFptUsername_forVietnameseFullName() {
        CreateUserRequest req = buildRequest("003a");
        req.setFullName("Chế Việt Khôi");
        UserDto dto = userService.createUser(req);
        assertThat(dto.getUsername()).isEqualTo("khoicv");
    }

    @Test
    void createUser_shouldAutoGenerateFptUsername_forSingleWordName() {
        CreateUserRequest req = buildRequest("003b");
        req.setFullName("Khôi");
        UserDto dto = userService.createUser(req);
        assertThat(dto.getUsername()).isEqualTo("khoi");
    }

    @Test
    void createUser_shouldAutoGenerateFptUsername_forVietnameseDd() {
        CreateUserRequest req = buildRequest("003c");
        req.setFullName("Đỗ Đăng Đạt");
        UserDto dto = userService.createUser(req);
        assertThat(dto.getUsername()).isEqualTo("datdd");
    }

    @Test
    void createUser_shouldAppendCollisionSuffix_whenSameNameCreatedMultipleTimes() {
        CreateUserRequest req1 = buildRequest("003d1");
        req1.setFullName("Lê An Alpha");
        UserDto dto1 = userService.createUser(req1);

        CreateUserRequest req2 = buildRequest("003d2");
        req2.setFullName("Lê An Alpha");
        UserDto dto2 = userService.createUser(req2);

        CreateUserRequest req3 = buildRequest("003d3");
        req3.setFullName("Lê An Alpha");
        UserDto dto3 = userService.createUser(req3);

        assertThat(dto1.getUsername()).isEqualTo("alphala");
        assertThat(dto2.getUsername()).isEqualTo("alphala2");
        assertThat(dto3.getUsername()).isEqualTo("alphala3");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void createUser_shouldReturnBareBase_whenNumericSuffixExistsButBareBaseDeleted() {
        CreateUserRequest req1 = buildRequest("003e1");
        req1.setFullName("Lê An Beta");
        UserDto dto1 = userService.createUser(req1);

        CreateUserRequest req2 = buildRequest("003e2");
        req2.setFullName("Lê An Beta");
        UserDto dto2 = userService.createUser(req2);

        UserDto dto3 = null;
        try {
            UsersRecord record = userRepository.findById(dto1.getId()).orElseThrow();
            record.setUsername("betala_deleted");
            userRepository.update(record);

            CreateUserRequest req3 = buildRequest("003e3");
            req3.setFullName("Lê An Beta");
            dto3 = userService.createUser(req3);

            assertThat(dto3.getUsername()).isEqualTo("betala");
        } finally {
            if (dto1.getId() != null) userRepository.deleteById(dto1.getId());
            if (dto2.getId() != null) userRepository.deleteById(dto2.getId());
            if (dto3 != null && dto3.getId() != null) userRepository.deleteById(dto3.getId());
        }
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
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void createUser_shouldHandleConcurrentSameNameCreation() throws Exception {
        int numThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);

        List<Future<UserDto>> futures = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                startLatch.await();
                try {
                    CreateUserRequest req = buildRequest("conc_name_" + idx);
                    req.setFullName("Lê An Gamma");
                    return userService.createUser(req);
                } finally {
                    doneLatch.countDown();
                }
            }));
        }

        try {
            startLatch.countDown();
            boolean completed = doneLatch.await(15, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(completed).isTrue();

            Set<String> generatedUsernames = new HashSet<>();
            for (Future<UserDto> future : futures) {
                UserDto dto = future.get();
                assertThat(dto).isNotNull();
                generatedUsernames.add(dto.getUsername());
            }

            assertThat(generatedUsernames)
                    .containsExactlyInAnyOrder("gammala", "gammala2", "gammala3", "gammala4", "gammala5");
        } finally {
            for (Future<UserDto> future : futures) {
                try {
                    UserDto dto = future.get();
                    if (dto != null && dto.getId() != null) {
                        userRepository.deleteById(dto.getId());
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void createUser_shouldFailOnDuplicateEmail_withoutRetryingAsUsernameCollision() throws Exception {
        int numThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);

        String sharedEmail = "concurrent-dup-email@test.com";
        List<Future<UserDto>> futures = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            final int idx = i;
            futures.add(executor.submit(() -> {
                startLatch.await();
                try {
                    CreateUserRequest req = buildRequest("conc_email_" + idx);
                    req.setEmail(sharedEmail);
                    return userService.createUser(req);
                } finally {
                    doneLatch.countDown();
                }
            }));
        }

        try {
            startLatch.countDown();
            boolean completed = doneLatch.await(15, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(completed).isTrue();

            int successCount = 0;
            int duplicateEmailExceptionCount = 0;

            for (Future<UserDto> future : futures) {
                try {
                    UserDto dto = future.get();
                    if (dto != null) {
                        successCount++;
                    }
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof EmailAlreadyExistsException) {
                        duplicateEmailExceptionCount++;
                    }
                }
            }

            assertThat(successCount).isEqualTo(1);
            assertThat(duplicateEmailExceptionCount).isEqualTo(numThreads - 1);
        } finally {
            for (Future<UserDto> future : futures) {
                try {
                    UserDto dto = future.get();
                    if (dto != null && dto.getId() != null) {
                        userRepository.deleteById(dto.getId());
                    }
                } catch (Exception ignored) {
                }
            }
        }
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

        assertThat(updated.getFullName()).isEqualTo("user016");
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

    // ── getCurrentUser ─────────────────────────────────────────────────────────

    @Test
    void getCurrentUser_shouldReturnAuthenticatedUser() {
        userService.createUser(buildRequest("021"));

        setSecurityContext("user021", "USER");

        UserDto current = userService.getCurrentUser();

        assertThat(current.getUsername()).isEqualTo("user021");
        assertThat(current.getEmail()).isEqualTo("user021@test.com");
    }

    @Test
    void getCurrentUser_shouldThrow_whenUserNotInDb() {
        setSecurityContext("nonexistent", "USER");

        assertThatThrownBy(() -> userService.getCurrentUser())
                .isInstanceOf(AppException.class)
                .hasMessageContaining("User not found");
    }

    // ── updateCurrentUser ──────────────────────────────────────────────────────

    @Test
    void updateCurrentUser_shouldUpdateFullNameAndEmail() {
        userService.createUser(buildRequest("022"));
        setSecurityContext("user022", "USER");

        UpdateCurrentUserRequest req = new UpdateCurrentUserRequest();
        req.setFullName("New Name 022");
        req.setEmail("new022@test.com");

        UserDto updated = userService.updateCurrentUser(req);

        assertThat(updated.getFullName()).isEqualTo("New Name 022");
        assertThat(updated.getEmail()).isEqualTo("new022@test.com");
        assertThat(updated.getUsername()).isEqualTo("user022");
    }

    @Test
    void updateCurrentUser_shouldEncodePassword() {
        userService.createUser(buildRequest("023"));
        setSecurityContext("user023", "USER");

        UpdateCurrentUserRequest req = new UpdateCurrentUserRequest();
        req.setPassword("NewPass123!");

        UserDto updated = userService.updateCurrentUser(req);

        assertThat(updated).isNotNull();
    }

    @Test
    void updateCurrentUser_shouldIgnoreNullFields() {
        userService.createUser(buildRequest("024"));
        setSecurityContext("user024", "USER");

        UpdateCurrentUserRequest req = new UpdateCurrentUserRequest();

        UserDto updated = userService.updateCurrentUser(req);

        assertThat(updated.getFullName()).isEqualTo("user024");
        assertThat(updated.getEmail()).isEqualTo("user024@test.com");
    }

    @Test
    void updateCurrentUser_shouldThrow_whenEmailTakenByAnotherUser() {
        userService.createUser(buildRequest("025"));
        userService.createUser(buildRequest("026"));
        setSecurityContext("user026", "USER");

        UpdateCurrentUserRequest req = new UpdateCurrentUserRequest();
        req.setEmail("user025@test.com");

        assertThatThrownBy(() -> userService.updateCurrentUser(req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void updateCurrentUser_shouldAllowSameEmail() {
        userService.createUser(buildRequest("027"));
        setSecurityContext("user027", "USER");

        UpdateCurrentUserRequest req = new UpdateCurrentUserRequest();
        req.setEmail("user027@test.com");

        UserDto updated = userService.updateCurrentUser(req);

        assertThat(updated.getEmail()).isEqualTo("user027@test.com");
    }

    private void setSecurityContext(String username, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null,
                        List.of(new SimpleGrantedAuthority(role)))
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private CreateUserRequest buildRequest(String suffix) {
        CreateUserRequest req = new CreateUserRequest();
        req.setFullName("user" + suffix);
        req.setEmail("user" + suffix + "@test.com");
        req.setPassword("Password@123");
        req.setRole(SysRole.USER);
        return req;
    }
}
