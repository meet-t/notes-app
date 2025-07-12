package io.noter.user.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.UUID

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserDto(
    val id: UUID?,
    val name: String,
    val email: String
)
