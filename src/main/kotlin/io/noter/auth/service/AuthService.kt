package io.noter.auth.service

import io.noter.auth.dto.AuthRequest
import io.noter.auth.dto.AuthResponse
import io.noter.auth.dto.RegisterRequest
import io.noter.user.dto.UserDto
import org.springframework.stereotype.Service

/**
 * Service interface for handling authentication-related operations.
 * Provides methods for user registration and login.
 */
@Service
interface AuthService {
    fun register(request: RegisterRequest): UserDto

    fun login(request: AuthRequest): AuthResponse
}
