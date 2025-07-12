package io.noter.auth.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.noter.auth.service.JwtService
import io.noter.exception.DetailedRequestException
import io.noter.note.dto.ApiError
import io.noter.util.Constants
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Filter to handle JWT authentication for incoming requests.
 */
@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
) : OncePerRequestFilter(){

    private val log = LoggerFactory.getLogger(javaClass)

    val mapper = ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())

    // Skip filter for authentication endpoints
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return request.requestURI.startsWith("/api/v1/auth") ||
                request.requestURI.startsWith("/h2-console") ||
                request.requestURI.startsWith("/swagger-ui") ||
                request.requestURI.startsWith("/v3/api-docs")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        log.debug("JWT Authentication Filter triggered for request: ${request.requestURI}")

        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (authHeader.isNullOrBlank()) {
            writeUnauthorized(response)
            return
        } else {
            val token = parseToken(authHeader)
            val verifiedToken = try {
                jwtService.verifyJwt(token)
            } catch (e: DetailedRequestException){
                response.status = e.httpStatus.value()
                val errorDto = ApiError(code = e.code, message = e.msg)
                val error = mapper.writeValueAsString(errorDto)
                response.writer.write(error)
                return
            }
            request.setAttribute(Constants.USERID_ATTRIBUTE, verifiedToken.userId)
            request.setAttribute(Constants.EMAIL_ATTRIBUTE, verifiedToken.email)
            request.setAttribute(Constants.NAME_ATTRIBUTE, verifiedToken.name)
            log.debug("request attributes set in filter")
            filterChain.doFilter(request, response)
        }
    }

    private fun parseToken(header: String): String {
        val start = header.indexOf(Constants.BEARER)
        return if (start < 0) header else header.substring(start + Constants.BEARER.length + 1)
    }


    private fun writeUnauthorized(response: HttpServletResponse) {
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        val errorDto = ApiError(
            code = Constants.INVALID_TOKEN,
            message = "Authentication required!"
        )
        response.writer.write(mapper.writeValueAsString(errorDto))
    }
}