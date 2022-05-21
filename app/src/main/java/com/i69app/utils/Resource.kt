package com.i69app.utils

import com.i69app.data.enums.HttpStatusCode

sealed class Resource<T>(val code: com.i69app.data.enums.HttpStatusCode? = null, val data: T? = null, val message: String? = null) {

    class Success<T>(code: com.i69app.data.enums.HttpStatusCode, data: T) : Resource<T>(code, data)

    class Error<T>(code: com.i69app.data.enums.HttpStatusCode, data: T? = null, message: String) : Resource<T>(code, data, message)

    class Loading<T>(data: T? = null) : Resource<T>(data = data)

}