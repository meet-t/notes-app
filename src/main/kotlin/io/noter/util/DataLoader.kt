package io.noter.util

import io.noter.note.entity.Note
import io.noter.note.repo.NoteRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

/**
 * DataLoader is a component that populates the database with sample notes for testing purposes.
 * It runs at application startup if the H2 in-memory database is used.
 */
@Component
@ConditionalOnProperty(name = ["spring.datasource.url"], havingValue = "jdbc:h2:mem:notesdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE", matchIfMissing = true)
class DataLoader(private val noteRepository: NoteRepository) : CommandLineRunner {
    private val log = LoggerFactory.getLogger(javaClass)
    override fun run(vararg args: String?) {

        val numberOfNotes= 1100
        log.info("Creating $numberOfNotes new notes")
        val notes = (1..numberOfNotes).map {
            Note(
                userId = UUID.fromString("11111111-1111-1111-1111-111111111111"),
                title = "Sample Note $it",
                content = "Content of the note $it",
                createdOn = Instant.now()
                    .minusSeconds((Math.random() * 60 * 60 * 24 * 30).toLong()),  // Random past date within 30 days
                updatedOn = Instant.now()
                    .minusSeconds((Math.random() * 60 * 60 * 24 * 10).toLong()),  // Random past date within 10 days
                expiresAt = Instant.now().plusSeconds(60 * 60 * 24 * 7) // Set expiration 7 days in the future
            )
        }

        noteRepository.saveAll(notes)
        log.info("Completed $numberOfNotes new notes creation!")
    }
}
