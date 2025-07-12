package io.noter.auth.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.JWTVerifier
import io.noter.auth.dto.VerifiedToken
import io.noter.exception.DetailedRequestException
import io.noter.user.entity.User
import io.noter.util.Constants
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Service for handling JWT operations i.e generating and verifying tokens.
 */
@Service
class JwtService(
    @Value("\${app.jwt.secret}") private val jwtSecret: String,
    @Value("\${app.jwt.expirationMinutes}") private val jwtExpirationMinutes: Long,
    @Value("\${app.jwt.issuer}") private val jwtIssuer: String,
    @Value("\${app.jwt.audience}") val audience: String,
    @Value("\${app.jwt.claim.userId}") val userIdClaim: String,
    @Value("\${app.jwt.claim.name}") val nameClaim: String,
    @Value("\${app.jwt.claim.email}") val emailClaim: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)


    fun generateToken(user: User): String {
        log.debug("generate: JWT for {} userId ", user.id)

        return JWT.create()
            .withIssuer(jwtIssuer)
            .withAudience(audience)
            .withExpiresAt(Instant.now().plus(jwtExpirationMinutes, ChronoUnit.MINUTES))
            .withClaim(userIdClaim, user.id.toString())
            .withClaim(nameClaim, user.name)
            .withClaim(emailClaim, user.email)
            .sign(Algorithm.HMAC512(jwtSecret))
    }

    fun verifyJwt(token: String): VerifiedToken {

        val verifier: JWTVerifier = JWT.require(Algorithm.HMAC512(jwtSecret))
            .withIssuer(jwtIssuer)
            .build()

        val decodedJWT = try {
             verifier.verify(token)
        } catch (e: JWTVerificationException) {
            log.error("verification failed with {} error", arrayOf(e.localizedMessage))
            throw DetailedRequestException(HttpStatus.UNAUTHORIZED, Constants.INVALID_TOKEN,"Token verification failed")
        }

        if (decodedJWT.audience.none { it == audience }) {
            log.error("audience mismatched {} expected and {} actual", audience, decodedJWT.audience)
            throw DetailedRequestException(HttpStatus.UNAUTHORIZED, Constants.INVALID_TOKEN,"Invalid audience in token")
        }

        val userId = decodedJWT.getClaim(userIdClaim)
        if (userIdClaim.isBlank()) {
            log.debug("uid is empty")
            throw DetailedRequestException(HttpStatus.UNAUTHORIZED, Constants.INVALID_TOKEN,"Invalid token: User ID is missing")
        }

        val name = decodedJWT.getClaim(nameClaim)
        val email = decodedJWT.getClaim(emailClaim)

        return VerifiedToken(userId.asString(),name.asString(),email.asString())
    }


}