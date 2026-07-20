package fpt.qn.pms.auth.service.impl;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fpt.qn.pms.auth.dto.request.LoginRequest;
import fpt.qn.pms.auth.dto.request.RefreshTokenRequest;
import fpt.qn.pms.auth.dto.response.LoginResponse;
import fpt.qn.pms.auth.dto.response.RefreshTokenResponse;
import fpt.qn.pms.auth.service.AuthService;
import fpt.qn.pms.common.exception.AppException;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.security.JwtTokenProvider;
import fpt.qn.pms.user.dto.response.UserDto;
import fpt.qn.pms.user.mapper.UserMapper;
import fpt.qn.pms.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    JwtTokenProvider jwtTokenProvider;
    JwtDecoder jwtDecoder;
    UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        UsersRecord user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new DisabledException("User account is locked");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRole().getLiteral());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
        UserDto userDto = userMapper.toDto(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userDto)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        try {
            Jwt jwt = jwtDecoder.decode(request.getRefreshToken());
            String tokenType = jwt.getClaimAsString("type");
            if (!"refresh".equals(tokenType)) {
                throw new BadCredentialsException("Invalid refresh token");
            }

            String username = jwt.getSubject();
            UsersRecord user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AppException("User not found"));

            if (user.getStatus() == UserStatus.LOCKED) {
                throw new DisabledException("Account is locked or disabled");
            }

            String newAccessToken = jwtTokenProvider.generateAccessToken(user.getUsername(), user.getRole().getLiteral());
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());

            return RefreshTokenResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();

        } catch (JwtException ex) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }
    }

    @Override
    public void logout(RefreshTokenRequest request) {
        // Stateless logout: tokens are managed client-side
    }
}
