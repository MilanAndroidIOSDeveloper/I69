package com.i69app.data.remote.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GraphqlApi {

    /// Graphql Api
    @POST("/")
    suspend fun callApi(
        @Header("Authorization") token: String?,
        @Body body: String?
    ): Response<String>

}