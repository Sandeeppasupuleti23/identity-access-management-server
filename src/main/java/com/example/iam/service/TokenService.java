package com.example.iam.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final StringRedisTemplate stringRedisTemplate;
    private final Set<String> inMemoryRevokedTokens = ConcurrentHashMap.newKeySet();
    private final Set<String> inMemoryRevokedUsers = ConcurrentHashMap.newKeySet();

    public void revokeUser(String username) {
        String key = "revoked:user:" + username;
        try {
            stringRedisTemplate.opsForSet().add(key, "all");
            stringRedisTemplate.expire(key, Duration.ofDays(7));
        } catch (RuntimeException e) {
            inMemoryRevokedUsers.add(key);
        }
    }

    public void revokeToken(String tokenKey) {
        String key = "revoked:tokens";
        try {
            stringRedisTemplate.opsForSet().add(key, tokenKey);
            stringRedisTemplate.expire(key, Duration.ofDays(7));
        } catch (RuntimeException e) {
            inMemoryRevokedTokens.add(tokenKey);
        }
    }

    public boolean isRevoked(String tokenKey) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember("revoked:tokens", tokenKey));
        } catch (RuntimeException e) {
            return inMemoryRevokedTokens.contains(tokenKey);
        }
    }

    public boolean isUserRevoked(String username) {
        String key = "revoked:user:" + username;
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(key, "all"));
        } catch (RuntimeException e) {
            return inMemoryRevokedUsers.contains(key);
        }
    }
}
