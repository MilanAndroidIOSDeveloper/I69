package com.i69app.data.enums

enum class HttpStatusCode(val statusCode: Int) {
    OK(200),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    INTERNAL_SERVER_ERROR(500)
}