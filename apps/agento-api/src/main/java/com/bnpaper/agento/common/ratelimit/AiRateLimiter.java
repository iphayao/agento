package com.bnpaper.agento.common.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-process sliding-window rate limiter for AI generation endpoints.
 *
 * Tracks calls per key (user or IP) in a 1-minute window.
 * Sufficient for a solo-founder internal tool; swap for Redis-backed Bucket4j in high-scale.
 */
@Component
public class AiRateLimiter {

    @Value("${agento.rate-limit.ai-calls-per-minute:10}")
    private int maxCallsPerMinute;

    private record Window(AtomicInteger count, long windowStart) {}

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    /**
     * Attempt to consume a rate-limit token for the given key.
     *
     * @param key typically the authenticated username or remote IP
     * @throws RateLimitExceededException if the limit is exceeded
     */
    public void consume(String key) {
        long nowMs = System.currentTimeMillis();
        long windowStart = nowMs - (nowMs % 60_000L);

        windows.compute(key, (k, existing) -> {
            if (existing == null || existing.windowStart() != windowStart) {
                return new Window(new AtomicInteger(1), windowStart);
            }
            int current = existing.count().incrementAndGet();
            if (current > maxCallsPerMinute) {
                throw new RateLimitExceededException(
                        "Rate limit exceeded: max " + maxCallsPerMinute + " AI calls per minute");
            }
            return existing;
        });
    }

    /** Returns the remaining token count for the current window without consuming. */
    public int remaining(String key) {
        long nowMs = System.currentTimeMillis();
        long windowStart = nowMs - (nowMs % 60_000L);
        Window w = windows.get(key);
        if (w == null || w.windowStart() != windowStart) return maxCallsPerMinute;
        return Math.max(0, maxCallsPerMinute - w.count().get());
    }
}
