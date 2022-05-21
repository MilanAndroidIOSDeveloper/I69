package com.i69app.ui.viewModels

import android.view.View
import androidx.databinding.ObservableField
import androidx.databinding.ObservableFloat
import androidx.databinding.ObservableInt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.i69app.data.models.IdWithValue
import com.i69app.data.models.User
import com.i69app.data.remote.repository.AppRepository
import com.i69app.data.remote.repository.SearchRepository
import com.i69app.data.remote.requests.SearchRequest
import com.i69app.data.remote.responses.DefaultPicker
import com.i69app.utils.isCurrentLanguageFrench
import javax.inject.Inject
import kotlin.math.roundToInt

private const val MAX_DIST_DEFAULT = 2500f
private const val AGE_RANGE_LEFT_DEFAULT = 18f
private const val AGE_RANGE_RIGHT_DEFAULT = 50f
private const val HEIGHT_RANGE_LEFT_DEFAULT = 55f
private const val HEIGHT_RANGE_RIGHT_DEFAULT = 97f

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val searchRepository: SearchRepository
) : ViewModel() {

    private var _randomUsers: ArrayList<User> = ArrayList()
    private var _popularUsers: ArrayList<User> = ArrayList()
    private var _mostActiveUsers: ArrayList<User> = ArrayList()
    var selectedUser: MutableLiveData<User> = MutableLiveData()

    val maxDistanceValue = ObservableFloat(MAX_DIST_DEFAULT)
    val ageRangeLeft = ObservableFloat(AGE_RANGE_LEFT_DEFAULT)
    val ageRangeRight = ObservableFloat(AGE_RANGE_RIGHT_DEFAULT)
    val heightRangeLeft = ObservableFloat(HEIGHT_RANGE_LEFT_DEFAULT)
    val heightRangeRight = ObservableFloat(HEIGHT_RANGE_RIGHT_DEFAULT)
    val tags: ArrayList<IdWithValue> = ArrayList()
    val familyPlans = SpinnerOptions()
    val politics = SpinnerOptions()
    val religious = SpinnerOptions()
    val zodiac = SpinnerOptions()

    var btnTagsAddListener = View.OnClickListener {}
    var searchBtnClickListener = View.OnClickListener {}

    val clearBtnClickListener = View.OnClickListener {
        clearItems()
    }

    fun updateTags(updated: List<IdWithValue>) {
        tags.clear()
        tags.addAll(updated)
    }

    private fun clearItems() {
        maxDistanceValue.set(MAX_DIST_DEFAULT)
        ageRangeLeft.set(AGE_RANGE_LEFT_DEFAULT)
        ageRangeRight.set(AGE_RANGE_RIGHT_DEFAULT)
        heightRangeLeft.set(HEIGHT_RANGE_LEFT_DEFAULT)
        heightRangeRight.set(HEIGHT_RANGE_RIGHT_DEFAULT)
        tags.clear()
        familyPlans.clear()
        politics.clear()
        religious.clear()
        zodiac.clear()
    }

    /// Default Pickers
    fun getDefaultPickers(token: String): LiveData<DefaultPicker> = appRepository.getDefaultPickers(viewModelScope, token)

    /// Search
    fun getSearchUsers(_searchRequest: SearchRequest, token: String, callback: (String?) -> Unit) {
        val searchRequest = SearchRequest(
            interestedIn = _searchRequest.interestedIn,
            id = _searchRequest.id,
            searchKey = _searchRequest.searchKey,
            minAge = ageRangeLeft.get().roundToInt(),
            maxAge = ageRangeRight.get().roundToInt(),
            minHeight = (heightRangeLeft.get().roundToInt() * 2.54).roundToInt(),
            maxHeight = (heightRangeRight.get().roundToInt() * 2.54).roundToInt(),
            lat = _searchRequest.lat,
            long = _searchRequest.long,
            tags = tags.map { it.id },
            familyPlans = this.familyPlans.getSelectedItem()?.id,
            politics = this.politics.getSelectedItem()?.id,
            religious = this.religious.getSelectedItem()?.id,
            zodiacSign = this.zodiac.getSelectedItem()?.id,
            maxDistance = maxDistanceValue.get().roundToInt()
        )

        searchRepository.getSearchUsers(viewModelScope, token = token, searchRequest) { _randomUsers, _popularUsers, _mostActiveUsers, error ->
            this._randomUsers.clear()
            this._randomUsers.addAll(_randomUsers)

            this._popularUsers.clear()
            this._popularUsers.addAll(_popularUsers)

            this._mostActiveUsers.clear()
            this._mostActiveUsers.addAll(_mostActiveUsers)
            callback.invoke(error)
        }
    }

    fun getRandomUsers(): ArrayList<User> = _randomUsers

    fun getPopularUsers(): ArrayList<User> = _popularUsers

    fun getMostActiveUsers(): ArrayList<User> = _mostActiveUsers


    fun updateDefaultPicker(lookingFor: String, defaultPicker: DefaultPicker) {
        familyPlans.update(lookingFor, defaultPicker.familyPicker)
        politics.update(lookingFor, defaultPicker.politicsPicker)
        religious.update(lookingFor, defaultPicker.religiousPicker)
        zodiac.update(lookingFor, defaultPicker.zodiacSignPicker)
    }

    class SpinnerOptions {
        val prompt = ObservableField<String>()
        val position = ObservableInt(-1)
        val fullItems = ObservableField<List<IdWithValue>>()
        val items = ObservableField<List<String>>()

        private var prmt = ""
        private var fullItms = listOf<IdWithValue>()
        private var itms = listOf<String>()
        private var pos: Int? = -1

        fun update(prmt: String, itms: List<IdWithValue>, pos: Int? = null) {
            val strList = itms.map { if (isCurrentLanguageFrench()) it.valueFr else it.value }
            this.prmt = prmt
            this.fullItms = itms
            this.itms = strList
            this.pos = pos

            this.prompt.set(prmt)
            this.fullItems.set(itms)
            this.items.set(strList)
            if (pos != null) this.position.set(pos)
        }

        fun getSelectedItem(): IdWithValue? {
            return fullItems.get()?.getOrNull(position.get())
        }

        fun clear() = update(prmt, fullItms.toList(), if (pos == null) -1 else pos)
    }

}