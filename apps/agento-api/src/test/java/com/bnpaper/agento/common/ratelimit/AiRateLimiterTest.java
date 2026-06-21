package com.bnpaper.agento.common.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

class AiRateLimiterTest {

    private AiRateLimiter limiter;

    @BeforeEach
    void setUp() {
        limiter = new AiRateLimiter();
        ReflectionTestUtils.setField(limiter, "maxCallsPerMinute", 3);
    }

    @Test
    void consume_allowsUpToLimit() {
        limiter.consume("user1");
        limiter.consume("user1");
        limiter.consume("user1");
        assertThat(limiter.remaining("user1")).isZero();
    }

    @Test
    void consume_throwsAfterLimit() {
        limiter.consume("user2");
        limiter.consume("user2");
        limiter.consume("user2");
        assertThatThrownBy(() -> limiter.consume("user2"))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessageContaining("Rate limit exceeded");
    }

    @Test
    void consume_differentKeyHasOwnLimit() {
        limiter.consume("user3");
        limiter.consume("user3");
        limiter.consume("user3");
        // user4 should still have its own limit
        assertThatCode(() -> limiter.consume("user4")).doesNotThrowAnyException();
    }

    @Test
    void remaining_startsAtMax() {
        assertThat(limiter.remaining("new-user")).isEqualTo(3);
    }

    @Test
    void remaining_decreasesAfterConsume() {
        limiter.consume("user5");
        assertThat(limiter.remaining("user5")).isEqualTo(2);
    }
}
