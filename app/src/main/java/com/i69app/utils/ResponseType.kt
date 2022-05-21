package com.i69app.utils

data class ResponseType<out T>(val status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T): ResponseType<T> = ResponseType(status = Status.SUCCESS, data = data, message = null)

        fun <T> error(data: T?, message: String): ResponseType<T> =
            ResponseType(status = Status.ERROR, data = data, message = message)

        fun <T> loading(data: T?): ResponseType<T> = ResponseType(status = Status.LOADING, data = data, message = null)
    }
}