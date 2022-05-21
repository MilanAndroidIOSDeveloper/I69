package com.i69app.billing

import android.app.Activity
import androidx.lifecycle.LifecycleObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "Billing"

@Singleton
class BillingRepository @Inject constructor(
    private val billingDataSource: BillingDataSource,
    defaultScope: CoroutineScope
) {

    val billingLifecycleObserver: LifecycleObserver
        get() = billingDataSource

    private val consumedPurchaseMessages: MutableSharedFlow<Int> = MutableSharedFlow()

    val consumedPurchase: Flow<Int>
        get() = consumedPurchaseMessages

    init {
        defaultScope.launch {
            billingDataSource.getConsumedPurchases().collect {
                it.forEach { sku ->
                    Timber.tag(TAG).d("Consumed Purchases SKU : $sku")
                    consumedPurchaseMessages.emit(0)
                }
            }
        }
    }

    fun buySku(activity: Activity, sku: String) {
        billingDataSource.launchBillingFlow(activity, sku)
    }

}