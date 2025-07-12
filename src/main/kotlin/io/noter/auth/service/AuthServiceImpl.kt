package io.noter.auth.service

import io.noter.auth.dto.AuthRequest
import io.noter.auth.dto.AuthResponse
import io.noter.auth.dto.RegisterRequest
import io.noter.user.dto.UserDto
import io.noter.user.entity.User
import io.noter.user.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

/**
 * Implementation of the AuthService interface for handling user authentication.
 * Provides methods for user registration and login, including password encoding and JWT token generation.
 *
 * @property userService Service for user-related operations.
 * @property passwordEncoder Password encoder for hashing passwords.
 * @property jwtService Service for generating JWT tokens.
*/
@Service
class AuthServiceImpl(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService) : AuthService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun register(request: RegisterRequest): UserDto {
        if (userService.findByEmail(request.email) != null) {
            throw IllegalArgumentException("Email already registered")
        }

        val user = User(
            name = request.name,
            email = request.email,
            password = passwordEncoder.encode(request.password)
        )
        log.info("Registering user with email: ${user.password}")
        val savedUser = userService.save(user)
        return UserDto(
            id = savedUser.id,
            name = savedUser.name,
            email = savedUser.email,
        )
    }

    override fun login(request: AuthRequest): AuthResponse {
        val user = userService.findByEmail(request.email)
            ?: throw IllegalArgumentException("Invalid email or password")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("Invalid email or password")
        }

        val token = jwtService.generateToken(user)
        return AuthResponse(token)
    }

}