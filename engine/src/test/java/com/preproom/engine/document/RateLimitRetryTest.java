package com.preproom.engine.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class RateLimitRetryTest {

    @Test
    void retriesRateLimitErrorsThenSucceeds() {
        int[] calls = {0};
        String result = RateLimitRetry.call(() -> {
            calls[0]++;
            if (calls[0] < 3) {
                throw new RuntimeException("429 Too Many Requests");
            }
            return "ok";
        }, 3, 1);
        assertThat(result).isEqualTo("ok");
        assertThat(calls[0]).isEqualTo(3);
    }

    @Test
    void otherErrorsAreNotRetried() {
        int[] calls = {0};
        assertThatThrownBy(() -> RateLimitRetry.call(() -> {
            calls[0]++;
            throw new RuntimeException("unreadable answer");
        }, 3, 1)).isInstanceOf(RuntimeException.class);
        assertThat(calls[0]).isEqualTo(1);
    }
}