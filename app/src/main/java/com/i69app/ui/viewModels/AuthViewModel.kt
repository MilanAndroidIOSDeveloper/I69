package com.i69app.ui.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebook.login.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import com.i69app.data.models.Id
import com.i69app.data.models.User
import com.i69app.data.remote.repository.AppRepository
import com.i69app.data.remote.repository.CoinRepository
import com.i69app.data.remote.repository.LoginRepository
import com.i69app.data.remote.repository.UserUpdateRepository
import com.i69app.data.remote.requests.LoginRequest
import com.i69app.data.remote.responses.CoinsResponse
import com.i69app.data.remote.responses.DefaultPicker
import com.i69app.data.remote.responses.LoginResponse
import com.i69app.data.remote.responses.ResponseBody
import com.i69app.utils.Resource
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val appRepository: AppRepository,
    val userUpdateRepository: UserUpdateRepository,
    private val coinRepository: CoinRepository,

    ) : ViewModel() {

    private var authUser: User? = null
    var token: String? = null


    fun getDefaultPickers(userToken: String): LiveData<DefaultPicker> = appRepository.getDefaultPickers(viewModelScope, userToken)

    suspend fun login(loginRequest: LoginRequest): Resource<ResponseBody<LoginResponse>> = loginRepository.login(loginRequest)

    fun getUserDataFromFacebook(loginResult: LoginResult, callback: (String?, ArrayList<String>?) -> Unit) =
        loginRepository.getUserDataFromFacebook(loginResult, callback)

    /// Coin
    suspend fun deductCoin(userId: String, token: String, deductCoinMethod: com.i69app.data.enums.DeductCoinMethod): Resource<ResponseBody<CoinsResponse>> =
        coinRepository.deductCoin(userId, token = token, deductCoinMethod)

    fun getAuthUser() = authUser

    fun setAuthUser(updated: User) {
        authUser = updated
    }

    /// Update
    suspend fun updateProfile(user: User, token: String): Resource<ResponseBody<Id>> = userUpdateRepository.updateProfile(user, token = token)

    suspend fun uploadImage(userId: String, token: String, filePath: String): Resource<ResponseBody<Id>> =
        userUpdateRepository.uploadImage(userId = userId, token = token, filePath)

}