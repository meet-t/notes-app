package io.noter.exception.handler

import io.noter.exception.DetailedRequestException
import io.noter.note.dto.ApiError
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

/** * Global exception handler for all exceptions.
 * Handles various exceptions and returns appropriate HTTP responses.
 */
@RestControllerAdvice
class NoteExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(ex: IllegalArgumentException): ResponseEntity<ApiError> {
        return ResponseEntity.badRequest().body(ApiError(
            code = "BAD_REQUEST",
            message = ex.localizedMessage ?: "bad request"
        ))
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiError> {
        val errors = ex.bindingResult.fieldErrors.associate { it.field to it.defaultMessage }
        log.info("Validation message: {}", ex.localizedMessage)
        log.info("Validation Errors: {}", errors)
        return ResponseEntity.badRequest().body(ApiError(
            code = "BAD_REQUEST",
            message = "Validation failed",
            details = errors
        ))
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    fun handleGenericError(ex: Exception): ResponseEntity<ApiError> {
        return ResponseEntity
            .internalServerError()
            .body(
                ApiError(
                    code = "INTERNAL_ERROR",
                    message = ex.localizedMessage ?: "An unexpected error occurred"
                )
            )
    }

    // Handle Spring DataAccessException (parent of all DB-related exceptions)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(DataAccessException::class)
    fun handleDatabaseError(ex: DataAccessException): ResponseEntity<ApiError> {
        log.error("Database error occurred: ${ex.message}", ex)

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiError(
                code = "DATABASE_ERROR",
                message = "A database error occurred. Please try again later."
            )
        )
    }

    @ExceptionHandler(DetailedRequestException::class)
    fun handleDetailedRequestException(ex: DetailedRequestException): ResponseEntity<ApiError> {
        return ResponseEntity
            .status(ex.httpStatus)
            .body(
                ApiError(
                    code = ex.code,
                    message = ex.msg
                )
            )
    }
}