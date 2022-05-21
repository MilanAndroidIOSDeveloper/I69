package com.i69app.data.remote.responses

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val id: String,
    val email: String?,
    @SerializedName("isNew")
    val isNew: Boolean,
    val token: String,
    val username: String,

)