package com.i69app.utils.qb

import com.quickblox.chat.exception.QBChatException
import com.quickblox.chat.model.QBAttachment
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.content.model.QBFile
import com.quickblox.core.exception.QBResponseException
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import timber.log.Timber

fun getUserByChatId(chatId: Int): QBUser? {
    return try {
        QBUsers.getUser(chatId).perform()
    } catch (e: Exception) {
        null
    }
}

fun getUserByLogin(login: String): QBUser? {
    return try {
        QBUsers.getUserByLogin(login).perform()
    } catch (e: Exception) {
        null
    }
}

fun createChatMessage(msgText: String?, qbFile: QBFile?): QBChatMessage {
    val chatMessage = QBChatMessage()
    msgText?.let {
        chatMessage.body = msgText
    }
    chatMessage.setSaveToHistory(true)
    chatMessage.dateSent = System.currentTimeMillis() / 1000
    chatMessage.isMarkable = true

    qbFile?.let {
        var type = "file"
        if (qbFile.contentType.contains("image", ignoreCase = true)) {
            type = QBAttachment.IMAGE_TYPE
        } else if (qbFile.contentType.contains("video", ignoreCase = true)) {
            type = QBAttachment.VIDEO_TYPE
        }

        val attachment = QBAttachment(type)
        attachment.id = qbFile.uid
        attachment.size = qbFile.size.toDouble()
        attachment.name = qbFile.name
        attachment.contentType = qbFile.contentType
        chatMessage.addAttachment(attachment)
    }

    return chatMessage
}

fun QBResponseException.onError(methodName: String?, callback: ((String?) -> Unit)? = null) {
    this.printStackTrace()
    Timber.e("$methodName Error: ${this.message}")
    callback?.let {
        callback(this.message)
    }
}

fun QBChatException.onError(methodName: String?, callback: ((String?) -> Unit)? = null) {
    this.printStackTrace()
    Timber.e("$methodName Error: ${this.message}")
    callback?.let {
        callback(this.message)
    }
}
