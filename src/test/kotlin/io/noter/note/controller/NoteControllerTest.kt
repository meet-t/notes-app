package io.noter.note.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.verify
import io.noter.config.NotesAppTestConfig
import io.noter.exception.DetailedRequestException
import io.noter.note.dto.NoteRequest
import io.noter.note.dto.NoteResponse
import io.noter.note.dto.NoteSummaryResponse
import io.noter.note.service.NoteService
import io.noter.ratelimiter.enum.RateLimitPlan
import io.noter.util.Constants
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@ExtendWith(MockKExtension::class)
@ExtendWith(SpringExtension::class)
@WebMvcTest(NoteController::class)
@Import(NotesAppTestConfig::class)
@ActiveProfiles("test")
class NoteControllerTest(
    @Value("\${app.test.jwt.userId}") val userId: String,
    @Value("\${app.test.jwt.token}") val validToken: String,
) {
    val mapper = ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var noteService: NoteService


    @Test
    fun `getNotes returns 401 Unauthorized when no auth token is provided`() {
        mockMvc.get("/api/v1/notes") {
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.code") { value(Constants.INVALID_TOKEN) }
            jsonPath("$.message") { value("Authentication required!") }
        }
    }

    @Test
    fun `getNotes returns 401 Unauthorized when invalid token is provided`() {
        mockMvc.get("/api/v1/notes") {
            header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.code") { value(Constants.INVALID_TOKEN) }
            jsonPath("$.message") { value("Token verification failed") }
        }
    }

    @Test
    fun `getNoteById returns 404 Not Found if note does not exist`() {
        val noteId = 999L

        val message = "Note not found for id $noteId and user $userId"
        every { noteService.findByNoteIdAndUserId(UUID.fromString(userId), noteId) } throws
                DetailedRequestException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.name, message)

        mockMvc.get("/api/v1/notes/$noteId") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $validToken")
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.code") { value(HttpStatus.NOT_FOUND.name) }
            jsonPath("$.message") { value(message) }
        }
    }

    @Test
    fun `getNoteById returns 200 OK with valid note ID`() {
        val noteId = 1L
        val curTime = Instant.now()
        val noteResponse = NoteResponse(noteId, "Sample Note", "Note content", curTime, curTime,curTime.plus(1, ChronoUnit.HOURS))

        // Mock service call
        every { noteService.findByNoteIdAndUserId(UUID.fromString(userId), noteId) } returns noteResponse

        mockMvc.get("/api/v1/notes/$noteId") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $validToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(noteId) }
            jsonPath("$.title") { value("Sample Note") }
        }
    }


    @Test
    fun `updateNote returns 200 OK when note is updated successfully`() {
        val curTime = Instant.now()
        val noteId = 1L

        val noteRequest = NoteRequest(null,"Updated Title", "Note content", curTime.plus(1, ChronoUnit.HOURS))
        val updatedNoteResponse = NoteResponse(noteId, "Updated Title", "Updated Content", curTime,curTime,curTime.plus(1, ChronoUnit.HOURS))

        every { noteService.update(any()) } returns updatedNoteResponse

        mockMvc.put("/api/v1/notes/$noteId") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $validToken")
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(noteRequest)
        }.andExpect {
            status { isOk() }
            jsonPath("$.id") { value(noteId) }
            jsonPath("$.title") { value("Updated Title") }
        }
    }

    @Test
    fun `updateNote returns 400 Bad Request if note does not exist`() {
        val noteId = 1L
        val curTime = Instant.now()
        val noteRequest = NoteRequest(noteId,"Updated Title", "Note content", curTime.plus(1, ChronoUnit.HOURS), userId=UUID.fromString(userId))
        val message = "Note not found for id $noteId and user $userId"
        every { noteService.update(noteRequest) } throws IllegalArgumentException(message)

        mockMvc.put("/api/v1/notes/$noteId") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $validToken")
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(noteRequest)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.code") { value(HttpStatus.BAD_REQUEST.name) }
            jsonPath("$.message") { value(message) }
        }
    }

    @Test
    fun `deleteNote returns 204 No Content when note is deleted successfully`() {
        val noteId = 1L
        every { noteService.delete(UUID.fromString(userId), noteId) } just Runs

        mockMvc.delete("/api/v1/notes/$noteId") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $validToken")
        }.andExpect {
            status { isNoContent() }
        }
    }

    @Test
    fun `deleteNote returns 400 Bad Request if note does not exist`() {
        val noteId = 999L
        val message = "Note not found for id $noteId and user $userId"
        every { noteService.delete(UUID.fromString(userId), noteId) } throws
                IllegalArgumentException(message)

        mockMvc.delete("/api/v1/notes/$noteId") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $validToken")
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.code") { value(HttpStatus.BAD_REQUEST.name) }
            jsonPath("$.message") { value(message) }
        }
    }


    @Test
    fun `createNote returns 429 Too Many Requests after exceeding rate limit`() {
        val curTime = Instant.now()
        val noteRequest = NoteRequest(null,"Sample Note", "Note content", curTime.plus(1, ChronoUnit.HOURS))
        val noteResponse = NoteResponse(1L, "Sample Note", "Note content", curTime,curTime,curTime.plus(1, ChronoUnit.HOURS))

        every { noteService.create(any()) } returns noteResponse
        val numberOfRequests = 5
        var successfulRequests = 0
        repeat (numberOfRequests) {
            mockMvc.post("/api/v1/notes") {
                contentType = MediaType.APPLICATION_JSON
                content = mapper.writeValueAsString(noteRequest)
                header(HttpHeaders.AUTHORIZATION, "Bearer $validToken")
            }.andExpect {
                status { isOk() }
                jsonPath("$.id") { value(1L) }
            }
            successfulRequests++
        }
        verify(exactly = successfulRequests) { noteService.create(any()) }
        // Simulate rate limit exceeded on the next request
        mockMvc.post("/api/v1/notes") {
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(noteRequest)
            header(HttpHeaders.AUTHORIZATION, "Bearer $validToken")
        }.andExpect {
            status { isTooManyRequests() }
            jsonPath("$.code") { value(HttpStatus.TOO_MANY_REQUESTS.name) }
            jsonPath("$.message") { value("Rate limit exceeded. Try again later.") }
        }
        verify(exactly = successfulRequests) { noteService.create(any()) }


    }

    @Test
    fun `getLatestNotes returns 200 OK with valid user `() {
        val latestNotes = listOf(NoteSummaryResponse(1L, "Latest Note", Instant.now(), Instant.now(), null))

        every { noteService.findLatestByUserId(UUID.fromString(userId)) } returns latestNotes
        mockMvc.get("/api/v1/notes/latest") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $validToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.size()") { value(1) }
        }
    }

    @Test
    fun `getLatestNotes returns 429 Too Many Requests after exceeding rate limit `() {
        val latestNotes = listOf(NoteSummaryResponse(1L, "Latest Note", Instant.now(), Instant.now(), null))

        every { noteService.findLatestByUserId(UUID.fromString(userId)) } returns latestNotes
        // Simulate rate limit exceeded, minus one request as we have already made one successful request in the previous test case
        val numberOfRequests = RateLimitPlan.resolvePlanFromPath("/api/v1/notes/latest").getLimit().capacity-1
        repeat (numberOfRequests.toInt()) {
            mockMvc.get("/api/v1/notes/latest") {
                header(HttpHeaders.AUTHORIZATION, "Bearer $validToken")
            }.andExpect {
                status { isOk() }
                jsonPath("$.size()") { value(1) }
            }
        }
        verify(exactly = numberOfRequests.toInt()) {
            noteService.findLatestByUserId(UUID.fromString(userId)) }
        mockMvc.get("/api/v1/notes/latest") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $validToken")
        }.andExpect {
            status { isTooManyRequests() }
            jsonPath("$.code") { value(HttpStatus.TOO_MANY_REQUESTS.name) }
            jsonPath("$.message") { value("Rate limit exceeded. Try again later.") }
        }

    }

}