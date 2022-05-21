package com.i69app.gifts

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.apollographql.apollo3.exception.ApolloException
import com.i69app.GetUserSendGiftQuery
import com.i69app.data.models.ModelGifts
import com.i69app.databinding.FragmentRealGiftsBinding
import com.i69app.singleton.App
import com.i69app.ui.adapters.AdapterGifts
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.screens.SplashActivity
import com.i69app.ui.viewModels.UserViewModel
import com.i69app.utils.apolloClient
import com.i69app.utils.snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class FragmentSendGifts(val userId: String? = null) : BaseFragment<FragmentRealGiftsBinding>() {

    var giftsAdapter: AdapterGifts? = null
    var list: MutableList<ModelGifts.Data.AllRealGift> = mutableListOf()

    private val viewModel: UserViewModel by activityViewModels()
    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentRealGiftsBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        giftsAdapter = AdapterGifts(requireContext(), list)
        binding.recyclerViewGifts.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerViewGifts.setHasFixedSize(true)
        binding.recyclerViewGifts.adapter = giftsAdapter
        showProgressView()
        getSendGiftIndex()
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

    private fun getSendGiftIndex() {
        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(
                    requireContext(),
                    userId!!
                ).query(GetUserSendGiftQuery(userId = userId!!))
                    .execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                binding.root.snackbar("Exception getGiftIndex ${e.message}")
                return@launchWhenResumed
            }

            Timber.d("apolloResponse getGiftIndex ${res.hasErrors()}")
            if (res.hasErrors() == false) {
                val sendGiftList = res.data?.allUserGifts?.edges
                res.data?.allUserGifts?.edges?.forEach { it ->
                    Timber.d("apolloResponse getGiftIndex ${it?.node?.gift?.giftName}")
                }
                list.clear()
                sendGiftList?.forEach { edge ->
                    edge?.node?.gift?.let {
                        list.add(
                            ModelGifts.Data.AllRealGift(
                                id = it.id,
                                cost = it.cost,
                                giftName = it.giftName,
                                picture = it.picture, type = it.type.rawValue
                            )
                        )
                    }
                }
                giftsAdapter?.notifyDataSetChanged()
            }
            if(res.hasErrors())
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
            hideProgressView()
        }
    }

    override fun setupClickListeners() {}
}