package io.noter.note.dto

import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.util.*

data class NoteRequest (

    var id: Long? = null,

    @field:NotBlank
    val title: String,

    val content: String?=null,

    val expiresAt: Instant? = null,

    var userId: UUID? = null
)