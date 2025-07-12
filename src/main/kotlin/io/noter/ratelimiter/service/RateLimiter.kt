package io.noter.ratelimiter.service

import org.springframework.stereotype.Service
import java.util.UUID

/**
 * RateLimiter interface defines the contract for rate limiting services.
 * Implementations of this interface should provide a mechanism to check if a user is allowed to perform an action
 * based on their userId and the requested path.
 */
@Service
interface RateLimiter {
    fun isAllowed(userId: UUID, path: String): Pair<Boolean, Int>
}