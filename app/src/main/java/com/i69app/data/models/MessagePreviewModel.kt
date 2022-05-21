package com.i69app.data.models

import com.quickblox.chat.model.QBChatDialog

data class MessagePreviewModel(
    var user: User?,
    val chatDialog: QBChatDialog,
)