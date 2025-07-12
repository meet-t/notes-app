package io.noter.note.dto

import java.time.Instant

data class NoteResponse(
    val id: Long?,
    val title: String,
    val content: String?,
    val createdOn: Instant?,
    val updatedOn: Instant?,
    val expiresAt: Instant?
)