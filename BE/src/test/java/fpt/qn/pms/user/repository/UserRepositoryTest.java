package fpt.qn.pms.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.pms.ProjectManagementSystemApplication;
import fpt.qn.pms.common.dto.PaginationResult;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.UsersRecord;

@SpringBootTest(classes = ProjectManagementSystemApplication.class)
@Transactional
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void create_shouldPersistAndReturnRecord() {
        UsersRecord saved = userRepository.create(buildUser("101"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("user101");
        assertThat(saved.getEmail()).isEqualTo("user101@test.com");
        assertThat(saved.getRole()).isEqualTo(SysRole.USER);
        assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void findById_shouldReturnRecord_whenExists() {
        UsersRecord created = userRepository.create(buildUser("102"));

        Optional<UsersRecord> found = userRepository.findById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("user102");
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        assertThat(userRepository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void findByUsername_shouldReturnRecord_whenExists() {
        userRepository.create(buildUser("003"));

        Optional<UsersRecord> found = userRepository.findByUsername("user003");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("user003@test.com");
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenNotExists() {
        assertThat(userRepository.findByUsername("nonexistent")).isEmpty();
    }

    @Test
    void existsByUsername_shouldReturnTrue_whenExists() {
        userRepository.create(buildUser("004"));
        assertThat(userRepository.existsByUsername("user004")).isTrue();
    }

    @Test
    void existsByUsername_shouldReturnFalse_whenNotExists() {
        assertThat(userRepository.existsByUsername("ghost")).isFalse();
    }

    @Test
    void existsByEmail_shouldReturnTrue_whenExists() {
        userRepository.create(buildUser("005"));
        assertThat(userRepository.existsByEmail("user005@test.com")).isTrue();
    }

    @Test
    void existsByEmployeeId_shouldReturnTrue_whenExists() {
        userRepository.create(buildUser("006"));
        assertThat(userRepository.existsByEmployeeId("EMP006")).isTrue();
    }

    @Test
    void findAll_shouldReturnAllUsers_whenNoFilter() {
        userRepository.create(buildUser("007"));
        userRepository.create(buildUser("008"));

        PaginationResult<UsersRecord> result = userRepository.findAll(null, null, null, 0, 20);

        assertThat(result.getTotal()).isGreaterThanOrEqualTo(2);
        assertThat(result.getItems()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void findAll_shouldFilterByKeyword() {
        userRepository.create(buildUser("009"));

        PaginationResult<UsersRecord> result = userRepository.findAll("user009", null, null, 0, 20);

        assertThat(result.getItems()).isNotEmpty();
        assertThat(result.getItems()).allMatch(r ->
                r.getUsername().contains("009")
                        || r.getFullName().contains("009")
                        || r.getEmail().contains("009"));
    }

    @Test
    void findAll_shouldFilterByRole() {
        UsersRecord admin = buildUser("010");
        admin.setRole(SysRole.ADMIN);
        userRepository.create(admin);

        PaginationResult<UsersRecord> result = userRepository.findAll(null, SysRole.ADMIN, null, 0, 20);

        assertThat(result.getItems()).isNotEmpty();
        assertThat(result.getItems()).allMatch(r -> r.getRole() == SysRole.ADMIN);
    }

    @Test
    void findAll_shouldFilterByStatus() {
        UsersRecord locked = buildUser("011");
        locked.setStatus(UserStatus.LOCKED);
        userRepository.create(locked);

        PaginationResult<UsersRecord> result = userRepository.findAll(null, null, UserStatus.LOCKED, 0, 20);

        assertThat(result.getItems()).isNotEmpty();
        assertThat(result.getItems()).allMatch(r -> r.getStatus() == UserStatus.LOCKED);
    }

    @Test
    void findAll_shouldRespectPagination() {
        userRepository.create(buildUser("012"));
        userRepository.create(buildUser("013"));
        userRepository.create(buildUser("014"));

        PaginationResult<UsersRecord> result = userRepository.findAll(null, null, null, 0, 2);

        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getTotal()).isGreaterThanOrEqualTo(3);
    }

    @Test
    void update_shouldModifyFields() {
        UsersRecord created = userRepository.create(buildUser("015"));

        UsersRecord fetched = userRepository.findById(created.getId()).orElseThrow();
        fetched.setFullName("Updated Name");
        fetched.setStatus(UserStatus.LOCKED);
        userRepository.update(fetched);

        UsersRecord updated = userRepository.findById(created.getId()).orElseThrow();
        assertThat(updated.getFullName()).isEqualTo("Updated Name");
        assertThat(updated.getStatus()).isEqualTo(UserStatus.LOCKED);
    }

    private UsersRecord buildUser(String suffix) {
        UsersRecord record = new UsersRecord();
        record.setEmployeeId("EMP" + suffix);
        record.setUsername("user" + suffix);
        record.setFullName("Test User " + suffix);
        record.setEmail("user" + suffix + "@test.com");
        record.setPassword("$2a$10$hashed" + suffix);
        record.setRole(SysRole.USER);
        return record;
    }
}
