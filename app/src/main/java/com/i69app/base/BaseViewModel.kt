package com.i69app.base

import androidx.lifecycle.ViewModel
import com.i69app.singleton.App

open class BaseViewModel : ViewModel() {
    var token = App.userPreferences.userToken
    var userId = App.userPreferences.userId
    var userName = App.userPreferences.userName

}
