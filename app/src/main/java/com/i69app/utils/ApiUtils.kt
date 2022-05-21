package com.i69app.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject
import com.i69app.R
import com.i69app.data.config.Constants
import com.i69app.data.enums.HttpStatusCode
import com.i69app.data.remote.api.GraphqlApi
import com.i69app.data.remote.requests.ReportRequest
import com.i69app.data.remote.requests.SearchRequest
import com.i69app.data.remote.responses.ResponseBody
import com.i69app.db.DBResource
import com.i69app.singleton.App
import com.i69app.ui.viewModels.UserViewModel
import timber.log.Timber

fun String.getGraphqlApiBody(): String {
    val paramObject = JSONObject()
    paramObject.put("query", this)
    return paramObject.toString()
}

fun getUserDetailsQueryResponse(): String =
    StringBuilder().append("id, username, fullName, email, ")
        .append("photosQuota, ${getPhotosQueryResponse()}, ")
        .append("purchaseCoins, giftCoins,isOnline, gender, age, height, about, avatarIndex,")
        .append("location, familyPlans, religion, politics, ")
        .append("interestedIn, tags, sportsTeams, books, ")
        .append("education, zodiacSign, movies, music, ")
        .append("ethinicity, tvShows, work, ${getAvtarQueryResponse()}, ")
        .append("likes { ${getBlockedUserQueryResponse()} }, ")
        .append("blockedUsers { ${getBlockedUserQueryResponse()} }")
        .toString()

fun getPhotosQueryResponse(): String = "avatarPhotos { id, url }"

fun getBlockedUserQueryResponse(): String = "id, username, fullName, ${getPhotosQueryResponse()}"

fun getAvtarQueryResponse(): String = "avatar { url }"


//// Search
fun getSearchQueryResponse(
    randomUsersQueryName: String,
    popularUsersQueryName: String,
    searchRequest: SearchRequest,
    hasLocation: Boolean
): String =
    StringBuilder()
        .append("query {")
        .append(getSearchOptionQueryResponse(randomUsersQueryName, searchRequest, Constants.SEARCH_RANDOM_LIMIT, hasLocation))
        .append(getSearchOptionQueryResponse(popularUsersQueryName, searchRequest, Constants.SEARCH_POPULAR_LIMIT, hasLocation))
        .append("}")
        .toString()

fun getSearchOptionQueryResponse(queryName: String, searchRequest: SearchRequest, limit: Int, hasLocation: Boolean): String =
    StringBuilder()
        .append("$queryName (")
        .append("interestedIn: ${searchRequest.interestedIn}, ")
        .append("limit: ${limit}, ")
        .append("id: \"${searchRequest.id}\", ")
        .append(if (searchRequest.minHeight == null) "" else "minHeight: ${searchRequest.minHeight}, ")
        .append(if (searchRequest.maxHeight == null) "" else "maxHeight: ${searchRequest.maxHeight}, ")
        .append(if (searchRequest.minAge == null) "" else "minAge: ${searchRequest.minAge}, ")
        .append(if (searchRequest.maxAge == null) "" else "maxAge: ${searchRequest.maxAge}, ")
//        .append(if (!hasLocation) "" else "latitude: ${searchRequest.lat}, ")
//        .append(if (!hasLocation) "" else "longitude: ${searchRequest.long}, ")
//        .append(if (!hasLocation) "" else "maxDistance: ${searchRequest.maxDistance}, ")
        .append(if (searchRequest.familyPlans == null) "" else "familyPlan: ${searchRequest.familyPlans}, ")
        .append(if (searchRequest.politics == null) "" else "politics: ${searchRequest.politics}, ")
        .append(if (searchRequest.religious == null) "" else "religious: ${searchRequest.religious}, ")
        .append(if (searchRequest.zodiacSign == null) "" else "zodiacSign: ${searchRequest.zodiacSign}, ")
        .append(if (searchRequest.searchKey.isNullOrEmpty()) "" else "searchKey: \"${searchRequest.searchKey}\" ")
        .append(") {")
        .append(getUserDetailsQueryResponse())
        .append("}, ")
        .toString()


fun List<String>?.getStringFromList(): String {
    var stringValue = ""
    this?.forEachIndexed { index, string ->
        if (index == 0) stringValue = "["
        stringValue += "\"${string}\","
        if (index == this.size - 1) stringValue = stringValue.substring(0, stringValue.length - 1) + "]"
    }
    return stringValue
}


suspend inline fun <reified T> GraphqlApi.getResponse(query: String?, queryName: String?, token: String?): Resource<ResponseBody<T>> {
    return try {
        val result = this.callApi(token = "Token $token", body = query?.getGraphqlApiBody())
        val jsonObject = Gson().fromJson(result.body(), JsonObject::class.java)
        Timber.i("Token: $token")
        Timber.i("Query: $query")
        Timber.w("Response: ${result.body().toString()}")

        when {
            result.isSuccessful -> {
                val json = Gson().fromJson(jsonObject["data"].asJsonObject[queryName], T::class.java)
                val error: String? = if (jsonObject.has("errors")) {
                    jsonObject["errors"].asJsonArray[0].asJsonObject["message"].asString
                } else {
                    null
                }

                val response = ResponseBody(data = json, errorMessage = error)
                Timber.d("Response: $response")

                if (response.errorMessage.isNullOrEmpty() && response.data != null) {
                    Resource.Success(HttpStatusCode.OK, response)
                } else {
                    Resource.Error(code = HttpStatusCode.BAD_REQUEST, message = response.errorMessage!!)
                }
            }
            else -> Resource.Error(code = HttpStatusCode.INTERNAL_SERVER_ERROR, message = App.getAppContext().getString(R.string.something_went_wrong))
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Resource.Error(code = HttpStatusCode.INTERNAL_SERVER_ERROR, message = "${App.getAppContext().getString(R.string.something_went_wrong)} ${e.localizedMessage}")
    }
}

suspend inline fun <reified T> GraphqlApi.getData(query: String?, queryName: String?, token: String?): DBResource<T> {
    return try {
        val result = this.callApi(token = "Token $token", body = query?.getGraphqlApiBody())
        val jsonObject = Gson().fromJson(result.body(), JsonObject::class.java)
        Timber.i("Query: $query")
        Timber.w("Response: ${result.body().toString()}")

        when {
            result.isSuccessful -> {
                val json = Gson().fromJson(jsonObject["data"].asJsonObject[queryName], T::class.java)
                val error: String? = if (jsonObject.has("errors")) {
                    jsonObject["errors"].asJsonArray[0].asJsonObject["message"].asString
                } else {
                    null
                }

                if (error.isNullOrEmpty() && json != null) {
                    DBResource.success(json, result.code())
                } else {
                    DBResource.error(message = error, result.code())
                }
            }
            else -> DBResource.error(message = App.getAppContext().getString(R.string.something_went_wrong), HttpStatusCode.INTERNAL_SERVER_ERROR.statusCode)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        DBResource.error("${App.getAppContext().getString(R.string.something_went_wrong)} ${e.localizedMessage}", HttpStatusCode.INTERNAL_SERVER_ERROR.statusCode)
    }
}

suspend fun reportUserAccount(token: String?, currentUserId: String?, otherUserId: String?, mViewModel: UserViewModel, callback: (String) -> Unit) {
    val reportRequest = ReportRequest(
        reportee = otherUserId,
        reporter = currentUserId,
        timestamp = getDateWithTimeZone()
    )
    when (val response = mViewModel.reportUser(reportRequest, token = token)) {
        is Resource.Success -> callback(App.getAppContext().getString(R.string.report_accepted))
        is Resource.Error -> {
            Timber.e("${App.getAppContext().getString(R.string.something_went_wrong)} ${response.message}")
            callback("${App.getAppContext().getString(R.string.something_went_wrong)} ${response.message}")
        }
    }
}