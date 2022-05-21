package com.i69app.profile.vm

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.*
import com.google.gson.Gson
import com.i69app.R
import com.i69app.base.BaseViewModel
import com.i69app.data.enums.HttpStatusCode
import com.i69app.data.models.MessagePreviewModel
import com.i69app.data.models.User
import com.i69app.data.remote.repository.*
import com.i69app.data.remote.requests.ReportRequest
import com.i69app.data.remote.responses.DefaultPicker
import com.i69app.db.DBResource
import com.i69app.profile.db.dao.UserDao
import com.i69app.singleton.App
import com.i69app.ui.adapters.UserItemsAdapter
import com.i69app.ui.screens.main.profile.subitems.MomentsFragment
import com.i69app.ui.screens.main.profile.subitems.UserProfileAboutFragment
import com.i69app.ui.screens.main.profile.subitems.UserProfileInterestsFragment
import com.i69app.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject
import com.i69app.ui.screens.main.profile.subitems.FeedsFragment as FeedsFragment

@HiltViewModel
class VMProfile @Inject constructor(
    private val appRepository: AppRepository,
    private val userDetailsRepository: UserDetailsRepository,
    private val context: Context
) : BaseViewModel() {
    val TAG = this.javaClass.canonicalName

    private var _data: MutableLiveData<DataCombined> = MutableLiveData()
    var data: LiveData<DataCombined?> = MutableLiveData()
        get() {
            return _data
        }
    @Inject
    lateinit var userDao: UserDao

    var isMyUser = false
    var isBackEnabled = MutableLiveData(false)
    var isDrawerEnabled = MutableLiveData(false)
    var isEditEnabled = MutableLiveData(false)
    var isReportEnabled = MutableLiveData(false)
    var isMessageEnabled = MutableLiveData(false)
    var isCoinsEnabled = MutableLiveData(false)
    var isEarnCoinsEnabled = MutableLiveData(false)
    var isGiftIconEnabled = MutableLiveData(false)
    var isLikesIconEnabled = MutableLiveData(false)

    var onSendMsg : (() -> Unit) ?= null
    var onDrawer : (() -> Unit) ?= null
    var onEditProfile : (() -> Unit) ?= null
    var onGift : (() -> Unit) ?= null
    var onReport : (() -> Unit) ?= null
    var onCoins : (() -> Unit) ?= null
    var onBackPressed: (() -> Unit) ?= null

    var selectedMsgPreview: MessagePreviewModel? = null

    fun onSendMsgClick(){
        onSendMsg?.invoke()
    }

    fun onBackPressed(){
        onBackPressed?.invoke()
    }

    fun onDrawerClick(){
        onDrawer?.invoke()
    }

    fun onEditProfile(){
        onEditProfile?.invoke()
    }

    fun onGiftClick(){
        onGift?.invoke()
    }

    fun onReport(){
        onReport?.invoke()
    }

    fun onCoinsClick(){
        onCoins?.invoke()
    }

    fun getProfile(userid: String? = "") {
        viewModelScope.launch(Dispatchers.Default) {
            _data.postValue(DataCombined(userDao.getUser(userid), userDao.getPicker()))
            val resOne = async {
                userDetailsRepository.getUserProfile(if (userid?.isEmpty() == true) userId.first() else userid, token.first()) }
            val resTwo = async { appRepository.loadPickers(token.first()) }
            if (resOne.await().status == Status.SUCCESS && resTwo.await().code == HttpStatusCode.OK) {
                resOne.await().data.let {
                    it?.id = userid ?: ""
                    resTwo.await().data?.data.let { it2 ->
                        userDao.insertPicker(it2)
                        it?.ageValue = "${it2?.agePicker?.getSelectedValueFromDefaultPicker(it?.age)} ${context.getString(R.string.years)}"
                        it?.heightValue = "${it2?.heightsPicker?.getSelectedValueFromDefaultPicker(it?.height)} ${context.getString(R.string.cm)}"
                        userDao.insertUser(it)
                        _data.postValue(DataCombined(it, it2))
                    }
                }
            } else {
                _data.postValue(null)
            }
        }
        setMyUI()
    }

    fun reportUser(reportee: String?) = liveData{
        when (val response = userDetailsRepository?.reportUser(ReportRequest(reportee, userId.first(), getDateWithTimeZone()), token.first())) {
            is Resource.Success -> emit(App.getAppContext().getString(R.string.report_accepted))
            is Resource.Error -> {
                Timber.e("${App.getAppContext().getString(R.string.something_went_wrong)} ${response.message}")
                emit("${App.getAppContext().getString(R.string.something_went_wrong)} ${response.message}")
            }
        }
    }

    private fun setMyUI(){
        isBackEnabled.value = !isMyUser
        isDrawerEnabled.value = isMyUser
        isEditEnabled.value = isMyUser
        isReportEnabled.value = !isMyUser
        isMessageEnabled.value = !isMyUser
        isCoinsEnabled.value = isMyUser
        isEarnCoinsEnabled.value = isMyUser
        isGiftIconEnabled.value = !isMyUser
        isLikesIconEnabled.value = !isMyUser
    }

    fun setupViewPager(childFragmentManager: FragmentManager, user: User?, defaultPicker: DefaultPicker?): UserItemsAdapter {
        val adapter = UserItemsAdapter(childFragmentManager)
        val about = UserProfileAboutFragment()
        val interests = UserProfileInterestsFragment()
        val feed = FeedsFragment()
        val useriddata = Bundle()
        val moment = MomentsFragment()
        useriddata.putString(EXTRA_USER_MODEL, Gson().toJson(user))
        moment.arguments = useriddata
        val userDataArgs = Bundle()
        if (user != null) userDataArgs.putString(EXTRA_USER_MODEL, Gson().toJson(user))
        userDataArgs.putString("default_picker", Gson().toJson(defaultPicker))
        about.arguments = userDataArgs
        interests.arguments = userDataArgs
        adapter.addFragItem(about,
            context.resources?.getString(R.string.about).toString()
        )
        adapter.addFragItem(interests,
            context.resources?.getString(R.string.interests).toString()
        )
        adapter.addFragItem(feed,
            context.resources?.getString(R.string.feed).toString()
        )
        adapter.addFragItem(moment,
            context.resources?.getString(R.string.moment).toString()
        )
        return adapter
    }

    data class DataCombined(var user: User?, var defaultPicker: DefaultPicker?)
}