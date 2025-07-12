package io.noter.ratelimiter.service

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

/* * InMemoryRateLimiter is a service that implements the RateLimiter interface.
 * It uses an in-memory bucket to track rate limits for users based on their userId and the requested path.
 * This implementation is intended for non-production environments.
 */

@Service
@Profile("!prod")
class InMemoryRateLimiter(
    private val rateLimitResolver: RateLimitResolver
) : RateLimiter {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun isAllowed(userId: UUID, path: String): Pair<Boolean, Int> {
        log.debug("Checking isAllowed for $path")
        val bucket = rateLimitResolver.resolveBucket(path)
        val probe = bucket.tryConsumeAndReturnRemaining(1)
        return probe.isConsumed to probe.remainingTokens.toInt()
    }

}