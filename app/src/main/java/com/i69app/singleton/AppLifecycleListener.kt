package com.i69app.singleton

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.i69app.data.preferences.UserPreferences
import com.i69app.data.remote.repository.UserUpdateRepository

class AppLifecycleListener(private val userPreferences: UserPreferences, private val userUpdateRepository: UserUpdateRepository) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    internal fun onStart() {
        GlobalScope.launch {
            val userId = userPreferences.userId.first()
            val userToken = userPreferences.userToken.first()

            if (userId != null && userToken != null) {
                userUpdateRepository.updateOnlineStatus(userId, token = userToken, true)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    internal fun onStop() {
        GlobalScope.launch {
            val userId = userPreferences.userId.first()
            val userToken = userPreferences.userToken.first()

            if (userId != null && userToken != null) {
                userUpdateRepository.updateOnlineStatus(userId, token = userToken, false)
            }
        }
    }

}