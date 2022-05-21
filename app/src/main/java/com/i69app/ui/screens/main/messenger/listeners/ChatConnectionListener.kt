package com.i69app.ui.screens.main.messenger.listeners

import org.jivesoftware.smack.ConnectionListener
import org.jivesoftware.smack.XMPPConnection
import timber.log.Timber
import java.lang.Exception

open class ChatConnectionListener : ConnectionListener {

    override fun connected(connection: XMPPConnection?) {
        Timber.i("Connected: $connection")
    }

    override fun authenticated(connection: XMPPConnection?, authenticated: Boolean) {
        Timber.i("Authenticated: $authenticated  Connection: $connection")
    }

    override fun connectionClosed() {
        Timber.e("Connection Closed")
    }

    override fun connectionClosedOnError(e: Exception) {
        e.printStackTrace()
        Timber.e("Connection Closed On Error. ${e.message}")
    }

    override fun reconnectingIn(seconds: Int) {
        if (seconds % 5 == 0 && seconds != 0) Timber.i("Reconnecting in $seconds")
    }

    override fun reconnectionSuccessful() {
        Timber.i("Reconnection Successful")
    }

    override fun reconnectionFailed(e: Exception) {
        e.printStackTrace()
        Timber.e("Reconnection Failed ${e.message}")
    }


}