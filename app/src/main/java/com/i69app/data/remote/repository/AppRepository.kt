package com.i69app.data.remote.repository

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.i69app.data.remote.api.GraphqlApi
import com.i69app.data.remote.responses.DefaultPicker
import com.i69app.data.remote.responses.ResponseBody
import com.i69app.utils.Resource
import com.i69app.utils.getResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val api: GraphqlApi
) {
    private var _defaultPickers: DefaultPicker? = null
    private val defaultPickers: MutableLiveData<DefaultPicker> = MutableLiveData()


    fun getDefaultPickers(viewModelScope: CoroutineScope, token: String): MutableLiveData<DefaultPicker> {
        if (_defaultPickers == null) {
            viewModelScope.launch(Dispatchers.IO) { loadDefaultPickers(token) }
        }
        defaultPickers.value = _defaultPickers
        return defaultPickers
    }

    /** New By suresh */
    suspend fun loadPickers(token: String?): Resource<ResponseBody<DefaultPicker>> {
        val queryName = "defaultPicker"
        val query = StringBuilder()
            .append("query {")
            .append("$queryName { ")
            .append("agePicker { id, value, valueFr }, ")
            .append("ethnicityPicker { id, value, valueFr }, ")
            .append("familyPicker { id, value, valueFr }, ")
            .append("heightsPicker { id, value, valueFr }, ")
            .append("politicsPicker { id, value, valueFr }, ")
            .append("religiousPicker { id, value, valueFr }, ")
            .append("tagsPicker { id, value, valueFr }, ")
            .append("zodiacSignPicker { id, value, valueFr } ")
            .append("}")
            .append("}")
            .toString()

        return api.getResponse<DefaultPicker>(query, queryName, token)
    }

    private suspend fun loadDefaultPickers(token: String) {
        val queryName = "defaultPicker"
        val query = StringBuilder()
            .append("query {")
            .append("$queryName { ")
            .append("agePicker { id, value, valueFr }, ")
            .append("ethnicityPicker { id, value, valueFr }, ")
            .append("familyPicker { id, value, valueFr }, ")
            .append("heightsPicker { id, value, valueFr }, ")
            .append("politicsPicker { id, value, valueFr }, ")
            .append("religiousPicker { id, value, valueFr }, ")
            .append("tagsPicker { id, value, valueFr }, ")
            .append("zodiacSignPicker { id, value, valueFr } ")
            .append("}")
            .append("}")
            .toString()

        api.getResponse<DefaultPicker>(query, queryName, token).data?.data?.let {
            _defaultPickers = it
            defaultPickers.postValue(_defaultPickers)
        }
    }

}