package io.noter.note.service

import io.noter.note.dto.NoteRequest
import io.noter.note.dto.NoteResponse
import io.noter.note.dto.NoteSummaryResponse
import io.noter.note.dto.PageDto
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.util.UUID

@Service
interface NoteService {
    fun create(request: NoteRequest): NoteResponse

    fun update(request: NoteRequest): NoteResponse

    fun delete(userId: UUID, noteId: Long)

    fun findByNoteIdAndUserId(userId: UUID, noteId: Long): NoteResponse

    fun findAllValidNotes(userId: UUID, pageable: Pageable): PageDto<NoteSummaryResponse>

    fun findLatestByUserId(userId: UUID): List<NoteSummaryResponse>
}