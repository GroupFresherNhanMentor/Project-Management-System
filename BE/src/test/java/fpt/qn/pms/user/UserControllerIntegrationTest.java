package fpt.qn.pms.user;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import fpt.qn.pms.BaseIntegrationTest;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.security.JwtTokenProvider;
import fpt.qn.pms.user.dto.request.ChangePasswordRequest;
import fpt.qn.pms.user.dto.request.CreateUserRequest;
import fpt.qn.pms.user.dto.request.UpdateCurrentUserRequest;
import fpt.qn.pms.user.dto.request.UpdateUserRequest;
import fpt.qn.pms.user.dto.request.UpdateUserStatusRequest;
import fpt.qn.pms.user.repository.UserRepository;

class UserControllerIntegrationTest extends BaseIntegrationTest {

    MockMvc mockMvc;

    @Autowired
    WebApplicationContext webApplicationContext;

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    String adminToken;
    String userToken;
    UsersRecord testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        userRepository.findByUsername("admin").ifPresentOrElse(
                admin -> {
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    admin.setStatus(UserStatus.ACTIVE);
                    userRepository.update(admin);
                },
                () -> {
                    UsersRecord admin = new UsersRecord();
                    admin.setEmployeeId("EMP_TEST_001");
                    admin.setUsername("admin");
                    admin.setFullName("System Administrator");
                    admin.setEmail("admin@pms.com");
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    admin.setRole(SysRole.ADMIN);
                    admin.setStatus(UserStatus.ACTIVE);
                    userRepository.create(admin);
                }
        );

        userRepository.findByUsername("regularuser").ifPresentOrElse(
                user -> {
                    user.setPassword(passwordEncoder.encode("password123"));
                    user.setStatus(UserStatus.ACTIVE);
                    userRepository.update(user);
                    testUser = user;
                },
                () -> {
                    testUser = new UsersRecord();
                    testUser.setEmployeeId("EMP_TEST_002");
                    testUser.setUsername("regularuser");
                    testUser.setFullName("Regular User");
                    testUser.setEmail("regular@pms.com");
                    testUser.setPassword(passwordEncoder.encode("password123"));
                    testUser.setRole(SysRole.USER);
                    testUser.setStatus(UserStatus.ACTIVE);
                    testUser = userRepository.create(testUser);
                }
        );

        adminToken = jwtTokenProvider.generateAccessToken("admin", SysRole.ADMIN.getLiteral());
        userToken = jwtTokenProvider.generateAccessToken("regularuser", SysRole.USER.getLiteral());
    }

    @Test
    void getUsers_shouldReturnPagedUsers_whenAdminTokenProvided() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    void getUsers_shouldReturn403_whenUserRoleTokenProvided() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserById_shouldReturnUser_whenAdminTokenProvided() throws Exception {
        mockMvc.perform(get("/api/users/" + testUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testUser.getId().toString()))
                .andExpect(jsonPath("$.data.username").value("regularuser"));
    }

    @Test
    void createUser_shouldCreateUser_whenAdminTokenProvided() throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                .fullName("newuser")
                .email("newuser@pms.com")
                .role(SysRole.USER)
                .build();

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.user.username").value("newuser"))
                .andExpect(jsonPath("$.data.generatedPassword").isString())
                .andExpect(jsonPath("$.data.generatedPassword").value(org.hamcrest.Matchers.matchesPattern("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{12}$")))
                .andExpect(jsonPath("$.data.user.employeeId").value(org.hamcrest.Matchers.matchesPattern("^EMP-[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")));
    }

    @Test
    void updateUser_shouldUpdateWithoutEmailConflict_whenEmailUnchanged() throws Exception {
        UpdateUserRequest request = UpdateUserRequest.builder()
                .fullName("Updated Regular User")
                .email("regular@pms.com") // Same email
                .role(SysRole.USER)
                .build();

        mockMvc.perform(put("/api/users/" + testUser.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Updated Regular User"));
    }

    @Test
    void getCurrentUser_shouldReturnOwnProfile_whenUserTokenProvided() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("regularuser"))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    void getCurrentUser_shouldReturnProfile_whenAdminTokenProvided() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    void getCurrentUser_shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateCurrentUser_shouldUpdateProfile_whenUserTokenProvided() throws Exception {
        UpdateCurrentUserRequest request = UpdateCurrentUserRequest.builder()
                .fullName("Self Updated User")
                .email("selfupdated@pms.com")
                .build();

        mockMvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Self Updated User"))
                .andExpect(jsonPath("$.data.email").value("selfupdated@pms.com"));
    }

    @Test
    void updateCurrentUser_shouldReturn401_whenNoToken() throws Exception {
        UpdateCurrentUserRequest request = UpdateCurrentUserRequest.builder()
                .fullName("Hacker")
                .build();

        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateUserStatus_shouldLockUser_whenAdminTokenProvided() throws Exception {
        UpdateUserStatusRequest request = UpdateUserStatusRequest.builder()
                .status(UserStatus.LOCKED)
                .build();

        mockMvc.perform(patch("/api/users/" + testUser.getId() + "/lock")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("LOCKED"));
    }

    @Test
    void updateUserStatus_shouldReturnBadRequest_whenAdminLocksSelf() throws Exception {
        UsersRecord adminUser = userRepository.findByUsername("admin").orElseThrow();

        UpdateUserStatusRequest request = UpdateUserStatusRequest.builder()
                .status(UserStatus.LOCKED)
                .build();

        mockMvc.perform(patch("/api/users/" + adminUser.getId() + "/lock")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Admin cannot lock their own account"));
    }

    @Test
    void resetPassword_shouldResetPassword_whenAdminTokenProvided() throws Exception {
        mockMvc.perform(put("/api/users/" + testUser.getId() + "/reset-password")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(testUser.getId().toString()))
                .andExpect(jsonPath("$.data.username").value(testUser.getUsername()))
                .andExpect(jsonPath("$.data.generatedPassword").isNotEmpty());
    }

    @Test
    void resetPassword_shouldReturn403_whenUserTokenProvided() throws Exception {
        mockMvc.perform(put("/api/users/" + testUser.getId() + "/reset-password")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void request_shouldFail_whenUserIsLocked() throws Exception {
        testUser.setStatus(UserStatus.LOCKED);
        userRepository.update(testUser);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_shouldReturn200_whenValidOldPassword() throws Exception {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("password123")
                .newPassword("NewPass123!")
                .build();

        mockMvc.perform(put("/api/users/me/change-password")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }

    @Test
    void changePassword_shouldReturn400_whenInvalidOldPassword() throws Exception {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .oldPassword("WrongOldPass123")
                .newPassword("NewPass123!")
                .build();

        mockMvc.perform(put("/api/users/me/change-password")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }
}
