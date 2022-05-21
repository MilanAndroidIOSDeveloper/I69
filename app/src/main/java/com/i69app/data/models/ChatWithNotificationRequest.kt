package com.i69app.data.models

import com.quickblox.chat.model.QBChatDialog

data class ChatWithNotificationRequest(
    val currentUserToken: String?,
    val receiverId: String?,
    val chatDialog: QBChatDialog?,
    val msgText: String? = null,
    val msgFilePath: String? = null,
    val notificationTitle: String?,
    val notificationBody: String?,
)