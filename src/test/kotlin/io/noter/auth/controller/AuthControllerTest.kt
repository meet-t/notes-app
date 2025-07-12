package io.noter.auth.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.noter.auth.dto.AuthRequest
import io.noter.auth.dto.AuthResponse
import io.noter.auth.filter.JwtAuthenticationFilter
import io.noter.auth.service.AuthService
import io.noter.auth.service.JwtService
import io.noter.ratelimiter.service.RateLimiter
import io.noter.auth.dto.RegisterRequest
import io.noter.user.dto.UserDto
import org.hamcrest.Matchers.matchesRegex
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.util.*

@ExtendWith(MockKExtension::class)
@ExtendWith(SpringExtension::class)
@WebMvcTest(AuthController::class)
@ActiveProfiles("test")
class AuthControllerMockkTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var authService: AuthService

    @MockkBean
    lateinit var jwtService: JwtService

    @MockkBean
    lateinit var rateLimiter: RateLimiter

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `register returns 201 and user dto`() {
        val request = RegisterRequest( "john@example.com","John", "Password123!")
        val userDto = UserDto(UUID.randomUUID(), "John", "john@example.com")
        every { authService.register(any()) } returns userDto

        mockMvc.post("/api/v1/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { matchesRegex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}") }
            jsonPath("$.name") { value("John") }
            jsonPath("$.email") { value("john@example.com") }
        }
    }

    @Test
    fun `register with invalid email returns 400`() {
        val request = RegisterRequest( "not-an-email","John", "password123")

        mockMvc.post("/api/v1/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.code") { value("BAD_REQUEST") }
            jsonPath("$.details.email") { value("Email should be valid") }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "s"])
    fun `register with invalid name returns 400`(name: String) {
        val request = RegisterRequest( "john@example.com",name, "Password123!")

        mockMvc.post("/api/v1/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.code") { value("BAD_REQUEST") }
            jsonPath("$.details.name") { value("Name should have at least 2 characters") }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "short", "nouppercase123!", "NOLOWERCASE123!", "NoDigits!@#", "Password123"])
    fun `register with invalid password returns 400`(password: String) {
        val request = RegisterRequest( "john@example.com","John", password)

        mockMvc.post("/api/v1/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.code") { value("BAD_REQUEST") }
            jsonPath("$.details.password") { value("Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character") }
        }
    }


    @Test
    fun `valid login request returns 200 OK`() {

        val request = AuthRequest("john@example.com", "Password123!")
        every { authService.login(any()) } returns AuthResponse(
            token = UUID.randomUUID().toString(),
        )
        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.token") { matchesRegex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}") }
        }
    }

    @Test
    fun `invalid email returns BAD_REQUEST`() {
        val request = AuthRequest("invalid-email", "Password123!")

        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.code") { value("BAD_REQUEST") }
            jsonPath("$.message") { value("Validation failed") }
            jsonPath("$.details.email") { value("Email should be valid") }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "short", "nouppercase123!", "NOLOWERCASE123!", "NoDigits!@#", "Password123"])
    fun `invalid password returns BAD_REQUEST`(password: String) {
        val request = AuthRequest("john@example.com", password)

        mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.code") { value("BAD_REQUEST") }
            jsonPath("$.message") { value("Validation failed") }
            jsonPath("$.details.password") { value("Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character") }
        }
    }



}