package com.i69app.gifts

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo3.exception.ApolloException
import com.i69app.GetAllUserMomentsQuery
import com.i69app.GetReceivedGiftsQuery
import com.i69app.GetSentGiftsQuery
import com.i69app.R
import com.i69app.databinding.FragmentReceivedSentGiftsBinding
import com.i69app.singleton.App
import com.i69app.ui.adapters.AdapterReceiveGifts
import com.i69app.ui.adapters.AdapterSentGifts
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.screens.SplashActivity
import com.i69app.ui.screens.main.search.userProfile.PicViewerFragment
import com.i69app.ui.screens.main.search.userProfile.SearchUserProfileFragment
import com.i69app.ui.viewModels.UserViewModel
import com.i69app.utils.AnimationTypes
import com.i69app.utils.apolloClient
import com.i69app.utils.navigate
import com.i69app.utils.snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class FragmentSentGifts: BaseFragment<FragmentReceivedSentGiftsBinding>(),AdapterSentGifts.SentGiftPicUserPicInterface {
    lateinit var giftsAdapter: AdapterSentGifts

    var list : MutableList<GetSentGiftsQuery.Edge?> = mutableListOf()
    private val viewModel: UserViewModel by activityViewModels()

    private var userId: String? = null
    private var userToken: String? = null

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentReceivedSentGiftsBinding.inflate(inflater, container, false)

    override fun setupTheme() {

        lifecycleScope.launch {
            userId = getCurrentUserId()!!
            userToken = getCurrentUserToken()!!
            Timber.i("usertokenn $userToken")
        }
        Timber.i("userID $userId")


        giftsAdapter = AdapterSentGifts(requireContext(),this@FragmentSentGifts, list)
        binding.recyclerViewGifts.setHasFixedSize(true)
        binding.recyclerViewGifts.adapter = giftsAdapter


        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(requireContext(), userToken!!).query(GetSentGiftsQuery(userId!!)).execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                binding.root.snackbar("Exception received gift ${e.message}")
                hideProgressView()
                return@launchWhenResumed
            }


            if (res.hasErrors() == false) {
                val allreceivedgift = res.data?.allUserGifts!!.edges
                if (allreceivedgift.size != 0) {
                    list.clear()
                    list.addAll(allreceivedgift)
                    giftsAdapter?.notifyDataSetChanged()
                }

                if (binding.recyclerViewGifts.itemDecorationCount == 0) {
                    binding.recyclerViewGifts.addItemDecoration(object :
                        RecyclerView.ItemDecoration() {
                        override fun getItemOffsets(
                            outRect: Rect,
                            view: View,
                            parent: RecyclerView,
                            state: RecyclerView.State
                        ) {
                            outRect.top = 10
                        }
                    })
                }
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

    override fun setupClickListeners() {}
    override fun onpiclicked(url: String) {



        val dialog = PicViewerFragment()
        val b = Bundle()
        b.putString("url", url)
        b.putString("mediatype", "image")

        dialog.arguments = b
        dialog.show(childFragmentManager, "GiftpicViewer")

    }

    override fun onuserpiclicked(userid: String) {
        val bundle = Bundle()
        bundle.putBoolean(SearchUserProfileFragment.ARGS_FROM_CHAT, false)
        bundle.putString("userId", userid)
        findNavController().navigate(
            destinationId = R.id.action_global_otherUserProfileFragment,
            popUpFragId = null,
            animType = AnimationTypes.SLIDE_ANIM,
            inclusive = true,
            args = bundle
        )
    }
}