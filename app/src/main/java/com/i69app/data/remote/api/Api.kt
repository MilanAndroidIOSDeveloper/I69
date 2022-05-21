package com.i69app.data.remote.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import com.i69app.data.remote.responses.ImageResult

interface Api {

    /// Upload Image
    @Multipart
    @POST("/api/user/{userId}/photo/")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Path("userId") userId: String,
        @Part image: MultipartBody.Part
    ): Response<ImageResult>

}