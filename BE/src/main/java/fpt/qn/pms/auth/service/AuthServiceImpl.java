package fpt.qn.pms.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import fpt.qn.pms.auth.dto.LoginRequest;
import fpt.qn.pms.auth.dto.LoginResponse;
import fpt.qn.pms.auth.dto.RefreshTokenRequest;
import fpt.qn.pms.auth.dto.RefreshTokenResponse;
import fpt.qn.pms.common.exception.AppException;
import fpt.qn.pms.jooq.enums.UserStatus;
import fpt.qn.pms.jooq.tables.records.UsersRecord;
import fpt.qn.pms.security.JwtTokenProvider;
import fpt.qn.pms.user.dto.UserDto;
import fpt.qn.pms.user.mapper.UserMapper;
import fpt.qn.pms.user.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    AuthenticationManager authenticationManager;
    UserRepository userRepository;
    JwtTokenProvider jwtTokenProvider;
    JwtDecoder jwtDecoder;
    UserMapper userMapper;

    @Override
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid username or password");
        } catch (DisabledException ex) {
            throw new DisabledException("Account is locked or disabled");
        }

        UsersRecord user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException("User not found"));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new DisabledException("Account is locked or disabled");
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
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        try {
            Jwt jwt = jwtDecoder.decode(request.getRefreshToken());
            if (!jwtTokenProvider.isRefreshToken(jwt)) {
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
