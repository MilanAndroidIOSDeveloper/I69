package com.i69app.ui.viewModels

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.i69app.billing.BillingRepository
import com.i69app.data.remote.repository.CoinRepository
import com.i69app.data.remote.repository.UserDetailsRepository
import com.i69app.data.remote.requests.PurchaseRequest
import com.i69app.data.remote.responses.CoinsResponse
import com.i69app.data.remote.responses.ResponseBody
import com.i69app.utils.Resource
import javax.inject.Inject

@HiltViewModel
class PurchaseCoinsViewModel @Inject constructor(
    private val coinRepository: CoinRepository,
    private val userDetailsRepository: UserDetailsRepository,
    private val billingRepository: BillingRepository
) : ViewModel() {

    val consumedPurchase: LiveData<Int>
        get() = billingRepository.consumedPurchase.asLiveData()

    fun buySku(activity: Activity, sku: String) {
        billingRepository.buySku(activity, sku)
    }

    fun loadCurrentUser(userId: String, token: String, reload: Boolean) = userDetailsRepository.getCurrentUser(viewModelScope, userId, token = token, reload)

    suspend fun purchaseCoin(purchaseRequest: PurchaseRequest, token: String): Resource<ResponseBody<CoinsResponse>> = coinRepository.purchaseCoin(purchaseRequest, token)

}