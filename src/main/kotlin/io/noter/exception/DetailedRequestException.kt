package io.noter.exception

import org.springframework.http.HttpStatus

/** * Custom exception class to represent detailed request exceptions with HTTP status, code, and message.
 *
 * @property httpStatus The HTTP status associated with the exception.
 * @property code A specific error code for the exception.
 * @property msg A detailed message describing the exception.
 */
class DetailedRequestException : RuntimeException {

    val httpStatus : HttpStatus
    val code: String
    val msg: String


    constructor(httpStatus: HttpStatus, code: String,message: String,) : super() {
        this.httpStatus = httpStatus
        this.msg = message
        this.code = code
    }

}