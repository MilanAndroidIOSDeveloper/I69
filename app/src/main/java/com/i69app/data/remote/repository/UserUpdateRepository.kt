package com.i69app.data.remote.repository

import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import com.i69app.R
import com.i69app.data.models.Id
import com.i69app.data.models.User
import com.i69app.data.remote.api.Api
import com.i69app.data.remote.api.GraphqlApi
import com.i69app.data.remote.requests.ReportRequest
import com.i69app.data.remote.responses.ResponseBody
import com.i69app.singleton.App
import com.i69app.utils.Resource
import com.i69app.utils.getResponse
import com.i69app.utils.getStringFromList
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserUpdateRepository @Inject constructor(
    private val graphqlApi: GraphqlApi,
    private val api: Api,
) {

    /// Update Profile
    suspend fun updateProfile(user: User, token: String): Resource<ResponseBody<Id>> {
        val books = user.books?.getStringFromList()
        val movies = user.movies?.getStringFromList()
        val music = user.music?.getStringFromList()
        val sportsTeams = user.sportsTeams?.getStringFromList()
        val tvShows = user.tvShows?.getStringFromList()

        val queryName = "updateProfile"
        val query = StringBuilder()
            .append("mutation {")
            .append("$queryName (")
            .append("id: \"${user.id}\", ")
            .append("fullName: \"${user.fullName}\", ")
            .append("email: \"${user.email}\", ")
            .append(if (user.about.isNullOrEmpty()) "" else "about: \"${user.about}\", ")
            .append(if (user.age == null) "" else "age: ${user.age}, ")
            .append(if (user.avatarIndex == null) "" else "avatarIndex: ${user.avatarIndex}, ")
            .append(if (user.gender == null) "" else "gender: ${user.gender}, ")
            .append(if (user.height == null) "" else "height: ${user.height}, ")
            .append(if (books.isNullOrEmpty()) "" else "book: $books, ")
            .append(if (user.education.isNullOrEmpty()) "" else "education: \"${user.education}\", ")
            .append(if (user.ethnicity == null) "" else "ethinicity: ${user.ethnicity}, ")
            .append(if (user.familyPlans == null) "" else "familyPlans: ${user.familyPlans}, ")
            .append(if (user.interestedIn.isNullOrEmpty()) "" else "interestedIn: ${user.interestedIn.toList()}, ")
            .append(if (movies.isNullOrEmpty()) "" else "movies: $movies, ")
            .append(if (music.isNullOrEmpty()) "" else "music: $music, ")
            .append(if (user.religion == null) "" else "religion: ${user.religion}, ")
            .append(if (user.politics == null) "" else "politics: ${user.politics}, ")
            .append(if (sportsTeams.isNullOrEmpty()) "" else "sportsTeams: $sportsTeams, ")
            .append(if (user.tags.isNullOrEmpty()) "" else "tagIds: ${user.tags.toList()}, ")
            .append(if (tvShows.isNullOrEmpty()) "" else "tvShows: ${tvShows}, ")
            .append(if (user.work.isNullOrEmpty()) "" else "work: \"${user.work}\", ")
            .append(if (user.zodiacSign == null) "" else "zodiacSign: ${user.zodiacSign}")
            .append(") {")
            .append("id")
            .append("}")
            .append("}")
            .toString()

        return graphqlApi.getResponse(query, queryName, token)
    }

    suspend fun uploadImage(userId: String, token: String, filePath: String): Resource<ResponseBody<Id>> {
        try {
            val file = File(filePath)
            val requestBody: RequestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val image = MultipartBody.Part.createFormData("photo", file.name ?: "i69", requestBody)

            val result = api.uploadImage(
                token = "Token $token",
                userId = userId,
                image = image
            )
            Timber.d("Result $result")
            Timber.d("Result ${result.body()}")
            Timber.d("Result ${result.raw()}")

            return Resource.Success(code = com.i69app.data.enums.HttpStatusCode.OK, ResponseBody(data = Id(result.body()!!.id.toString())))

        } catch (e: Exception) {
            e.printStackTrace()
            return Resource.Error(
                code = com.i69app.data.enums.HttpStatusCode.BAD_REQUEST,
                message = e.localizedMessage ?: App.getAppContext().getString(R.string.something_went_wrong)
            )
        }
    }

    suspend fun deleteUserPhotos(token: String, photoId: String): Resource<ResponseBody<Id>> {
        val queryName = "deleteAvatarPhoto"

        val query = StringBuilder()
            .append("mutation {")
            .append("$queryName (")
            .append("id: \"${photoId}\", ")
            .append(") {")
            .append("message")
            .append("}")
            .append("}")
            .toString()

        return graphqlApi.getResponse(query, queryName, token)
    }

    /// Update User Likes
    suspend fun updateUserLikes(userId: String, userLikes: ArrayList<String>, token: String): Resource<ResponseBody<Id>> {
        val likes = userLikes.getStringFromList()

        val queryName = "updateProfile"
        val query = StringBuilder()
            .append("mutation {")
            .append("$queryName (")
            .append("id: \"${userId}\", ")
            .append("likes: $likes ")
            .append(") {")
            .append("id")
            .append("}")
            .append("}")
            .toString()

        return graphqlApi.getResponse(query, queryName, token)
    }

    /// Update One Signal Player Id
    suspend fun updateOneSignalPlayerId(userId: String, onesignalPlayerId: String, token: String): Resource<ResponseBody<Id>> {
        val queryName = "updateProfile"
        val query = StringBuilder()
            .append("mutation {")
            .append("$queryName (")
            .append("id: \"${userId}\", ")
            .append("onesignalPlayerId: \"${onesignalPlayerId}\" ")
            .append(") {")
            .append("id")
            .append("}")
            .append("}")
            .toString()

        return graphqlApi.getResponse(query, queryName, token)
    }
    /// Update Firebase Token
    suspend fun updateFirebasrToken(userId: String, firebasetoken: String, token: String): Resource<ResponseBody<Id>> {
        val queryName = "updateProfile"
        val query = StringBuilder()
            .append("mutation {")
            .append("$queryName (")
            .append("id: \"${userId}\", ")
            .append("fcmRegistrationId: \"${firebasetoken}\" ")
            .append(") {")
            .append("id")
            .append("}")
            .append("}")
            .toString()

        return graphqlApi.getResponse(query, queryName, token)
    }



    /// Update Location
    suspend fun updateLocation(userId: String, token: String, location: Array<Double>): Resource<ResponseBody<Id>> {
        val queryName = "updateProfile"
        val query = StringBuilder()
            .append("mutation {")
            .append("$queryName (")
            .append("id: \"${userId}\", ")
            .append("location: ${location.toList()} ")
            .append(") {")
            .append("id")
            .append("}")
            .append("}")
            .toString()

        return graphqlApi.getResponse(query, queryName, token)
    }

    /// Update Online Status
    suspend fun updateOnlineStatus(userId: String, token: String, isUserOnline: Boolean): Resource<ResponseBody<Id>> {
        val queryName = "updateProfile"
        val query = StringBuilder()
            .append("mutation {")
            .append("$queryName (")
            .append("id: \"${userId}\", ")
            .append("isOnline: $isUserOnline ")
            .append(") {")
            .append("id")
            .append("}")
            .append("}")
            .toString()

        return graphqlApi.getResponse(query, queryName, token)
    }

    /// Report
    suspend fun reportUser(reportRequest: ReportRequest, token: String?): Resource<ResponseBody<Id>> {
        val queryName = "reportUser"
        val query = StringBuilder()
            .append("mutation {")
            .append("$queryName (")
            .append("reportee: \"${reportRequest.reportee}\", ")
            .append("reporter: \"${reportRequest.reporter}\", ")
            .append("timestamp: \"${reportRequest.timestamp}\"")
            .append(") {")
            .append("id")
            .append("}")
            .append("}")
            .toString()

        return graphqlApi.getResponse(query, queryName, token)
    }

    /// Block
    suspend fun blockUser(token: String?, userId: String?, blockedId: String?): Resource<ResponseBody<Id>> {
        val queryName = "blockUser"
        val query = StringBuilder()
            .append("mutation {")
            .append("$queryName (")
            .append("id: \"$userId\", ")
            .append("blockedId: \"$blockedId\"")
            .append(") {")
            .append("id")
            .append("}")
            .append("}")
            .toString()

        return graphqlApi.getResponse(query, queryName, token)
    }

    suspend fun unblockUser(token: String, userId: String, blockedId: String): Resource<ResponseBody<Id>> {
        val queryName = "unblockUser"
        val query = StringBuilder()
            .append("mutation {")
            .append("$queryName (")
            .append("id: \"$userId\", ")
            .append("blockedId: \"$blockedId\"")
            .append(") {")
            .append("id")
            .append("}")
            .append("}")
            .toString()

        return graphqlApi.getResponse(query, queryName, token)
    }


    /// Delete
    suspend fun deleteProfile(userId: String, token: String): Resource<ResponseBody<JsonObject>> {
        val queryName = "deleteProfile"
        val query = StringBuilder()
            .append("mutation {")
            .append("$queryName (")
            .append("id: \"${userId}\"")
            .append(") {")
            .append("result")
            .append("}")
            .append("}")
            .toString()

        return graphqlApi.getResponse(query, queryName, token)
    }


}