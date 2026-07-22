package fpt.qn.pms.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisTokenBlacklistServiceTest {

    @Mock
    StringRedisTemplate redisTemplate;

    @Mock
    ValueOperations<String, String> valueOperations;

    @InjectMocks
    RedisTokenBlacklistService blacklistService;

    @Test
    void blacklistToken_shouldSetKeyInRedis_whenValidInput() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        blacklistService.blacklistToken("test-jwt-id", 60000);

        verify(valueOperations).set(eq("token:blacklist:test-jwt-id"), eq("revoked"), eq(Duration.ofMillis(60000)));
    }

    @Test
    void isBlacklisted_shouldReturnTrue_whenKeyExistsInRedis() {
        when(redisTemplate.hasKey("token:blacklist:test-jwt-id")).thenReturn(true);

        boolean result = blacklistService.isBlacklisted("test-jwt-id");

        assertTrue(result);
    }

    @Test
    void isBlacklisted_shouldReturnFalse_whenKeyDoesNotExistInRedis() {
        when(redisTemplate.hasKey("token:blacklist:test-jwt-id")).thenReturn(false);

        boolean result = blacklistService.isBlacklisted("test-jwt-id");

        assertFalse(result);
    }
}
