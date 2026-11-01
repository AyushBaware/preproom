package com.preproom.engine.document;

import java.util.function.Supplier;

/** Retries a call that failed because the AI provider's rate limit was hit. */
public final class RateLimitRetry {

    private RateLimitRetry() {}

    public static <T> T call(Supplier<T> action, int maxAttempts, long waitMillis) {
        for (int attempt = 1; ; attempt++) {
            try {
                return action.get();
            } catch (RuntimeException e) {
                if (!isRateLimit(e) || attempt >= maxAttempts) {
                    throw e;
                }
                try {
                    Thread.sleep(waitMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
    }

    static boolean isRateLimit(Throwable t) {
        for (int depth = 0; t != null && depth < 10; depth++, t = t.getCause()) {
            String name = t.getClass().getSimpleName();
            String message = String.valueOf(t.getMessage()).toLowerCase();
            if (name.contains("RateLimit") || message.contains("429") || message.contains("rate limit")) {
                return true;
            }
        }
        return false;
    }
}