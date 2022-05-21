package com.i69app.data.remote.repository

import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.i69app.data.enums.DeductCoinMethod
import com.i69app.data.models.CoinSettings
import com.i69app.data.remote.api.GraphqlApi
import com.i69app.data.remote.requests.PurchaseRequest
import com.i69app.data.remote.responses.CoinsResponse
import com.i69app.data.remote.responses.ResponseBody
import com.i69app.utils.Resource
import com.i69app.utils.getGraphqlApiBody
import com.i69app.utils.getResponse
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoinRepository @Inject constructor(
    private val api: GraphqlApi
) {
    private var _coinSettings: ArrayList<CoinSettings> = ArrayList()
    private val coinSettings: MutableLiveData<ArrayList<CoinSettings>> = MutableLiveData()


    suspend fun purchaseCoin(purchaseRequest: PurchaseRequest, token: String): Resource<ResponseBody<CoinsResponse>> {
        val queryName = "purchaseCoin"
        val query = StringBuilder()
            .append("mutation {")
            .append("$queryName (")
            .append("id: \"${purchaseRequest.id}\", ")
            .append("coins: ${purchaseRequest.coins}, ")
            .append("money: ${purchaseRequest.money}, ")
            .append("method: \"${purchaseRequest.method}\"")
            .append(") {")
            .append("id, coins, success ")
            .append("}")
            .append("}")
            .toString()

        return api.getResponse(query, queryName, token)
    }

    suspend fun deductCoin(userId: String, token: String, deductCoinMethod: com.i69app.data.enums.DeductCoinMethod): Resource<ResponseBody<CoinsResponse>> {
        val queryName = "deductCoin"
        val query = StringBuilder()
            .append("mutation {")
            .append("$queryName (")
            .append("id: \"${userId}\", ")
            .append("method: \"${deductCoinMethod.name}\"")
            .append(") {")
            .append("id, coins, success ")
            .append("}")
            .append("}")
            .toString()

        return api.getResponse(query, queryName, token)
    }

    fun getCoinSettings(viewModelScope: CoroutineScope, token: String): MutableLiveData<ArrayList<CoinSettings>> {
        if (_coinSettings.isEmpty()) {
            viewModelScope.launch(Dispatchers.IO) { loadCoinSettings(token) }
        }
        coinSettings.value = _coinSettings
        return coinSettings
    }

    private suspend fun loadCoinSettings(token: String) {
        val queryName = "coinSettings"
        val query = StringBuilder()
            .append("query {")
            .append("$queryName { ")
            .append("method, coinsNeeded")
            .append("}")
            .append("}")
            .toString()

        try {
            val result = api.callApi(token = "Token $token", body = query.getGraphqlApiBody())
            val jsonObject = Gson().fromJson(result.body(), JsonObject::class.java)
            Timber.i("Query: $query")
            Timber.w("Result: ${result.body()}")

            when {
                result.isSuccessful -> {
                    val dataJsonObject = jsonObject["data"].asJsonObject
                    val coinSettingsJsonArray = dataJsonObject[queryName].asJsonArray

                    coinSettingsJsonArray.forEach { jsonElement ->
                        val json = Gson().fromJson(jsonElement, CoinSettings::class.java)
                        _coinSettings.add(json)
                    }

                    val error: String? = if (jsonObject.has("errors")) {
                        jsonObject["errors"].asJsonArray[0].asJsonObject["message"].asString
                    } else null


                    if (error.isNullOrEmpty()) coinSettings.postValue(_coinSettings)

                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}