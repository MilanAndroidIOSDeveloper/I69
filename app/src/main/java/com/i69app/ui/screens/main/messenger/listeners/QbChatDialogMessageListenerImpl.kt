package com.i69app.ui.screens.main.messenger.listeners

import com.quickblox.chat.exception.QBChatException
import com.quickblox.chat.listeners.QBChatDialogMessageListener
import com.quickblox.chat.model.QBChatMessage

open class QbChatDialogMessageListenerImpl : QBChatDialogMessageListener {

    override fun processMessage(dialogId: String, qbChatMessage: QBChatMessage, senderId: Int?) {

    }

    override fun processError(dialogId: String, e: QBChatException, qbChatMessage: QBChatMessage?, senderId: Int?) {

    }

}