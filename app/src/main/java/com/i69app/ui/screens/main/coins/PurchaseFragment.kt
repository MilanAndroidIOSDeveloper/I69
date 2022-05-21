package com.i69app.ui.screens.main.coins

import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.i69app.R
import com.i69app.data.config.Constants
import com.i69app.data.remote.requests.PurchaseRequest
import com.i69app.databinding.FragmentPurchaseBinding
import com.i69app.databinding.ItemPurchaseCoinsBinding
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.viewModels.PurchaseCoinsViewModel
import com.i69app.utils.Resource
import com.i69app.utils.setVisibleOrInvisible
import com.i69app.utils.snackbar
import timber.log.Timber

class PurchaseFragment : BaseFragment<FragmentPurchaseBinding>() {

    private val viewModel: PurchaseCoinsViewModel by activityViewModels()
    private var selectedSku: Int = -1
    private var selectedCoins: Int = 0
    private var selectedPrice: Double = 0.0
//    private lateinit var dialog: Dialog

    override fun getStatusBarColor() = R.color.colorPrimary

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentPurchaseBinding.inflate(inflater, container, false)

    override fun setupTheme() {
//        setupDialog()
//        initPayPal()
        showPrice(1, binding.firstPrice, 100, 4.99, "4", ".99", "8.99")
        showPrice(4, binding.secondPrice, 250, 9.99, "9", ".99", "12.99")
        showPrice(2, binding.thirdPrice, 500, 14.99, "14", ".99", "19.99")
        showPrice(3, binding.fourthPrice, 1150, 24.99, "24", ".99", "34.99")
        showPrice(5, binding.fifthPrice, 2550, 49.99, "49", ".99", "66.99")
        showPrice(6, binding.sixthPrice, 5600, 99.99, "99", ".99", "149.99")

        viewModel.consumedPurchase.observe(viewLifecycleOwner, { isPurchased ->
            isPurchased?.let {
                Timber.d("Observing Purchased  $it")
                if (it == 0) onPaymentSuccess("in-app-purchase")
            }
        })
    }

//    private fun setupDialog() {
//        dialog = Dialog(requireContext())
//        with(dialog) {
//            setContentView(R.layout.dialog_payment_options)
//            setCancelable(true)
//            window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//        }
//        dialog.findViewById<ImageButton>(R.id.google_pay).setOnClickListener {
//            dialog.dismiss()
//            val product = getSkuProductById(selectedSku)
//            makePurchase(product)
//        }
//    }

    override fun setupClickListeners() {
        binding.purchaseClose.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun showPrice(
        sku: Int,
        price: ItemPurchaseCoinsBinding,
        coins: Int,
        buyingPrice: Double,
        priceFirst: String,
        priceSecond: String,
        discountPrice: String,
        showDiscount: Boolean = true
    ) {
        price.numberOfCoins.text = "$coins"
        price.priceGoldCircle.setVisibleOrInvisible(showDiscount)
        price.price.text = priceFirst
        price.priceSmall.text = priceSecond
        if (!showDiscount) {
            price.price.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            price.priceSmall.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        }
        price.salePrice.text = Html.fromHtml("<strike>€${discountPrice}</strike>")

        price.root.setOnClickListener {
            selectedSku = sku
            selectedCoins = coins
            selectedPrice = buyingPrice

            val product = getSkuProductById(selectedSku)
            makePurchase(product)
            //dialog.show()
        }
    }

    private fun getSkuProductById(sku: Int) = when (sku) {
        1 -> com.i69app.data.config.Constants.IN_APP_FIRST_TYPE
        2 -> com.i69app.data.config.Constants.IN_APP_SECOND_TYPE
        3 -> com.i69app.data.config.Constants.IN_APP_THIRD_TYPE
        4 -> com.i69app.data.config.Constants.IN_APP_FOURTH_TYPE
        5 -> com.i69app.data.config.Constants.IN_APP_FIFTH_TYPE
        6 -> com.i69app.data.config.Constants.IN_APP_SIXTH_TYPE
        else -> com.i69app.data.config.Constants.IN_APP_FIRST_TYPE
    }

//    private fun initPayPal() {
//        val payPalBtn = dialog.findViewById<PayPalButton>(R.id.payPalButton)
//        selectedPrice = 10.0
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            payPalBtn.setup(
//                createOrder = CreateOrder { createOrderActions ->
//                    val order = Order(
//                        intent = OrderIntent.CAPTURE,
//                        appContext = AppContext(
//                            userAction = Constants.PAYPAL_USER_ACTION
//                        ),
//                        purchaseUnitList = listOf(
//                            PurchaseUnit(
//                                amount = Amount(
//                                    currencyCode = Constants.PAYPAL_CURRENCY,
//                                    value = selectedPrice.toString()
//                                )
//                            )
//                        )
//                    )
//                    createOrderActions.create(order)
//                },
//                onApprove = OnApprove { approval ->
//                    dialog.dismiss()
//                    approval.orderActions.capture { captureOrderResult ->
//                        Timber.d("CaptureOrderResult: $captureOrderResult")
//                        onPaymentSuccess("paypal")
//                    }
//                },
//                onCancel = OnCancel {
//                    dialog.dismiss()
//                    Timber.e("Buyer canceled the PayPal experience.")
//                },
//                onError = OnError { errorInfo ->
//                    dialog.dismiss()
//                    onFailureListener(errorInfo.reason)
//                }
//            )
//        }
//    }

    private fun makePurchase(sku: String) {
        viewModel.buySku(requireActivity(), sku)
    }

    private fun onPaymentSuccess(method: String) {
        showProgressView()

        lifecycleScope.launch(Dispatchers.Main) {
            val userId = getCurrentUserId()!!
            val userToken = getCurrentUserToken()!!

            val purchaseRequest = PurchaseRequest(
                id = userId,
                method = method,
                coins = selectedCoins,
                money = selectedPrice,
            )

            when (val response = viewModel.purchaseCoin(purchaseRequest, token = userToken)) {
                is Resource.Success -> {
                    hideProgressView()
                    if (response.data?.data!!.success) {
                        viewModel.loadCurrentUser(userId, token = userToken, true)
                        congratulationsToast(selectedCoins)

                    } else {
                        onFailureListener(getString(R.string.something_went_wrong))
                    }
                }
                is Resource.Error -> onFailureListener(response.message ?: "")
            }
        }
    }

    private fun congratulationsToast(coins: Int) {
        binding.root.snackbar(String.format(getString(R.string.congrats_purchase), coins))
    }

    private fun onFailureListener(error: String) {
        hideProgressView()
        Timber.e("${getString(R.string.something_went_wrong)} $error")
        binding.root.snackbar("${getString(R.string.something_went_wrong)} $error")
    }

}