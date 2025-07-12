package io.noter.auth.controller

import io.noter.auth.dto.AuthRequest
import io.noter.auth.dto.AuthResponse
import io.noter.auth.service.AuthService
import io.noter.auth.dto.RegisterRequest
import io.noter.user.dto.UserDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "User Authentication", description = "User authentication and registration APIs")
class AuthController(
    private val authService: AuthService
) {

    @Operation(summary = "User Registration", description = "User Registration")
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<UserDto> {
        val user = authService.register(request)
        return ResponseEntity(user, HttpStatus.CREATED)
    }

    @Operation(summary = "User Login", description = "User Login")
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: AuthRequest): ResponseEntity<AuthResponse> {
        val response = authService.login(request)
        return ResponseEntity.ok(response)
    }
}
