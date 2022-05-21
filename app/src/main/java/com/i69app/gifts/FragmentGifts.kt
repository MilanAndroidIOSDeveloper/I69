package com.i69app.gifts

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.exception.ApolloException
import com.i69app.GiftPurchaseMutation
import com.i69app.data.models.ModelGifts
import com.i69app.databinding.FragmentGiftsBinding
import com.i69app.di.modules.AppModule
import com.i69app.singleton.App
import com.i69app.ui.base.profile.BaseGiftsFragment
import com.i69app.ui.screens.SplashActivity
import com.i69app.ui.viewModels.UserViewModel
import com.i69app.utils.apolloClient
import com.i69app.utils.getResponse
import com.i69app.utils.snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class FragmentGifts: BaseGiftsFragment() {

    var purchaseGiftFor: String?=""
    private val viewModel: UserViewModel by viewModels()

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentGiftsBinding.inflate(inflater, container, false)

    override fun setupClickListeners() {
        binding.purchaseButton.setOnClickListener {
            var items: MutableList<ModelGifts.Data.AllRealGift> = mutableListOf()
            fragVirtualGifts?.giftsAdapter?.getSelected()?.let { it1 -> items.addAll(it1) }
            fragRealGifts?.giftsAdapter?.getSelected()?.let { it1 -> items.addAll(it1) }

            lifecycleScope.launchWhenCreated() {
                if (items.size > 0) {
                    showProgressView()
                    items.forEach { gift ->

                        var res: ApolloResponse<GiftPurchaseMutation.Data>? = null
                        try {
                            res = apolloClient(
                                requireContext(),
                                getCurrentUserToken()!!
                            ).mutation(GiftPurchaseMutation(gift.id, purchaseGiftFor!!)).execute()
                        } catch (e: ApolloException) {
                            Timber.d("apolloResponse ${e.message}")
                            binding.root.snackbar("Exception ${e.message}")
                            //hideProgressView()
                            //return@launchWhenResumed
                        }
                        if (res?.hasErrors() == false) {
                            binding.root.snackbar("You bought ${res.data?.giftPurchase?.giftPurchase?.gift?.giftName} successfully!")
                            fireGiftBuyNotificationforreceiver(gift.id)

                        }
                        if(res!!.hasErrors())
                        {
                            if(res.errors!![0].message.equals("User doesn't exist"))
                            {
                                binding.root.snackbar("" + res.errors!![0].message)

                                Handler().postDelayed({ nouserexist() }, 1500)
                            }
                            else {
                                binding.root.snackbar("" + res.errors!![0].message)
                            }


                        }
                        Timber.d("apolloResponse ${res?.hasErrors()} ${res?.data?.giftPurchase?.giftPurchase?.gift?.giftName}")
                    }
                    hideProgressView()
                }
            }
        }
    }

    fun nouserexist()
    {
        lifecycleScope.launch(Dispatchers.Main) {
            App.userPreferences.clear()
            clearAppData()
            val intent = Intent(requireActivity(), SplashActivity::class.java)
            startActivity(intent)
            requireActivity().finishAffinity()
        }
    }


    private fun clearAppData() {
        try {
            // clearing app data
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {

                val activityManager =
                    requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

                activityManager.clearApplicationUserData()

            } else {

                val packageName: String = requireActivity().applicationContext.packageName
                val runtime = Runtime.getRuntime()
                runtime.exec("pm clear $packageName")
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    fun fireGiftBuyNotificationforreceiver(gid: String) {

        lifecycleScope.launchWhenResumed {


            val queryName = "sendNotification"
            val query = StringBuilder()
                .append("mutation {")
                .append("$queryName (")
                .append("userId: \"${purchaseGiftFor!!}\", ")
                .append("notificationSetting: \"GIFT RLVRTL\", ")
                .append("data: {giftId:${gid}}")
                .append(") {")
                .append("sent")
                .append("}")
                .append("}")
                .toString()

            val result= AppModule.provideGraphqlApi().getResponse<Boolean>(
                query,
                queryName, getCurrentUserToken()!!)
            Timber.d("RSLT",""+result.message)

        }








    }
    override fun setupScreen() {
        purchaseGiftFor = requireArguments().getString("userId")
    }
}