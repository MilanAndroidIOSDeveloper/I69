package com.i69app.data.remote.repository

import android.os.Bundle
import com.quickblox.auth.session.QBSessionManager
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.users.QBUsers
import com.quickblox.users.model.QBUser
import com.i69app.data.enums.HttpStatusCode
import com.i69app.data.models.User
import com.i69app.utils.qb.ChatHelper
import com.i69app.utils.qb.onError
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatLoginRepository @Inject constructor(
) {

    private fun isLoggedIn() = QBSessionManager.getInstance().sessionParameters != null

    fun prepareAndCheckUser(user: User, callback: (String?) -> Unit) {
        if (isLoggedIn()) {
            callback(QBSessionManager.getInstance().sessionParameters.userId.toString())

        } else {
            val qbUser = QBUser()
            qbUser.login = user.id
            qbUser.fullName = user.fullName
            qbUser.password = user.id
            signIn(qbUser, callback)
        }
    }

    private fun signIn(qbUser: QBUser, callback: (String?) -> Unit) {
        Timber.d("$qbUser")
        QBUsers.signIn(qbUser).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(userFromRest: QBUser, args: Bundle?) {
                Timber.d("User: $userFromRest")
                if (userFromRest.fullName != null && userFromRest.fullName == qbUser.fullName) {
                    loginToChat(qbUser, callback, userFromRest.id.toString())

                } else {
                    // Need to set password NULL, because server will update user only with NULL password
                    qbUser.password = null
                    updateUser(qbUser, callback, userFromRest.id.toString())
                }
            }

            override fun onError(error: QBResponseException) {
                if (error.httpStatusCode == com.i69app.data.enums.HttpStatusCode.UNAUTHORIZED.statusCode) {
                    signUp(qbUser, callback)
                    return
                }
                error.onError(methodName = "signIn", callback)
            }
        })
    }

    fun loginToChat(qbUser: QBUser, callback: (String?) -> Unit, chatUserId: String? = null) {
        // Need to set password, because the server will not register to chat without password
        qbUser.password = qbUser.login

        ChatHelper.loginToChat(qbUser, object : QBEntityCallback<Void> {
            override fun onSuccess(o: Void?, bundle: Bundle?) {
                Timber.d("Connected To Chat. $qbUser")
                callback(chatUserId)
            }

            override fun onError(error: QBResponseException) {
                error.onError(methodName = "loginToChat", callback)
            }
        })
    }

    private fun updateUser(qbUser: QBUser, callback: (String?) -> Unit, chatUserId: String? = null) {
        QBUsers.updateUser(qbUser).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(userFromRest: QBUser, args: Bundle?) {
                Timber.d("User: $userFromRest")
                loginToChat(qbUser, callback, chatUserId)
            }

            override fun onError(error: QBResponseException) {
                error.onError(methodName = "updateUser", callback)
            }
        })
    }

    private fun signUp(qbUser: QBUser, callback: (String?) -> Unit) {
        QBUsers.signUp(qbUser).performAsync(object : QBEntityCallback<QBUser> {
            override fun onSuccess(userFromRest: QBUser, args: Bundle?) {
                Timber.d("User: $userFromRest")
                signIn(qbUser, callback)
            }

            override fun onError(error: QBResponseException) {
                error.onError(methodName = "signUp", callback)
            }
        })
    }

}