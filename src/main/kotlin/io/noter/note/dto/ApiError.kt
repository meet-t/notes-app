package io.noter.note.dto

/**
 * A data class representing an API error response.
 *
 * @property code a unique error code identifying the type of error.
 * @property message a human-readable message describing the error.
 * @property details optional additional details about the error.
 */
data class ApiError(
    val code: String,
    val message: String,
    val details: Any? = null
)

