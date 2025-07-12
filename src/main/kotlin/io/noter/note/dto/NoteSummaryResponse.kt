package io.noter.note.dto

import java.time.Instant

data class NoteSummaryResponse(
    val id: Long,
    val title: String,
    val createdOn: Instant,
    val updatedOn: Instant,
    val expiresAt: Instant?
)

