package com.docutrace.user_service.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RateLimiter {
    private final StringRedisTemplate redis;
    public RateLimiter(StringRedisTemplate redis) { this.redis = redis; }

    public boolean allow(String key, int max, Duration window) {
        try {
            String redisKey = "rl:" + key;
            Long count = redis.opsForValue().increment(redisKey);
            if (count != null && count == 1L) {
                redis.expire(redisKey, window);
            }
            return count != null && count <= max;
    } catch (DataAccessException ex) {
            // Fail open when Redis is unavailable (e.g., tests), do not block the request
            return true;
        }
    }
}
