package com.i69app.billing

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

private const val TAG = "Billing"
private const val RECONNECT_TIMER_START_MILLISECONDS = 1L * 1000L
private const val RECONNECT_TIMER_MAX_TIME_MILLISECONDS = 1000L * 60L * 15L // 15 minutes
private const val SKU_DETAILS_REQUERY_TIME = 1000L * 60L * 60L * 4L // 4 hours

@Singleton
class BillingDataSource @Inject constructor(
    application: Application,
    private val defaultScope: CoroutineScope,
    knownInAppSkus: Array<String>
) : LifecycleObserver, PurchasesUpdatedListener, BillingClientStateListener {

    private val billingClient: BillingClient
    private val knownInAppSKUs: List<String> = listOf(*knownInAppSkus)

    private var reconnectMilliseconds = RECONNECT_TIMER_START_MILLISECONDS
    private var skuDetailsResponseTime = -SKU_DETAILS_REQUERY_TIME

    // Flows that are mostly maintained so they can be transformed into observables.
    private val skuStateMap: MutableMap<String, MutableStateFlow<SkuState>> = HashMap()
    private val skuDetailsMap: MutableMap<String, MutableStateFlow<SkuDetails?>> = HashMap()

    // Observables that are used to communicate state.
    private val purchaseConsumptionInProcess: MutableSet<Purchase> = HashSet()
    private val newPurchaseFlow = MutableSharedFlow<List<String>>(extraBufferCapacity = 1)
    private val purchaseConsumedFlow = MutableSharedFlow<List<String>>()
    private val billingFlowInProcess = MutableStateFlow(false)

    private enum class SkuState {
        SKU_STATE_UNPURCHASED,
        SKU_STATE_PENDING,
        SKU_STATE_PURCHASED,
        SKU_STATE_PURCHASED_AND_ACKNOWLEDGED
    }

    companion object {
        @Volatile
        private var mInstance: BillingDataSource? = null
        private val handler = Handler(Looper.getMainLooper())

        // Standard boilerplate double check locking pattern for thread-safe singletons.
        @JvmStatic
        fun getInstance(
            application: Application,
            defaultScope: CoroutineScope,
            knownInAppSkus: Array<String>
        ) = mInstance ?: synchronized(this) {
            mInstance ?: BillingDataSource(
                application,
                defaultScope,
                knownInAppSkus
            )
                .also { mInstance = it }
        }
    }

    init {
        initializeFlows()
        billingClient = BillingClient.newBuilder(application)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(this)
    }

    private fun initializeFlows() {
        addSkuFlows(knownInAppSKUs)
    }

    private fun addSkuFlows(skuList: List<String>) {
        skuList.forEach { sku ->
            val skuState = MutableStateFlow(SkuState.SKU_STATE_UNPURCHASED)
            val details = MutableStateFlow<SkuDetails?>(null)

            details.subscriptionCount.map { count -> count > 0 }
                .distinctUntilChanged() // Only react to  true<->false  changes
                .onEach { isActive -> // Configure an action
                    if (isActive && SystemClock.elapsedRealtime() - skuDetailsResponseTime > SKU_DETAILS_REQUERY_TIME) {
                        skuDetailsResponseTime = SystemClock.elapsedRealtime()
                        Timber.tag(TAG).d("Skus not fresh, re-querying")
                        querySkuDetailsAsync()
                    }
                }
                .launchIn(defaultScope)

            skuStateMap[sku] = skuState
            skuDetailsMap[sku] = details
        }
    }

    fun getNewPurchases() = newPurchaseFlow.asSharedFlow()


    //// Billing Setup
    override fun onBillingSetupFinished(billingResult: BillingResult) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Timber.tag(TAG).d("Billing Setup Finished: Response: $responseCode  Msg: $debugMessage")

        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                // The BillingClient is ready. You can query purchases here.
                // This doesn't mean that your app is set up correctly in the console -- it just
                // means that you have a connection to the Billing service.
                reconnectMilliseconds = RECONNECT_TIMER_START_MILLISECONDS
                defaultScope.launch {
                    querySkuDetailsAsync()
                    refreshPurchases()
                }
            }
            else -> retryBillingServiceConnectionWithExponentialBackoff()
        }
    }

    override fun onBillingServiceDisconnected() {
        // Try to restart the connection on the next request to Google Play by calling the startConnection() method.
        Timber.tag(TAG).d("Billing Service Disconnected")
        retryBillingServiceConnectionWithExponentialBackoff()
    }

    private fun retryBillingServiceConnectionWithExponentialBackoff() {
        handler.postDelayed(
            { billingClient.startConnection(this) }, reconnectMilliseconds
        )
        reconnectMilliseconds = min(reconnectMilliseconds * 2, RECONNECT_TIMER_MAX_TIME_MILLISECONDS)
    }


    /// Query
    private fun querySkuDetailsAsync() {
        val skuDetailsParams = SkuDetailsParams.newBuilder()
            .setType(BillingClient.SkuType.INAPP)
            .setSkusList(knownInAppSKUs)
            .build()

        billingClient.querySkuDetailsAsync(skuDetailsParams) { billingResult, skuDetailsList ->
            Timber.tag(TAG).d("Query Sku Details: $skuDetailsList")
            onSkuDetailsResponse(billingResult, skuDetailsList)
        }
    }

    private suspend fun refreshPurchases() {
        Timber.tag(TAG).d("Refreshing Purchases.")
        val purchasesResult = billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP)
        val billingResult = purchasesResult.billingResult

        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            Timber.tag(TAG).d("Problem getting purchases: ${billingResult.debugMessage}")
        } else {
            processPurchaseList(purchasesResult.purchasesList, knownInAppSKUs)
        }
    }

    private fun onSkuDetailsResponse(billingResult: BillingResult, skuDetailsList: List<SkuDetails>?) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Timber.tag(TAG).d("on Sku Details Response: $responseCode  Msg: $debugMessage")

        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                if (skuDetailsList.isNullOrEmpty()) {
                    Timber.tag(TAG).e("on Sku Details Response: Found null or empty SkuDetails. Check to see if the SKUs you requested are correctly published in the Google Play Console.")
                } else {
                    skuDetailsList.forEach { skuDetails ->
                        val sku = skuDetails.sku
                        val detailsMutableFlow = skuDetailsMap[sku]
                        detailsMutableFlow?.tryEmit(skuDetails)
                            ?: Timber.tag(TAG).d("Unknown sku: $sku")
                    }
                }
            }

            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE,
            BillingClient.BillingResponseCode.DEVELOPER_ERROR,
            BillingClient.BillingResponseCode.ERROR -> Timber.tag(TAG).e("on Sku Details Response: $responseCode  Msg: $debugMessage")
            BillingClient.BillingResponseCode.USER_CANCELED -> Timber.tag(TAG).i("on Sku Details Response: $responseCode  Msg: $debugMessage")
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED,
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED,
            BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> Timber.tag(TAG).wtf("on Sku Details Response: $responseCode  Msg: $debugMessage")
            else -> Timber.tag(TAG).wtf("on Sku Details Response: $responseCode  Msg: $debugMessage")
        }
        skuDetailsResponseTime = if (responseCode == BillingClient.BillingResponseCode.OK) SystemClock.elapsedRealtime() else -SKU_DETAILS_REQUERY_TIME
    }


    private fun setSkuState(sku: String, newSkuState: SkuState) {
        val skuStateFlow = skuStateMap[sku]
        skuStateFlow?.tryEmit(newSkuState)
            ?: Timber.tag(TAG).e("Unknown SKU $sku. Check to make sure SKU matches SKUS in the Play developer console.")
    }

    private fun setSkuStateFromPurchase(purchase: Purchase) {
        purchase.skus.forEach { purchaseSku ->
            val skuStateFlow = skuStateMap[purchaseSku]
            if (skuStateFlow == null) {
                Timber.tag(TAG).e("Unknown SKU $purchaseSku. Check to make sure SKU matches SKUS in the Play developer console.")

            } else {
                when (purchase.purchaseState) {
                    Purchase.PurchaseState.PENDING -> skuStateFlow.tryEmit(SkuState.SKU_STATE_PENDING)
                    Purchase.PurchaseState.UNSPECIFIED_STATE -> skuStateFlow.tryEmit(SkuState.SKU_STATE_UNPURCHASED)
                    Purchase.PurchaseState.PURCHASED ->
                        if (purchase.isAcknowledged)
                            skuStateFlow.tryEmit(SkuState.SKU_STATE_PURCHASED_AND_ACKNOWLEDGED)
                        else
                            skuStateFlow.tryEmit(SkuState.SKU_STATE_PURCHASED)
                    else -> Timber.tag(TAG).e("Purchase in unknown state: ${purchase.purchaseState}")
                }
            }
        }
    }

    fun getSkuDetails(sku: String): Flow<SkuDetails> {
        val skuDetailsFlow = skuDetailsMap[sku]!!
        Timber.tag(TAG).d("Sku Details:  ${skuDetailsFlow.value}")
        return skuDetailsFlow.mapNotNull { skuDetails ->
            skuDetails
        }
    }

    fun getConsumedPurchases() = purchaseConsumedFlow.asSharedFlow()


    /// Consume
    suspend fun consumeInAppPurchase(sku: String) {
        val pr = billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP)
        val br = pr.billingResult
        val purchasesList = pr.purchasesList

        if (br.responseCode != BillingClient.BillingResponseCode.OK) {
            Timber.tag(TAG).e("Problem getting purchases: ${br.debugMessage}")

        } else {
            for (purchase in purchasesList) {
                // for right now any bundle of SKUs must all be consumable
                for (purchaseSku in purchase.skus) {
                    if (purchaseSku == sku) {
                        consumePurchase(purchase)
                        return
                    }
                }
            }
        }
        Timber.tag(TAG).e("Unable to consume SKU: $sku Sku not found.")
    }

    private suspend fun consumePurchase(purchase: Purchase) {
        // weak check to make sure we're not already consuming the sku
        if (purchaseConsumptionInProcess.contains(purchase)) {
            // already consuming
            return
        }
        purchaseConsumptionInProcess.add(purchase)

        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        val consumePurchaseResult = billingClient.consumePurchase(consumeParams)
        purchaseConsumptionInProcess.remove(purchase)

        if (consumePurchaseResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Timber.tag(TAG).d("Consumption successful. Emitting sku.")
            defaultScope.launch {
                purchaseConsumedFlow.emit(purchase.skus)
            }
            // Since we've consumed the purchase
            for (sku in purchase.skus) {
                setSkuState(sku, SkuState.SKU_STATE_UNPURCHASED)
            }
        } else {
            Timber.tag(TAG).e("Error while consuming: ${consumePurchaseResult.billingResult.debugMessage}")
        }
    }


    /// Launch
    fun launchBillingFlow(activity: Activity, sku: String) {
        val skuDetails = skuDetailsMap[sku]?.value
        Timber.tag(TAG).d("Sku Details $skuDetails")

        skuDetails?.let {
            val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(it)
                .build()

            defaultScope.launch {
                val br = billingClient.launchBillingFlow(activity, flowParams)

                when (br.responseCode) {
                    BillingClient.BillingResponseCode.OK -> billingFlowInProcess.emit(true)
                    else -> Timber.tag(TAG).d("Billing failed: ${br.debugMessage}")
                }
            }
        }
    }

    fun getBillingFlowInProcess(): Flow<Boolean> = billingFlowInProcess.asStateFlow()


    @SuppressLint("BinaryOperationInTimber")
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        when (val responseCode = billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Timber.tag(TAG).d("Billing Result OK: $responseCode  Msg: ${billingResult.debugMessage}")
                if (purchases != null) {
                    processPurchaseList(purchases, null)
                    return
                } else Timber.tag(TAG).d("Null Purchase List Returned from OK response!")
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> Timber.tag(TAG).i("onPurchasesUpdated: User canceled the purchase")
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> Timber.tag(TAG).i("onPurchasesUpdated: The user already owns this item")
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> Timber.tag(TAG).e(
                "onPurchasesUpdated: Developer error means that Google Play does not recognize the configuration. " +
                        "If you are just getting started, make sure you have configured the application correctly in the Google Play Console. " +
                        "The SKU product ID must match and the APK you are using must be signed with release keys."
            )
            else -> Timber.tag(TAG).d("Billing Result: $responseCode  Msg: ${billingResult.debugMessage}")
        }
        defaultScope.launch {
            billingFlowInProcess.emit(false)
        }
    }

    private fun processPurchaseList(purchases: List<Purchase>?, skusToUpdate: List<String>?) {
        val updatedSkus = HashSet<String>()

        purchases?.let {
            for (purchase in purchases) {

                for (sku in purchase.skus) {
                    val skuStateFlow = skuStateMap[sku]
                    if (skuStateFlow == null) {
                        Timber.tag(TAG).e("Unknown SKU $skuStateFlow. Check to make sure SKU matches SKUS in the Play developer console.")
                        continue
                    }
                    updatedSkus.add(sku)
                }

                // Global check to make sure all purchases are signed correctly. This check is best performed on your server.
                when (purchase.purchaseState) {
                    Purchase.PurchaseState.PURCHASED -> {
                        if (!isSignatureValid(purchase)) {
                            Timber.tag(TAG).e("Invalid signature. Check to make sure your public key is correct.")
                            continue
                        }

                        // only set the purchased state after we've validated the signature.
                        setSkuStateFromPurchase(purchase)

                        defaultScope.launch {
                            consumePurchase(purchase)
                            newPurchaseFlow.tryEmit(purchase.skus)
                        }
                    }
                    else -> {
                        // make sure the state is set
                        setSkuStateFromPurchase(purchase)
                    }
                }
            }
        }

        skusToUpdate?.let {
            it.forEach { sku ->
                if (!updatedSkus.contains(sku)) setSkuState(sku, SkuState.SKU_STATE_UNPURCHASED)
            }
        }
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun resume() {
        Timber.tag(TAG).d("ON RESUME")
        // this just avoids an extra purchase refresh after we finish a billing flow
        if (!billingFlowInProcess.value) {
            if (billingClient.isReady) {
                defaultScope.launch {
                    refreshPurchases()
                }
            }
        }
    }

    private fun isSignatureValid(purchase: Purchase): Boolean = Security.verifyPurchase(purchase.originalJson, purchase.signature)

}