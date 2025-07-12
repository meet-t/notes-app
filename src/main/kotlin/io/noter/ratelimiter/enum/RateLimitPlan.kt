package io.noter.ratelimiter.enum

import io.github.bucket4j.Bandwidth
import java.time.Duration

/**
 * Enum representing different rate limit plans.
 * Each plan defines its own bandwidth limits and refill intervals for Bucket4j.
 */
enum class RateLimitPlan {
    FREE {
        override fun getLimit(): Bandwidth = Bandwidth.builder()
                .capacity(2)
                .refillIntervally(2, Duration.ofMinutes(1))
                .build()
        },
    BASIC {
        override fun getLimit(): Bandwidth = Bandwidth.builder()
            .capacity(5)
            .refillIntervally(5, Duration.ofMinutes(1))
            .build()
    };

    companion object {
        fun resolvePlanFromPath(path: String): RateLimitPlan {
            if (path.isEmpty() || path.startsWith("/api/v1/notes/latest")) {
                return FREE
            } else if (path.startsWith("/api/v1/notes")) {
                return BASIC
            }
            return FREE
        }
    }

    abstract fun getLimit(): Bandwidth

}