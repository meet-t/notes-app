package io.noter.note.repo

import io.noter.note.dto.NoteSummaryResponse
import io.noter.note.entity.Note
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface NoteRepository : JpaRepository<Note, Long> {

    fun findByIdAndUserId(id: Long, userId: UUID): Note?

    @Query("""
        SELECT new io.noter.note.dto.NoteSummaryResponse(
            n.id, n.title, n.createdOn, n.updatedOn, n.expiresAt
        )
        FROM Note n
        WHERE n.userId = :userId 
          AND (n.expiresAt IS NULL OR n.expiresAt > :now)
    """)
    fun findValidNotesByUserId(userId: UUID, now: Instant,pageable: Pageable): Slice<NoteSummaryResponse>

}