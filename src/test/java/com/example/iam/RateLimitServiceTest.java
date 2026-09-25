package com.example.iam;

import com.example.iam.service.RateLimitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.springframework.dao.DataAccessResourceFailureException;

@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RateLimitService rateLimitService;

    @Test
    void rateLimitAllowsRequestsUntilLimitReached() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment("login:127.0.0.1:user")).thenReturn(1L, 2L, 3L, 4L, 5L, 6L);

        assertTrue(rateLimitService.allow("login:127.0.0.1:user", 5, Duration.ofMinutes(15)));
        assertTrue(rateLimitService.allow("login:127.0.0.1:user", 5, Duration.ofMinutes(15)));
        assertTrue(rateLimitService.allow("login:127.0.0.1:user", 5, Duration.ofMinutes(15)));
        assertTrue(rateLimitService.allow("login:127.0.0.1:user", 5, Duration.ofMinutes(15)));
        assertTrue(rateLimitService.allow("login:127.0.0.1:user", 5, Duration.ofMinutes(15)));
        assertFalse(rateLimitService.allow("login:127.0.0.1:user", 5, Duration.ofMinutes(15)));
    }

    @Test
    void rateLimitFallsBackToMemoryWhenRedisIsUnavailable() {
        when(redisTemplate.opsForValue()).thenThrow(new DataAccessResourceFailureException("redis down"));

        assertTrue(rateLimitService.allow("fallback:key", 2, Duration.ofMinutes(1)));
        assertTrue(rateLimitService.allow("fallback:key", 2, Duration.ofMinutes(1)));
        assertFalse(rateLimitService.allow("fallback:key", 2, Duration.ofMinutes(1)));
    }
}
