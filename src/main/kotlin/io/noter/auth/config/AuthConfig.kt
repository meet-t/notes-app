package io.noter.auth.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * Configuration class for authentication-related beans.
 */
@Configuration
class AuthConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        val passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
        return passwordEncoder
    }
}