package com.i69app.data.remote.repository

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.users.QBUsers
import com.i69app.R
import com.i69app.data.models.Id
import com.i69app.data.models.User
import com.i69app.data.preferences.UserPreferences
import com.i69app.data.remote.api.GraphqlApi
import com.i69app.data.remote.requests.ReportRequest
import com.i69app.data.remote.responses.ResponseBody
import com.i69app.db.AppExecutors
import com.i69app.db.DBResource
import com.i69app.db.NetworkAndDBBoundResource
import com.i69app.profile.db.dao.UserDao
import com.i69app.singleton.App
import com.i69app.utils.*
import com.i69app.utils.qb.ChatHelper
import com.i69app.utils.qb.QbDialogHolder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDetailsRepository @Inject constructor(
    private val api: GraphqlApi,
    private val userUpdateRepository: UserUpdateRepository,
    private val userDao: UserDao,
    private val context: Context)
{
    private var _currentUser: User? = null
    private var currentUser: MutableLiveData<User> = MutableLiveData()
    fun getCurrentUser(viewModelScope: CoroutineScope, userId: String, token: String, reload: Boolean): MutableLiveData<User> {
        if (_currentUser == null || reload) viewModelScope.launch { loadCurrentUser(userId, token) }
        currentUser.postValue(_currentUser)
        return currentUser
    }

    private suspend fun loadCurrentUser(userId: String, token: String) {
        getUserDetails(userId, token = token)?.let {
            _currentUser = it
            currentUser.postValue(_currentUser)
        }
    }

    // User
    suspend fun getUserDetails(userId: String?, token: String?): User? {
        val queryName = "user"
        val query = StringBuilder()
            .append("query {")
            .append("$queryName (")
            .append("id: \"${userId}\" ")
            .append(") {")
            .append(getUserDetailsQueryResponse())
            .append("}")
            .append("}")
            .toString()
        return api.getResponse<User>(query, queryName, token).data?.data
    }

    /*fun getProfile(userId: String?, token: String?): LiveData<DBResource<User?>> {
        return object :NetworkAndDBBoundResource<User, User>(appExecutors = appExecutors){
            override fun saveCallResult(item: User) {
                Log.d(TAG, "saveCallResult: ${Thread.currentThread()}")
                if(item!=null){
                    userDao?.insertUser(item)
                }
            }

            override fun createCall(): LiveData<DBResource<User>> {
                Log.d(TAG, "createCall: ${Thread.currentThread()}")
                var _user: MutableLiveData<DBResource<User>> = MutableLiveData()
                *//*viewModelScope.launch(Dispatchers.IO) {
                    _user.value = getUserProfile(userId, token)
                }*//*
                return _user
            }

            override fun shouldFetch(data: User?): Boolean {
                Log.d(TAG, "shouldFetch: ${Thread.currentThread()}")
                return (ConnectivityUtil.isConnected(context))
            }

            override fun loadFromDb(): LiveData<User> {
                Log.d(TAG, "loadFromDb: ${Thread.currentThread()}")
                *//*var data : MutableLiveData<User> = MutableLiveData()
                viewModelScope.launch(Dispatchers.IO) {
                    data.postValue(userDao?.getUser(userId))
                }*//*
                return userDao.getUser(userId)
            }
        }.asLiveData()
    }*/

    suspend fun getUserProfile(userId: String?, token: String?): DBResource<User> {
        val queryName = "user"
        val query = StringBuilder()
            .append("query {")
            .append("$queryName (")
            .append("id: \"${userId}\" ")
            .append(") {")
            .append(getUserDetailsQueryResponse())
            .append("}")
            .append("}")
            .toString()
        return api.getData<User>(query, queryName, token)
    }

    suspend fun reportUser(reportRequest: ReportRequest, token: String?): Resource<ResponseBody<Id>> {
        val queryName = "reportUser"
        val query = StringBuilder()
            .append("mutation {")
            .append("$queryName (")
            .append("reportee: \"${reportRequest.reportee}\", ")
            .append("reporter: \"${reportRequest.reporter}\", ")
            .append("timestamp: \"${reportRequest.timestamp}\"")
            .append(") {")
            .append("id")
            .append("}")
            .append("}")
            .toString()

        return api.getResponse(query, queryName, token)
    }


    /// Log Out
    fun logOut(viewModelScope: CoroutineScope, userId: String, token: String, callback: () -> Unit) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(App.getAppContext().getString(R.string.server_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(App.getAppContext(), gso)

        viewModelScope.launch {

            userUpdateRepository.updateFirebasrToken(userId = userId, firebasetoken = "log_out", token = token)

            userUpdateRepository.updateOneSignalPlayerId(userId = userId, onesignalPlayerId = "log_out", token = token)



        }

        QBUsers.signOut().performAsync(object : QBEntityCallback<Void> {
            override fun onSuccess(p0: Void?, p1: Bundle?) {}

            override fun onError(p0: QBResponseException?) {}
        })
        _currentUser = null
        currentUser.postValue(_currentUser)

        clearChatSystem()

        googleSignInClient.signOut()
            .addOnSuccessListener {
                Timber.d("Google Sign Out Success")
                logOutFromFacebook(callback)
            }
            .addOnFailureListener {
                Timber.e("Google Sign Out Failure")
                logOutFromFacebook(callback)
            }
    }

    private fun clearChatSystem() {
        ChatHelper.logOut()
        ChatHelper.destroy()
        QbDialogHolder.clear()
    }

    private fun logOutFromFacebook(callback: () -> Unit) {
        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired
        if (isLoggedIn) {
            LoginManager.getInstance().logOut()
            callback()
            return
        }
        callback()
    }

}