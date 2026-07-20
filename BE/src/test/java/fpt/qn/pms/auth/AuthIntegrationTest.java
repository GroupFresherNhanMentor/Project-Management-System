package fpt.qn.pms.auth;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import fpt.qn.pms.ProjectManagementSystemApplication;
import fpt.qn.pms.auth.dto.request.LoginRequest;
import fpt.qn.pms.auth.dto.request.RefreshTokenRequest;
import fpt.qn.pms.jooq.enums.SysRole;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.security.JwtTokenProvider;
import fpt.qn.pms.user.repository.UserRepository;

import fpt.qn.pms.config.TestRedisConfig;

import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {ProjectManagementSystemApplication.class, TestRedisConfig.class})
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

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
                    admin.setEmployeeId("EMP001");
                    admin.setUsername("admin");
                    admin.setFullName("System Administrator");
                    admin.setEmail("admin@pms.com");
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    admin.setRole(SysRole.ADMIN);
                    admin.setStatus(UserStatus.ACTIVE);
                    userRepository.create(admin);
                }
        );
    }

    @Test
    void login_shouldReturnTokens_whenCredentialsAreValid() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("admin123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.user.username").value("admin"))
                .andExpect(jsonPath("$.data.user.role").value("ADMIN"));
    }

    @Test
    void login_shouldReturn401_whenPasswordIsInvalid() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void login_shouldReturn403_whenUserIsLocked() throws Exception {
        UsersRecord lockedUser = new UsersRecord();
        lockedUser.setEmployeeId("EMP999");
        lockedUser.setUsername("lockeduser");
        lockedUser.setFullName("Locked User");
        lockedUser.setEmail("locked@pms.com");
        lockedUser.setPassword(passwordEncoder.encode("password123"));
        lockedUser.setRole(SysRole.USER);
        lockedUser.setStatus(UserStatus.LOCKED);
        userRepository.create(lockedUser);

        LoginRequest request = LoginRequest.builder()
                .username("lockeduser")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void refresh_shouldReturnNewTokens_whenRefreshTokenIsValid() throws Exception {
        String refreshToken = jwtTokenProvider.generateRefreshToken("admin");
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    void refresh_shouldReturn401_whenRefreshTokenIsInvalid() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("invalid.jwt.token")
                .build();

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void logout_shouldReturn204_whenCalled() throws Exception {
        String refreshToken = jwtTokenProvider.generateRefreshToken("admin");
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken(refreshToken)
                .build();

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    void protectedEndpoint_shouldReturn200_whenValidAdminTokenProvided() throws Exception {
        String accessToken = jwtTokenProvider.generateAccessToken("admin", SysRole.ADMIN.getLiteral());

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void protectedEndpoint_shouldReturn401_whenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }
}
