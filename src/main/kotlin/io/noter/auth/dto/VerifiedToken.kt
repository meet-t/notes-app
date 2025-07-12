package io.noter.auth.dto

data class VerifiedToken(
    val userId: String,
    val name: String,
    val email: String,
)
