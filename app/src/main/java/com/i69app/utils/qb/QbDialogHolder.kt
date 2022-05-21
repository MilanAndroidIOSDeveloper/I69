package com.i69app.utils.qb

import com.quickblox.chat.model.QBChatMessage
import com.i69app.data.models.MessagePreviewModel
import com.i69app.ui.screens.main.MainActivity
import timber.log.Timber
import java.util.*

private const val DEFAULT_MESSAGE_COUNT = 1

object QbDialogHolder {

    private val _dialogsMap: MutableMap<String, MessagePreviewModel> = TreeMap()
    val dialogsMap: Map<String, MessagePreviewModel>
        get() = getSortedMap(_dialogsMap)

    fun getChatDialogById(dialogId: String?): MessagePreviewModel? {
        return _dialogsMap[dialogId]
    }

    fun getChatDialogByUserId(userId: String?): MessagePreviewModel? {
        _dialogsMap.entries.forEach {
            val msgPreviewModel = it.value
            if (msgPreviewModel.user?.id == userId) return msgPreviewModel
        }
        return null
    }

    fun clear() {
        _dialogsMap.clear()
    }

    fun addDialog(dialog: MessagePreviewModel?) {
        dialog?.let {
            _dialogsMap[it.chatDialog.dialogId] = it
        }
    }

    fun addDialogs(dialogs: List<MessagePreviewModel>) {
        dialogs.forEach { addDialog(it) }
    }

    fun hasDialogWithId(dialogId: String): Boolean {
        return _dialogsMap.containsKey(dialogId)
    }

    private fun getSortedMap(unsortedMap: Map<String, MessagePreviewModel>): Map<String, MessagePreviewModel> {
        val sortedMap = TreeMap<String, MessagePreviewModel>(LastMessageDateSentComparator(unsortedMap))
        sortedMap.putAll(unsortedMap)
        return sortedMap
    }

    fun updateDialog(dialogId: String?, qbChatMessage: QBChatMessage, increaseUnreadMsgCount: Boolean) {
        val updatedDialog = getChatDialogById(dialogId)

        updatedDialog?.chatDialog?.let {
            it.lastMessage = qbChatMessage.body
            it.lastMessageDateSent = qbChatMessage.dateSent
            val messageCount = if (increaseUnreadMsgCount) {
                if (it.unreadMessageCount != null) {
                    updatedDialog.chatDialog.unreadMessageCount + 1
                } else {
                    DEFAULT_MESSAGE_COUNT
                }

            } else {
                0
            }
            Timber.tag(MainActivity.CHAT_TAG).w("Method: Increase or not: $increaseUnreadMsgCount   $messageCount")

            it.unreadMessageCount = messageCount
            it.lastMessageUserId = qbChatMessage.senderId

            _dialogsMap[updatedDialog.chatDialog.dialogId] = updatedDialog
        }
    }

    internal class LastMessageDateSentComparator(var map: Map<String, MessagePreviewModel>) : Comparator<String> {
        override fun compare(keyA: String, keyB: String): Int {
            val valueA = map[keyA]?.chatDialog?.lastMessageDateSent ?: return -1
            val valueB = map[keyB]?.chatDialog?.lastMessageDateSent ?: return -1
            return if (valueB < valueA) -1 else 1
        }
    }

}