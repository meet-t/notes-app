package io.noter.ratelimiter.service

import io.github.bucket4j.Bucket
import io.noter.ratelimiter.enum.RateLimitPlan
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * RateLimitResolver is a service that resolves rate limit buckets based on the requested path.
 * It uses a cache to store and retrieve buckets for different paths, creating new buckets as needed.
 * This implementation is intended for non-production environments.
 */

@Component
class RateLimitResolver {

    private val cache = ConcurrentHashMap<String, Bucket>()

    /**
     * Resolves a Bucket for the given path.
     * If a bucket for the path already exists in the cache, it returns that bucket.
     * Otherwise, it creates a new bucket based on the rate limit plan associated with the path.
     *
     * @param path The requested path for which to resolve the bucket.
     * @return The resolved Bucket for the given path.
     */

    fun resolveBucket(path: String): Bucket {
        return cache.computeIfAbsent(path, ::newBucket)
    }

    private fun newBucket(path: String): Bucket {
        val rateLimitConfig = RateLimitPlan.resolvePlanFromPath(path )
        return Bucket.builder().addLimit(rateLimitConfig.getLimit()).build()

    }
}
