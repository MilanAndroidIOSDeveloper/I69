package com.i69app.ui.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.i69app.ChatRoomSubscription
import com.i69app.GetAllRoomsQuery
import com.i69app.data.models.*
import com.i69app.data.remote.repository.*
import com.i69app.data.remote.requests.ReportRequest
import com.i69app.data.remote.responses.CoinsResponse
import com.i69app.data.remote.responses.DefaultPicker
import com.i69app.data.remote.responses.ResponseBody
import com.i69app.utils.Resource
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.users.model.QBUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val userDetailsRepository: UserDetailsRepository,
    private val userUpdateRepository: UserUpdateRepository,
    private val coinRepository: CoinRepository,
    private val chatRepository: ChatRepository,
    private val chatLoginRepository: ChatLoginRepository,
    private val giftsRepository: GiftsRepository
) : ViewModel() {

    private var selectedMsgPreview: GetAllRoomsQuery.Edge? = null

    private val _shouldUpdateAdapter: MutableSharedFlow<Boolean> = MutableSharedFlow()
    val shouldUpdateAdapter: Flow<Boolean>
        get() = _shouldUpdateAdapter

    /// Mutable Flow
    fun updateAdapterFlow() {
        viewModelScope.launch {
            _shouldUpdateAdapter.emit(true)
        }
    }

    /// Selected Preview Model
    fun getSelectedMessagePreview(): GetAllRoomsQuery.Edge? = selectedMsgPreview

    fun setSelectedMessagePreview(updated: GetAllRoomsQuery.Edge) {
        selectedMsgPreview = updated
    }

    /// Default Pickers
    fun getDefaultPickers(token: String): LiveData<DefaultPicker> =
        appRepository.getDefaultPickers(viewModelScope, token)

    /// Coin Settings
    fun getCoinSettings(token: String): LiveData<ArrayList<CoinSettings>> =
        coinRepository.getCoinSettings(viewModelScope, token)

    fun getRealGifts(token: String): MutableLiveData<ArrayList<ModelGifts.Data.AllRealGift>> =
        giftsRepository.getRealGifts(viewModelScope, token)

    fun getVirtualGifts(token: String): MutableLiveData<ArrayList<ModelGifts.Data.AllRealGift>> =
        giftsRepository.getVirtualGifts(viewModelScope, token)

    fun getAllGifts(token: String): MutableLiveData<ArrayList<ModelGifts.Data.AllRealGift>> =
        giftsRepository.getAllGifts(viewModelScope, token)

    suspend fun purchaseGift(
        token: String?,
        userId: String?,
        giftId: Int?
    ): Resource<ResponseBody<Id>> = giftsRepository.purchaseGift(token = token, userId, giftId)

    /// Current User
    fun getCurrentUser(userId: String, token: String, reload: Boolean): LiveData<User> =
        userDetailsRepository.getCurrentUser(viewModelScope, userId, token = token, reload)


    /// Chat
    fun prepareAndCheckUser(user: User, callback: (String?) -> Unit) =
        chatLoginRepository.prepareAndCheckUser(user, callback)

    fun loginToChat(qbUser: QBUser, callback: (String?) -> Unit) =
        chatLoginRepository.loginToChat(qbUser, callback)

    fun loadAllDialogs(
        userId: String,
        token: String,
        currentUserChatId: Int,
        callback: (String?) -> Unit
    ) =
        chatRepository.loadAllDialogs(
            viewModelScope,
            userId = userId,
            token = token,
            currentUserChatId,
            callback
        )

    fun deleteAllDialogs(qbChatUserId: Int, dialogIds: List<String>, callback: (String?) -> Unit) =
        chatRepository.deleteAllDialogs(qbChatUserId = qbChatUserId, dialogIds, callback)

    fun createNewChatDialog(receiverId: String, callback: (QBChatDialog?, String?) -> Unit) =
        chatRepository.createNewChatDialog(viewModelScope, receiverId = receiverId, callback)

    fun loadChatHistory(
        dialog: QBChatDialog?,
        skipPagination: Int,
        callback: (ArrayList<QBChatMessage>?, String?) -> Unit
    ) =
        chatRepository.loadChatHistory(dialog, skipPagination, callback)

    fun sendMessageToServer(
        chatWithNotificationRequest: ChatWithNotificationRequest,
        callback: (QBChatMessage?, String?) -> Unit
    ) =
        chatRepository.sendMessageToServer(chatWithNotificationRequest, callback)


    /// Coin
    suspend fun deductCoin(
        userId: String,
        token: String,
        deductCoinMethod: com.i69app.data.enums.DeductCoinMethod
    ): Resource<ResponseBody<CoinsResponse>> =
        coinRepository.deductCoin(userId, token = token, deductCoinMethod)


    /// Update
    suspend fun updateProfile(user: User, token: String): Resource<ResponseBody<Id>> =
        userUpdateRepository.updateProfile(user, token = token)

    suspend fun uploadImage(
        userId: String,
        token: String,
        filePath: String
    ): Resource<ResponseBody<Id>> =
        userUpdateRepository.uploadImage(userId = userId, token = token, filePath)

    suspend fun deleteUserPhotos(token: String, photoId: String): Resource<ResponseBody<Id>> =
        userUpdateRepository.deleteUserPhotos(token = token, photoId = photoId)

    suspend fun updateUserLikes(
        userId: String,
        userLikes: ArrayList<String>,
        token: String
    ): Resource<ResponseBody<Id>> =
        userUpdateRepository.updateUserLikes(userId, userLikes, token = token)

    suspend fun updateLocation(
        userId: String,
        location: Array<Double>,
        token: String
    ): Resource<ResponseBody<Id>> =
        userUpdateRepository.updateLocation(userId, token = token, location)


    /// Report
    suspend fun reportUser(
        reportRequest: ReportRequest,
        token: String?
    ): Resource<ResponseBody<Id>> = userUpdateRepository.reportUser(reportRequest, token = token)

    /// Block
    suspend fun blockUser(
        userId: String?,
        blockedId: String?,
        token: String?
    ): Resource<ResponseBody<Id>> = userUpdateRepository.blockUser(token = token, userId, blockedId)

    suspend fun unblockUser(
        userId: String,
        blockedId: String,
        token: String
    ): Resource<ResponseBody<Id>> =
        userUpdateRepository.unblockUser(token = token, userId, blockedId)

    /// Delete
    suspend fun deleteProfile(userId: String, token: String): Resource<ResponseBody<JsonObject>> =
        userUpdateRepository.deleteProfile(userId, token = token)

    /// Log Out
    fun logOut(userId: String, token: String, callback: () -> Unit) =
        userDetailsRepository.logOut(viewModelScope, userId = userId, token = token, callback)


    private val _newMessageFlow: MutableSharedFlow<ChatRoomSubscription.Message?> =
        MutableSharedFlow()
    val newMessageFlow: SharedFlow<ChatRoomSubscription.Message?>
        get() = _newMessageFlow

    suspend fun onNewMessage(newMessage: ChatRoomSubscription.Message?) {
        _newMessageFlow.emit(newMessage)
    }

}