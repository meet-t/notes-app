package io.noter.auth.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.noter.auth.dto.AuthRequest
import io.noter.auth.dto.AuthResponse
import io.noter.auth.dto.RegisterRequest
import io.noter.user.dto.UserDto
import io.noter.user.entity.User
import io.noter.user.service.UserService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@ExtendWith(MockKExtension::class)
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class AuthServiceImplTest {

    @InjectMockKs
    lateinit var authService: AuthServiceImpl


    @MockK
    lateinit var userService: UserService

    @MockK
    lateinit var passwordEncoder: PasswordEncoder

    @MockK
    lateinit var jwtService: JwtService


    @Test
    fun `register returns UserDto on success`() {
        val request = RegisterRequest("john@example.com", "John", "Password123!")
        val encodedPassword = "encodedPassword"
        val saveUser = User(id = null, name = "John", email = "john@example.com", password = encodedPassword)
        val savedUser = User(id = UUID.fromString("11111111-1111-1111-1111-111111111111"), name = "John", email = "john@example.com", password = encodedPassword)
        val userDto = UserDto(id = UUID.fromString("11111111-1111-1111-1111-111111111111"), name = "John", email = "john@example.com")

        every { userService.findByEmail(request.email) } returns null // Email not registered
        every { passwordEncoder.encode(request.password) } returns encodedPassword
        every { userService.save(saveUser) } returns savedUser
        val result = authService.register(request)
        assert(result == userDto)
    }

    @Test
    fun `register throws IllegalArgumentException if email is already registered`() {
        val request = RegisterRequest("john@example.com", "John", "Password123!")

        // Mocking the service to return an existing user with the same email
        every { userService.findByEmail(request.email) } returns User(id = UUID.randomUUID(), name = "John", email = "john@example.com", password = "encodedPassword")

        val exception = assertThrows<IllegalArgumentException> {
            authService.register(request)
        }
        assert(exception.message == "Email already registered")
    }

    @Test
    fun `login returns AuthResponse on successful login`() {
        val request = AuthRequest("john@example.com", "Password123!")
        val user = User(id = UUID.randomUUID(), name = "John", email = "john@example.com", password = "encodedPassword")
        val authResponse = AuthResponse("token")

        every { userService.findByEmail(request.email) } returns user
        every { passwordEncoder.matches(request.password, user.password) } returns true
        every { jwtService.generateToken(user) } returns "token"

        val result = authService.login(request)

        assert(result == authResponse)
    }

    @Test
    fun `login throws IllegalArgumentException for invalid email`() {
        val request = AuthRequest("john@example.com", "WrongPassword!")

        // Mocking the services to simulate invalid credentials
        every { userService.findByEmail(request.email) } returns null // Email not found

        val exception = assertThrows<IllegalArgumentException> {
            authService.login(request)
        }
        assert(exception.message == "Invalid email or password")
    }


}
