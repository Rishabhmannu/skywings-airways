package com.skywings.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Abstraction over OTP storage.
 * Uses Redis in dev, in-memory ConcurrentHashMap in production.
 */
@Component
@Slf4j
public class OtpStore {

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    private final ConcurrentHashMap<String, String> memoryStore = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public void set(String key, String value, long ttl, TimeUnit unit) {
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(key, value, ttl, unit);
                return;
            } catch (Exception e) {
                log.debug("Redis unavailable, falling back to memory store");
            }
        }
        memoryStore.put(key, value);
        scheduler.schedule(() -> memoryStore.remove(key), ttl, unit);
    }

    public String get(String key) {
        if (redisTemplate != null) {
            try {
                return redisTemplate.opsForValue().get(key);
            } catch (Exception e) {
                log.debug("Redis unavailable, falling back to memory store");
            }
        }
        return memoryStore.get(key);
    }

    public void delete(String key) {
        if (redisTemplate != null) {
            try {
                redisTemplate.delete(key);
            } catch (Exception ignored) {}
        }
        memoryStore.remove(key);
    }

    public Long increment(String key) {
        if (redisTemplate != null) {
            try {
                return redisTemplate.opsForValue().increment(key);
            } catch (Exception ignored) {}
        }
        String val = memoryStore.getOrDefault(key, "0");
        long next = Long.parseLong(val) + 1;
        memoryStore.put(key, String.valueOf(next));
        return next;
    }

    public void expire(String key, long ttl, TimeUnit unit) {
        if (redisTemplate != null) {
            try {
                redisTemplate.expire(key, ttl, unit);
                return;
            } catch (Exception ignored) {}
        }
        scheduler.schedule(() -> memoryStore.remove(key), ttl, unit);
    }
}
