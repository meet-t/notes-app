package io.noter.note.controller

import io.noter.note.dto.NoteRequest
import io.noter.note.dto.NoteResponse
import io.noter.note.dto.NoteSummaryResponse
import io.noter.note.dto.PageDto
import io.noter.note.service.NoteService
import io.noter.util.Constants
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/notes")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Notes", description = "APIs for managing user notes")
class NoteController(
    private val noteService: NoteService
) {
    private val log = LoggerFactory.getLogger(javaClass)
    val allowedSortFields = listOf("createdOn", "updatedOn", "expiresAt")

    @Operation(
        summary = "Get All Notes",
        description = "Fetch all notes for the authenticated user, optionally paginated and sorted by default updated date."
    )
    @GetMapping
    fun getNotes(
        @RequestAttribute(Constants.USERID_ATTRIBUTE) userId: String,
        @RequestParam(required = false, value = "page") page: Int = 0,
        @RequestParam(required = false, value = "size") size: Int = 10,
        @RequestParam(defaultValue = "updatedOn") sortBy: String,
        @RequestParam(defaultValue = "desc") direction: String): ResponseEntity<PageDto<NoteSummaryResponse>> {

        val safeSortBy = if (sortBy in allowedSortFields) sortBy else "createdOn"
        val sortDirection = if (direction.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(page, size, Sort.by(sortDirection, safeSortBy))

        val notesList = noteService.findAllValidNotes(UUID.fromString(userId), pageable)
        return ResponseEntity.ok(notesList)
    }

    @Operation(
        summary = "Get Note by ID",
        description = "Retrieve the full details of a note by its ID, if it belongs to the authenticated user."
    )
    @GetMapping("/{id}")
    fun getNoteById(
        @RequestAttribute(Constants.USERID_ATTRIBUTE) userId: String,
        @PathVariable id: Long,
    ): ResponseEntity<NoteResponse> {
        log.debug("getting note by id $id")
        return ResponseEntity.ok(noteService.findByNoteIdAndUserId(UUID.fromString(userId), id))
    }

    @Operation(
        summary = "Create Note",
        description = "Creates a new note for the authenticated user"
    )
    @PostMapping
    fun createNote(
        @RequestAttribute(Constants.USERID_ATTRIBUTE) userId: String,
        @RequestBody @Valid request: NoteRequest,
    ): ResponseEntity<NoteResponse> {
        request.userId = UUID.fromString(userId)
        return ResponseEntity.ok(noteService.create(request))
    }

    @Operation(
        summary = "Update Note",
        description = "Update the title or content of an existing note. Only fields provided in the request will be updated."
    )
    @PutMapping("/{id}")
    fun updateNote(
        @RequestAttribute(Constants.USERID_ATTRIBUTE) userId: String,
        @PathVariable id: Long,
        @RequestBody @Valid request: NoteRequest,
    ): ResponseEntity<NoteResponse> {
        request.id = id
        request.userId = UUID.fromString(userId)
        return ResponseEntity.ok(noteService.update(request))
    }

    @Operation(
        summary = "Delete Note",
        description = "Delete a note by its ID. Only notes belonging to the authenticated user can be deleted."
    )
    @DeleteMapping("/{id}")
    fun deleteNote(
        @RequestAttribute(Constants.USERID_ATTRIBUTE) userId: String,
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        noteService.delete(UUID.fromString(userId), id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/latest")
    fun getLatestNotes(
        @RequestAttribute(Constants.USERID_ATTRIBUTE) userId: String,
    ): ResponseEntity<List<NoteSummaryResponse>> {
        val notesList = noteService.findLatestByUserId(UUID.fromString(userId))
        return ResponseEntity.ok(notesList)
    }
}
