package com.i69app.data.remote.responses

data class ResponseBody<T>(
    val data: T?,
    val errorMessage: String? = null
)