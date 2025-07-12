package io.noter.ratelimiter.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.noter.note.dto.ApiError
import io.noter.util.Constants
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.util.*

@Component
class RateLimitInterceptor(
    private val rateLimiter: RateLimiter
) : HandlerInterceptor {

    private val log = LoggerFactory.getLogger(javaClass)

    val mapper = ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        log.debug("Inside Rate Limiter")
        val path = request.requestURI
        val userId = request.getAttribute(Constants.USERID_ATTRIBUTE) ?: return true // Skip unauthenticated (or limit IP-based here)
        log.debug("Inside Rate Limiter for userId: {}", userId)
        if(userId !is String) {
            writeUnauthorized(response)
            return false
        }
        val (allowed, remaining) = rateLimiter.isAllowed(UUID.fromString(userId), path)
        response.setHeader("X-RateLimit-Remaining", remaining.toString())
        log.debug("Rate Limiter for userId: {} allowed {} remaining {}", userId, allowed,remaining)
        if (!allowed) {
            writeRateLimitExceeded(response)
            return false
        }

        return true
    }

    private fun writeRateLimitExceeded(response: HttpServletResponse) {
        response.status = HttpStatus.TOO_MANY_REQUESTS.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        val errorDto = ApiError(
            code = HttpStatus.TOO_MANY_REQUESTS.name,
            message = "Rate limit exceeded. Try again later."
        )
        response.writer.write(mapper.writeValueAsString(errorDto))
    }

    private fun writeUnauthorized(response: HttpServletResponse) {
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        val errorDto = ApiError(
            code = HttpStatus.UNAUTHORIZED.name,
            message = "Invalid Authentication!"
        )
        response.writer.write(mapper.writeValueAsString(errorDto))
    }
}
