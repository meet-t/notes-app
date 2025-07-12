package io.noter.config

import io.noter.auth.filter.JwtAuthenticationFilter
import io.noter.auth.service.JwtService
import io.noter.ratelimiter.service.InMemoryRateLimiter
import io.noter.ratelimiter.service.RateLimitInterceptor
import io.noter.ratelimiter.service.RateLimitResolver
import io.noter.ratelimiter.service.RateLimiter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
@Import(JwtService::class, RateLimitResolver::class)
class NotesAppTestConfig {


    @Bean
    fun jwtAuthenticationFilter(jwtService: JwtService): JwtAuthenticationFilter {
        return JwtAuthenticationFilter(jwtService)
    }

    @Bean
    fun filterRegistration(jwtAuthenticationFilter: JwtAuthenticationFilter): FilterRegistrationBean<JwtAuthenticationFilter> {
        val registrationBean = FilterRegistrationBean(jwtAuthenticationFilter)
        registrationBean.addUrlPatterns("/api/v1/notes/*")
        return registrationBean
    }


    @Bean
    fun rateLimiter(rateLimitResolver: RateLimitResolver): RateLimiter {
        return InMemoryRateLimiter(rateLimitResolver)
    }

    @Bean
    fun rateLimitInterceptor(rateLimiter: RateLimiter): RateLimitInterceptor {
        return RateLimitInterceptor(rateLimiter)
    }

    @Bean
    fun interceptorConfig(rateLimitInterceptor: RateLimitInterceptor): InterceptorConfig {
        return InterceptorConfig(rateLimitInterceptor)
    }
}