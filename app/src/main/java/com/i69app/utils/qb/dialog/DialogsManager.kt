package com.i69app.utils.qb.dialog

import android.os.Bundle
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.i69app.data.models.MessagePreviewModel
import com.i69app.data.remote.repository.UserDetailsRepository
import com.i69app.ui.screens.main.MainActivity
import com.i69app.utils.qb.ChatHelper
import com.i69app.utils.qb.QbDialogHolder
import com.i69app.utils.qb.getUserByChatId
import timber.log.Timber
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet

const val PROPERTY_NOTIFICATION_TYPE = "notification_type"

class DialogsManager {

    private val managingDialogsCallbackListener = CopyOnWriteArraySet<ManagingDialogsCallbacks>()

    fun updateAndDecreaseUnSeenMessages(
        dialogId: String?,
        chatMessage: QBChatMessage
    ) {
        Timber.tag(MainActivity.CHAT_TAG).w("Decreasing Unseen Msg Counts")
        QbDialogHolder.updateDialog(dialogId, chatMessage, false)
        notifyListenersDialogUpdated(dialogId)
    }

    ////// Message Receivers
    fun onGlobalMessageReceived(
        dialogId: String,
        chatMessage: QBChatMessage,
        currentUserChatId: Int,
        token: String,
        userDetailsRepository: UserDetailsRepository
    ) {
        if (chatMessage.isMarkable) {
            if (QbDialogHolder.hasDialogWithId(dialogId)) {
                QbDialogHolder.updateDialog(dialogId, chatMessage, true)
                notifyListenersDialogUpdated(dialogId)

            } else {
                ChatHelper.getDialogById(dialogId, object : QBEntityCallback<QBChatDialog> {
                    override fun onSuccess(qbChatDialog: QBChatDialog, bundle: Bundle?) {
                        Timber.tag(MainActivity.CHAT_TAG).d("Loading Dialog Successful")

                        GlobalScope.launch(Dispatchers.IO) {
                            val otherUserId = if (qbChatDialog.occupants[0] == currentUserChatId) qbChatDialog.occupants[1] else qbChatDialog.occupants[0]
                            val chatUser = getUserByChatId(otherUserId) ?: return@launch
                            val appUser = userDetailsRepository.getUserDetails(chatUser.login, token = token) ?: return@launch

                            val messagePreviewModel = MessagePreviewModel(
                                user = appUser,
                                chatDialog = qbChatDialog
                            )
                            QbDialogHolder.addDialog(messagePreviewModel)

                            withContext(Dispatchers.Main) {
                                notifyListenersNewDialogLoaded(qbChatDialog)
                            }
                        }
                    }

                    override fun onError(e: QBResponseException) {
                        Timber.tag(MainActivity.CHAT_TAG).d("Loading Dialog Error: ${e.message}")
                    }
                })
            }
        }
    }

    fun onSystemMessageReceived(systemMessage: QBChatMessage, currentUserChatId: Int, token: String, userDetailsRepository: UserDetailsRepository) {
        Timber.tag(MainActivity.CHAT_TAG).d("System Message Received: ${systemMessage.body} Notification Type: ${systemMessage.getProperty(
            PROPERTY_NOTIFICATION_TYPE
        )}")
        onGlobalMessageReceived(systemMessage.dialogId, systemMessage, currentUserChatId = currentUserChatId, token = token, userDetailsRepository)
    }


    ////// Listeners
    private fun notifyListenersDialogCreated(chatDialog: QBChatDialog) {
        for (listener in getManagingDialogsCallbackListeners()) {
            listener.onDialogCreated(chatDialog)
        }
    }

    private fun notifyListenersDialogUpdated(dialogId: String?) {
        for (listener in getManagingDialogsCallbackListeners()) {
            listener.onDialogUpdated(dialogId)
        }
    }

    private fun notifyListenersNewDialogLoaded(chatDialog: QBChatDialog) {
        for (listener in getManagingDialogsCallbackListeners()) {
            listener.onNewDialogLoaded(chatDialog)
        }
    }

    fun addManagingDialogsCallbackListener(listener: ManagingDialogsCallbacks?) {
        if (listener != null) managingDialogsCallbackListener.add(listener)
    }

    fun removeManagingDialogsCallbackListener(listener: ManagingDialogsCallbacks) {
        managingDialogsCallbackListener.remove(listener)
    }

    fun getManagingDialogsCallbackListeners(): Collection<ManagingDialogsCallbacks> = Collections.unmodifiableCollection(managingDialogsCallbackListener)


    interface ManagingDialogsCallbacks {
        fun onDialogCreated(chatDialog: QBChatDialog)

        fun onDialogUpdated(chatDialog: String?)

        fun onNewDialogLoaded(chatDialog: QBChatDialog)
    }

}