package com.i69app.utils.qb

import android.os.Bundle
import com.quickblox.auth.session.QBSettings
import com.quickblox.chat.QBChatService
import com.quickblox.chat.QBRestChatService
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.core.LogLevel
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.request.QBRequestGetBuilder
import com.quickblox.users.model.QBUser
import org.jivesoftware.smack.ConnectionListener
import com.i69app.data.config.Constants.ALLOW_LISTEN_NETWORK
import com.i69app.data.config.Constants.AUTO_JOIN
import com.i69app.data.config.Constants.AUTO_MARK_DELIVERED
import com.i69app.data.config.Constants.CHAT_PORT
import com.i69app.data.config.Constants.KEEP_ALIVE
import com.i69app.data.config.Constants.RECONNECTION_ALLOWED
import com.i69app.data.config.Constants.SOCKET_TIMEOUT
import com.i69app.data.config.Constants.USE_TLS
import com.i69app.utils.qb.callback.QbEntityCallbackWrapper

const val CHAT_HISTORY_ITEMS_PER_PAGE = 500
const val USERS_PER_PAGE = 50
const val CHAT_HISTORY_ITEMS_SORT_FIELD = "date_sent"

object ChatHelper {

    private var qbChatService: QBChatService = QBChatService.getInstance()

    init {
        QBSettings.getInstance().logLevel = LogLevel.DEBUG
        QBChatService.setDebugEnabled(true)
        QBChatService.setConfigurationBuilder(buildChatConfigs())
        QBChatService.setDefaultPacketReplyTimeout(10000)
        qbChatService.setUseStreamManagement(true)
    }

    private fun buildChatConfigs(): QBChatService.ConfigurationBuilder {
        val configurationBuilder = QBChatService.ConfigurationBuilder()
        configurationBuilder.socketTimeout = SOCKET_TIMEOUT
        configurationBuilder.isUseTls = USE_TLS
        configurationBuilder.isKeepAlive = KEEP_ALIVE
        configurationBuilder.isAutojoinEnabled = AUTO_JOIN
        configurationBuilder.setAutoMarkDelivered(AUTO_MARK_DELIVERED)
        configurationBuilder.isReconnectionAllowed = RECONNECTION_ALLOWED
        configurationBuilder.setAllowListenNetwork(ALLOW_LISTEN_NETWORK)
        configurationBuilder.port = CHAT_PORT
        return configurationBuilder
    }

    fun getDialogs(requestBuilder: QBRequestGetBuilder, callback: QBEntityCallback<ArrayList<QBChatDialog>>) {
        QBRestChatService.getChatDialogs(null, requestBuilder).performAsync(
            object : QbEntityCallbackWrapper<ArrayList<QBChatDialog>>(callback) {
                override fun onSuccess(dialogs: ArrayList<QBChatDialog>, bundle: Bundle?) {
                    callback.onSuccess(dialogs, bundle)
                }
            })
    }

    fun getDialogById(dialogId: String, callback: QBEntityCallback<QBChatDialog>) {
        QBRestChatService.getChatDialogById(dialogId).performAsync(callback)
    }

    fun isLogged(): Boolean = QBChatService.getInstance().isLoggedIn

    fun loginToChat(user: QBUser, callback: QBEntityCallback<Void>) {
        if (!qbChatService.isLoggedIn) {
            qbChatService.login(user, callback)
        } else {
            callback.onSuccess(null, null)
        }
    }

    fun addConnectionListener(listener: ConnectionListener?) {
        qbChatService.addConnectionListener(listener)
    }

    fun removeConnectionListener(listener: ConnectionListener?) {
        qbChatService.removeConnectionListener(listener)
    }

    fun logOut() {
        qbChatService.logout()
    }

    fun destroy() {
        qbChatService.destroy()
    }

}