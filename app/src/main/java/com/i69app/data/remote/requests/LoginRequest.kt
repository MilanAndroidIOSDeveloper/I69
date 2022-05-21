package com.i69app.data.remote.requests

data class LoginRequest(
    val accessToken: String,
    val accessVerifier: String = "",
    val provider: String,
)