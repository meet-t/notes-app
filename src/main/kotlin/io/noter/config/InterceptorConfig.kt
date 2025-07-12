package io.noter.config

import io.noter.ratelimiter.service.RateLimitInterceptor
import org.springframework.stereotype.Service
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/** * Configuration class to register the RateLimitInterceptor.
 * This interceptor will apply rate limiting to specific API endpoints.
 */
@Service
class InterceptorConfig(private val rateLimitInterceptor: RateLimitInterceptor): WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(rateLimitInterceptor)
            .addPathPatterns("/api/v1/notes/**") // Or all secured endpoints
    }
}