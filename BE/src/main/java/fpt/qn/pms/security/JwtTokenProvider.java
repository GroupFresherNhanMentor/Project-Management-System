package fpt.qn.pms.security;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtTokenProvider {

    @Value("${app.jwt.access-token-expiration:900000}")
    long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration:604800000}")
    long refreshTokenExpiration;

    final JwtEncoder jwtEncoder;

    public String generateAccessToken(String username, String role) {
        return buildToken(username, role, accessTokenExpiration, "access");
    }

    public String generateRefreshToken(String username) {
        return buildToken(username, null, refreshTokenExpiration, "refresh");
    }

    public boolean isRefreshToken(Jwt jwt) {
        return "refresh".equals(jwt.getClaim("type"));
    }

    private String buildToken(String username, String role, long expirationMs, String tokenType) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .subject(username)
                .issuedAt(now)
                .expiresAt(now.plusMillis(expirationMs))
                .claim("type", tokenType);

        if (role != null) {
            claimsBuilder.claim("role", role);
        }

        return jwtEncoder.encode(
                JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claimsBuilder.build())
        ).getTokenValue();
    }
}
