package com.example.iam.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;
    private final Map<String, AtomicInteger> inMemoryCounters = new ConcurrentHashMap<>();

    public boolean allow(String key, int limit, Duration window) {
        try {
            Long value = redisTemplate.opsForValue().increment(key);
            if (value == null || value == 1L) {
                redisTemplate.expire(key, window);
            }
            return value != null && value <= limit;
        } catch (RuntimeException e) {
            AtomicInteger counter = inMemoryCounters.computeIfAbsent(key, ignored -> new AtomicInteger(0));
            int next = counter.incrementAndGet();
            return next <= limit;
        }
    }

    public void reset(String key) {
        try {
            redisTemplate.delete(key);
        } catch (RuntimeException e) {
            inMemoryCounters.remove(key);
        }
    }
}
