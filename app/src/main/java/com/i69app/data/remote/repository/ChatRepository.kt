package com.i69app.data.remote.repository

import android.os.Bundle
import com.google.gson.Gson
import com.i69app.R
import com.i69app.data.models.ChatWithNotificationRequest
import com.i69app.data.models.MessagePreviewModel
import com.i69app.data.remote.api.GraphqlApi
import com.i69app.chat.model.ModelQBChatDialogs
import com.i69app.chat.dao.ChatDialogsDao
import com.i69app.data.models.User
import com.i69app.profile.db.dao.UserDao
import com.i69app.singleton.App
import com.i69app.ui.screens.main.MainActivity
import com.i69app.utils.getResponse
import com.i69app.utils.qb.*
import com.quickblox.chat.QBRestChatService
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.chat.request.QBMessageGetBuilder
import com.quickblox.chat.utils.DialogUtils
import com.quickblox.content.QBContent
import com.quickblox.content.model.QBFile
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.core.request.QBRequestGetBuilder
import com.quickblox.users.QBUsers
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList

@Singleton
class ChatRepository @Inject constructor(
    private val api: GraphqlApi,
    private val userDetailsRepository: UserDetailsRepository,
    private val chatDialogsDao: ChatDialogsDao,
    private val userDao: UserDao
) {
    /**
     * Sender - Current User
     * Receiver - Other User
     */

    fun loadAllDialogs(
        viewModelScope: CoroutineScope,
        userId: String,
        token: String,
        currentUserChatId: Int,
        callback: (String?) -> Unit
    ) {
        val requestBuilder = QBRequestGetBuilder()
        requestBuilder.limit = USERS_PER_PAGE
//        requestBuilder.sortDesc("last_message_date_sent")
;
        ChatHelper.getDialogs(requestBuilder, object : QBEntityCallback<ArrayList<QBChatDialog>> {
            override fun onSuccess(result: ArrayList<QBChatDialog>, params: Bundle?) {
                Timber.tag(MainActivity.CHAT_TAG).i("Chat Dialogs from REST: ${result.size}")
                QbDialogHolder.clear()

                viewModelScope.launch(Dispatchers.IO) {
                    val currentUser = userDetailsRepository.getCurrentUser(
                        viewModelScope,
                        userId,
                        token,
                        true
                    ).value!!
//                    val likes = currentUser.likes.map { it.id }
                    val blockedUsers = currentUser.blockedUsers.map { it.id }

                    result.forEach { chatDialog ->
                        val otherUserId =
                            if (chatDialog.occupants[0] == currentUserChatId) chatDialog.occupants[1] else chatDialog.occupants[0]
                        val chatUser = getUserByChatId(otherUserId) ?: return@forEach
//                        if (!likes.contains(chatUser.login)) return@forEach
                        if (blockedUsers.contains(chatUser.login)) return@forEach

                        val appUser =
                            userDetailsRepository.getUserDetails(chatUser.login, token = token)
                                ?: return@forEach
                        val messagePreviewModel = MessagePreviewModel(
                            user = appUser,
                            chatDialog = chatDialog
                        )
                        QbDialogHolder.addDialog(messagePreviewModel)
                        storeDialogs(chatDialog, appUser)
                    }
                    withContext(Dispatchers.Main) {
                        callback(null)
                    }
                }
            }

            override fun onError(e: QBResponseException) {
                e.onError("createNewChatDialog") {
                    callback(it)
                }
            }
        })
        viewModelScope.launch(Dispatchers.IO) {
            chatDialogsDao?.getChatDialogs().forEach {
                var dialog = Gson().toJson(it)
                var modelQBChatDialog = Gson().fromJson(dialog, QBChatDialog::class.java)
                val otherUserId =
                    if (modelQBChatDialog.occupants[0] == currentUserChatId) modelQBChatDialog.occupants[1] else modelQBChatDialog.occupants[0]
                var user = User(id = it.localUserId?: "", fullName = it.userFullName ?: "", avatarPhotos = it.localUserImages)
                QbDialogHolder.addDialog(MessagePreviewModel(user, modelQBChatDialog))
            }
        }
    }

    fun createNewChatDialog(
        viewModelScope: CoroutineScope,
        receiverId: String,
        callback: (QBChatDialog?, String?) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val chatUser = getUserByLogin(receiverId)
            if (chatUser == null) {
                callback(null, App.getAppContext().getString(R.string.user_not_available))
                return@launch
            }

            val dialog = DialogUtils.buildPrivateDialog(chatUser.id)
            withContext(Dispatchers.Main) {
                QBRestChatService.createChatDialog(dialog)
                    .performAsync(object : QBEntityCallback<QBChatDialog> {
                        override fun onSuccess(result: QBChatDialog?, params: Bundle?) {
                            callback(result, null)
                        }

                        override fun onError(e: QBResponseException) {
                            e.onError("createNewChatDialog") {
                                callback(null, it)
                            }
                        }
                    })
            }
        }
    }

    fun deleteAllDialogs(qbChatUserId: Int, dialogIds: List<String>, callback: (String?) -> Unit) {
        val qbDialogsIDs = StringifyArrayList<String>()
        qbDialogsIDs.addAll(dialogIds)

        QBRestChatService.deleteDialogs(qbDialogsIDs, true, null)
            .performAsync(object : QBEntityCallback<ArrayList<String>> {
                override fun onSuccess(strings: ArrayList<String>?, bundle: Bundle?) {
                    Timber.tag(MainActivity.CHAT_TAG).i("Deleted all dialogs!")
                    deleteUser(qbChatUserId, callback)
                }

                override fun onError(e: QBResponseException?) {
                    callback(e?.message)
                }
            })
    }

    fun deleteUser(qbChatUserId: Int, callback: (String?) -> Unit) {
        QBUsers.deleteUser(qbChatUserId).performAsync(object : QBEntityCallback<Void> {
            override fun onSuccess(strings: Void?, bundle: Bundle?) {
                Timber.tag(MainActivity.CHAT_TAG).i("Deleted User!")
                callback(null)
            }

            override fun onError(e: QBResponseException?) {
                Timber.tag(MainActivity.CHAT_TAG).e(e)
                callback(e?.message)
            }
        })
    }

    fun loadChatHistory(
        dialog: QBChatDialog?,
        skipPagination: Int,
        callback: (ArrayList<QBChatMessage>?, String?) -> Unit
    ) {
        val messageGetBuilder = QBMessageGetBuilder()
//        messageGetBuilder.skip = skipPagination
        messageGetBuilder.limit = CHAT_HISTORY_ITEMS_PER_PAGE
        messageGetBuilder.sortAsc(CHAT_HISTORY_ITEMS_SORT_FIELD)
        messageGetBuilder.markAsRead(true)

        QBRestChatService
            .getDialogMessages(dialog, messageGetBuilder)
            .performAsync(object : QBEntityCallback<ArrayList<QBChatMessage>> {
                override fun onSuccess(messages: ArrayList<QBChatMessage>, bundle: Bundle?) {
                    Timber.tag("TP_LINK").w("Msgs:  $messages")
                    addMessageToLocal(messages)
                    callback(messages, null)
                }

                override fun onError(e: QBResponseException) {
                    e.printStackTrace()
                    Timber.e("Error: ${e.message}")
                    callback(null, e.message)
                }
            })
    }

    private fun addMessageToLocal(messages: java.util.ArrayList<QBChatMessage>) {
        var messagesJson = Gson().toJson(messages)
        Timber.d(messagesJson)
    }

    fun sendMessageToServer(
        chatWithNotificationRequest: ChatWithNotificationRequest,
        callback: (QBChatMessage?, String?) -> Unit
    ) {
        if (chatWithNotificationRequest.msgText == null) {
            sendImageMessage(chatWithNotificationRequest, callback = callback)
        } else {
            sendTextMessage(chatWithNotificationRequest, callback = callback)
        }
    }

    private fun sendTextMessage(
        chatWithNotificationRequest: ChatWithNotificationRequest,
        callback: (QBChatMessage?, String?) -> Unit
    ) {
        val chatMessage =
            createChatMessage(msgText = chatWithNotificationRequest.msgText, qbFile = null)

        sendMessage(
            chatWithNotificationRequest = chatWithNotificationRequest,
            chatMessage = chatMessage,
            callback
        )
    }

    private fun sendImageMessage(
        chatWithNotificationRequest: ChatWithNotificationRequest,
        callback: (QBChatMessage?, String?) -> Unit
    ) {
        val file = File(chatWithNotificationRequest.msgFilePath!!)

        QBContent
            .uploadFileTask(file, false, arrayOf("image").toString()) {}
            .performAsync(object : QBEntityCallback<QBFile> {
                override fun onSuccess(qbFile: QBFile?, bundle: Bundle?) {
                    val chatMessage = createChatMessage(msgText = null, qbFile = qbFile)

                    sendMessage(
                        chatWithNotificationRequest = chatWithNotificationRequest,
                        chatMessage = chatMessage,
                        callback
                    )
                }

                override fun onError(e: QBResponseException) {
                    e.onError("sendImageMessage") {
                        callback(null, it)
                    }
                }
            })
    }

    private fun sendMessage(
        chatWithNotificationRequest: ChatWithNotificationRequest,
        chatMessage: QBChatMessage,
        callback: (QBChatMessage?, String?) -> Unit
    ) {
        chatWithNotificationRequest.chatDialog?.sendMessage(
            chatMessage,
            object : QBEntityCallback<Void> {
                override fun onSuccess(aVoid: Void?, bundle: Bundle?) {
                    callback(chatMessage, null)

                    GlobalScope.launch {
                        sendNotification(
                            chatWithNotificationRequest = chatWithNotificationRequest,
                        )
                    }
                }

                override fun onError(e: QBResponseException) {
                    e.onError("sendMessage") {
                        callback(null, it)
                    }
                }
            })
    }

    private suspend fun sendNotification(
        chatWithNotificationRequest: ChatWithNotificationRequest
    ): Boolean? {
        val queryName = "sendNotification"
        val query = StringBuilder()
            .append("mutation {")
            .append("$queryName (")
            .append("userId: \"${chatWithNotificationRequest.receiverId}\", ")
            .append("icon: \"logo\", ")
            .append("title: \"${chatWithNotificationRequest.notificationTitle}\", ")
            .append("body: \"${chatWithNotificationRequest.notificationBody}\", ")
            .append("priority: 10, ")
            .append("androidChannelId: \"${MainActivity.ARGS_CHANNEL_ID}\", ")
            .append("appUrl: \"schema://${MainActivity.ARGS_MESSAGE_SCREEN}\", ")
            .append("data: \"{\\\"sender_id\\\": \\\"${chatWithNotificationRequest.chatDialog?.dialogId}\\\"}\" ")
            .append(") {")
            .append("sent")
            .append("}")
            .append("}")
            .toString()

        return api.getResponse<Boolean>(
            query,
            queryName,
            token = chatWithNotificationRequest.currentUserToken
        ).data?.data
    }

    fun storeDialogs(dialog: QBChatDialog?, user: User?){
        var json = Gson().toJson(dialog)
        var model = Gson().fromJson(json, ModelQBChatDialogs::class.java)
        model.localUserId = user?.id
        model.localUserImages = user?.avatarPhotos
        model.userFullName = user?.fullName
        chatDialogsDao?.insertChatDialog(model)
    }
}