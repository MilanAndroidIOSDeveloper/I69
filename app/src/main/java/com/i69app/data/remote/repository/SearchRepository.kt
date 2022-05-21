package com.i69app.data.remote.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.i69app.data.models.User
import com.i69app.data.remote.api.GraphqlApi
import com.i69app.data.remote.requests.SearchRequest
import com.i69app.utils.getGraphqlApiBody
import com.i69app.utils.getSearchQueryResponse
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val api: GraphqlApi
) {

    fun getSearchUsers(viewModelScope: CoroutineScope, token: String, searchRequest: SearchRequest, callback: (List<User>, List<User>, List<User>, String?) -> Unit) {
        val randomUsersQueryName = "randomUsers"
        val popularUsersQueryName = "popularUsers"
        val hasLocation = searchRequest.lat != null && searchRequest.long != null && searchRequest.maxDistance != null

        val searchQuery = getSearchQueryResponse(randomUsersQueryName, popularUsersQueryName, searchRequest, hasLocation)

        viewModelScope.launch(Dispatchers.IO) {
            getUsers(token, searchQuery, randomUsersQueryName, popularUsersQueryName) { _randomUsers, _popularUsers, _mostActiveUsers, error ->
                viewModelScope.launch(Dispatchers.Main) {
                    callback(_randomUsers, _popularUsers, _mostActiveUsers, error)
                }
            }
        }
    }

    private suspend fun getUsers(
        token: String,
        query: String,
        randomUsersQueryName: String,
        popularUsersQueryName: String,
        callback: (List<User>, List<User>, List<User>, String?) -> Unit
    ) {
        try {
            val result = api.callApi(token = "Token $token", body = query.getGraphqlApiBody())
            val jsonObject = Gson().fromJson(result.body(), JsonObject::class.java)
            Timber.i("Query: $query")
            Timber.w("Result: ${result.body()}")

            when {
                result.isSuccessful -> {
                    val randomUsersList = ArrayList<User>()
                    val popularUsersList = ArrayList<User>()

                    val dataJsonObject = jsonObject["data"].asJsonObject
                    val randomUsersJsonArray = dataJsonObject[randomUsersQueryName].asJsonArray
                    val popularUsersJsonArray = dataJsonObject[popularUsersQueryName].asJsonArray

                    randomUsersJsonArray.forEach { jsonElement ->
                        val json = Gson().fromJson(jsonElement, User::class.java)
                        randomUsersList.add(json)
                    }

                    popularUsersJsonArray.forEach { jsonElement ->
                        val json = Gson().fromJson(jsonElement, User::class.java)
                        popularUsersList.add(json)
                    }

                    val mostActiveUsersList = randomUsersList.filter { it.isOnline }

                    val error: String? = if (jsonObject.has("errors")) {
                        jsonObject["errors"].asJsonArray[0].asJsonObject["message"].asString
                    } else {
                        null
                    }

                    if (error.isNullOrEmpty())
                        callback.invoke(randomUsersList, popularUsersList, mostActiveUsersList, null)
                    else
                        callback.invoke(emptyList(), emptyList(), emptyList(), error)

                }
                else -> callback.invoke(emptyList(), emptyList(), emptyList(), "Something went wrong! Please try again later!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            callback.invoke(emptyList(), emptyList(), emptyList(), "Something went wrong! Please try again later!")
        }
    }

}