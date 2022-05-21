package com.i69app.profile

import android.R
import android.app.ActivityManager
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.apollographql.apollo3.exception.ApolloException
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.i69app.BuildConfig
import com.i69app.GetNotificationCountQuery
import com.i69app.GetUserReceiveGiftQuery
import com.i69app.UpdateCoinMutation
import com.i69app.data.models.Photo
import com.i69app.databinding.AlertFullImageBinding
import com.i69app.databinding.FragmentUserProfileBinding
import com.i69app.gifts.FragmentReceivedGifts
import com.i69app.gifts.FragmentSentGifts
import com.i69app.profile.vm.VMProfile
import com.i69app.singleton.App
import com.i69app.ui.adapters.UserItemsAdapter
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.screens.SplashActivity
import com.i69app.ui.screens.main.MainActivity
import com.i69app.ui.screens.main.notification.NotificationDialogFragment
import com.i69app.ui.screens.main.search.userProfile.getimageSliderIntent
import com.i69app.ui.viewModels.UserViewModel
import com.i69app.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class UserProfileFragment : BaseFragment<FragmentUserProfileBinding>() {
    private var userToken: String? = null
    private var userId: String? = null

    private lateinit var GiftbottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    var fragReceivedGifts: FragmentReceivedGifts?= null
    var fragSentGifts: FragmentSentGifts?= null

    private val tabIcons = intArrayOf(
        com.i69app.R.drawable.pink_gift_noavb,
        com.i69app.R.drawable.pink_gift_noavb
    )

    private val viewModel by lazy {
        activity?.let { ViewModelProviders.of(it).get(VMProfile::class.java) }
    }

    private val viewModels: UserViewModel by activityViewModels()

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentUserProfileBinding.inflate(inflater, container, false).apply {
            viewModel?.isMyUser = true
            this.vm = viewModel
        }

    private val addSliderImageIntent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

            Timber.d("RESULT" + result)

        }

    override fun setupTheme() {
        navController = findNavController()

        lifecycleScope.launch {
            userToken = getCurrentUserToken()!!
            userId = getCurrentUserId()!!

            Timber.i("usertokenn $userToken")
        }
        Timber.i("usertokenn 2 $userToken")
        viewModel?.getProfile()

        getTypeActivity<MainActivity>()?.enableNavigationDrawer()
        binding.initChatMsgBtn.visibility = View.GONE

        binding.actionGifts1.visibility = View.VISIBLE
        //binding.actionGifts.visibility = View.GONE
        binding.actionCoins.visibility = View.VISIBLE

        viewModel?.data?.observe(this) { data ->
            Timber.d("observe: $data")
            if (data != null) {
                if (data.user != null) {
                    if (data.user!!.avatarPhotos != null && data.user!!.avatarPhotos!!.size != 0) {
                        binding.userImgHeader.setIndicatorVisibility(View.VISIBLE)
                        if (data.user!!.giftCoins <= 0) {
                            binding.giftCounter.visibility = View.GONE
                            //binding.bell.setImageDrawable(ContextCompat.getDrawable(requireContext(), com.i69app.R.drawable.notification1))
                        } else {
                            binding.giftCounter.visibility = View.VISIBLE
                            binding.giftCounter.text = "${data.user!!.giftCoins}"
                            //binding.bell.setImageDrawable(ContextCompat.getDrawable(requireContext(), com.i69app.R.drawable.notification2))
                        }

                        try {
                            binding.userImgHeader.setImageListener { position, imageView ->

                                if (position <= data.user!!.avatarPhotos!!.size) {
                                    data.user?.avatarPhotos?.get(position)?.let {
                                        imageView.loadImage(
                                            it.url.replace(
                                                "http://95.216.208.1:8000/media/",
                                                "${BuildConfig.BASE_URL}media/"
                                            )
                                        )
                                    }
                                }
                                imageView.setOnClickListener {

                                    if (data.user!!.avatarPhotos != null && data.user!!.avatarPhotos!!.size != 0) {

                                        val dataarray: ArrayList<Photo> = ArrayList()
                                        data.user!!.avatarPhotos!!.indices.forEach { i ->

                                            val photo_ = data.user!!.avatarPhotos!![i]
                                            dataarray.add(photo_)
                                        }


                                        addSliderImageIntent.launch(
                                            getimageSliderIntent(
                                                requireActivity(),
                                                Gson().toJson(dataarray),
                                                position
                                            )
                                        )


//
//                                        data.user?.avatarPhotos?.get(position)?.let { it1 ->
//                                            showImageDialog(
//                                                it1.url.replace(
//                                                    "http://95.216.208.1:8000/media/",
//                                                    "${BuildConfig.BASE_URL}media/"
//                                                )
//                                            )
//                                        }
                                    }

                                }

                            }
                            binding.userImgHeader.pageCount = data.user!!.avatarPhotos!!.size

                        } catch (e: Exception) {
                        }
                    } else {

                        for (f in 0 until binding.userImgHeader.pageCount) {


                            binding.userImgHeader.removeViewAt(f)
                            binding.userImgHeader.setIndicatorVisibility(View.GONE)


                        }


                    }
                }
            }


            if (data != null) {
                if (data.user != null) {
                    if (data.user!!.avatarPhotos != null) {

                        if (data.user?.avatarPhotos!!.size != 0) {
                            binding.userProfileImg.loadCircleImage(
                                data.user!!.avatarPhotos!!.get(
                                    data?.user!!.avatarIndex!!
                                ).url.replace(
                                    "http://95.216.208.1:8000/media/",
                                    "${BuildConfig.BASE_URL}media/"
                                )
                            )

                        }

                    }
                }


            }

            binding.profileTabs.setupWithViewPager(binding.userDataViewPager)
            binding.userDataViewPager.adapter =
                viewModel?.setupViewPager(childFragmentManager, data?.user, data?.defaultPicker)
//            binding.userImgHeader.pageCount = data?.user?.avatarPhotos?.size ?: 1

        }


        adjustScreen()
        GiftbottomSheetBehavior = BottomSheetBehavior.from<ConstraintLayout>(binding.giftbottomSheet)
        GiftbottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        Timber.d("Slide Up")

                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {

                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        Timber.d("Slide Down")

                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {

                    }
                    BottomSheetBehavior.STATE_SETTLING -> {

                    }
                }
            }
        })

        binding.giftsTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
                binding.giftsPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
            }
        })
        binding.giftsTabs.setupWithViewPager(binding.giftsPager)
        setupViewPager(binding.giftsPager)
        binding.giftsTabs.getTabAt(0)!!.setIcon(tabIcons[0])
        binding.giftsTabs.getTabAt(1)!!.setIcon(tabIcons[1])
        binding.giftsTabs.setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {

                // no where in the code it is defined what will happen when tab is tapped/selected by the user
                // this is why the following line is necessary
                // we need to manually set the correct fragment when a tab is selected/tapped
                // and this is the problem in your code

                if(tab.position == 0)
                {
                    binding.unametitle.text = "SENDER"
                }
                else if(tab.position == 1)
                {
                    binding.unametitle.text = "BENEFICIARY NAME"

                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {


                // Reload your recyclerView here
            }
        })
    }
    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = UserItemsAdapter(childFragmentManager)
        fragReceivedGifts = FragmentReceivedGifts()
        fragSentGifts = FragmentSentGifts()

        adapter.addFragItem(fragReceivedGifts!!, getString(com.i69app.R.string.rec_gifts))
        adapter.addFragItem(fragSentGifts!!, getString(com.i69app.R.string.sent_gifts))
        viewPager.adapter = adapter

//        viewPager.addOnPageChangeListener()
    }
    private fun showImageDialog(imageUrl: String) {

        val dialog =
            Dialog(requireContext(), R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
        val dialogBinding = AlertFullImageBinding.inflate(layoutInflater, null, false)
        dialogBinding.fullImg.loadImage(imageUrl) {
            dialogBinding.alertTitle.setViewGone()
        }
        dialog.setContentView(dialogBinding.root)
        dialog.show()
        dialogBinding.root.setOnClickListener {
            dialog.cancel()
        }
        dialogBinding.alertTitle.setViewGone()

    }

    private fun getNotificationIndex() {

        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(requireContext(), userToken!!).query(GetNotificationCountQuery())
                    .execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                binding.root.snackbar("Exception NotificationIndex ${e.message}")
                return@launchWhenResumed
            }
            Timber.d("apolloResponse NotificationIndex ${res.hasErrors()}")

            if (res?.hasErrors() == false) {

                val NotificationCount = res.data?.unseenCount
                if (NotificationCount == null || NotificationCount == 0) {
                    binding.counter.visibility = View.GONE
                    binding.bell.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            com.i69app.R.drawable.notification1
                        )
                    )

                } else {
                    binding.counter.visibility = View.VISIBLE
                    binding.counter.setText("" + NotificationCount)
                    binding.bell.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            com.i69app.R.drawable.notification2
                        )
                    )


                }
            }
            if (res!!.hasErrors()) {
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
    override fun onResume() {
        getNotificationIndex()
        //getReceivedGiftIndex()
        super.onResume()
    }

//    private fun getReceivedGiftIndex() {
//        lifecycleScope.launchWhenResumed {
//            val res = try {
//                apolloClient(
//                    requireContext(),
//                    userToken!!
//                ).query(GetUserReceiveGiftQuery(receiverId = userId!!))
//                    .execute()
//            } catch (e: ApolloException) {
//                Timber.d("apolloResponse ${e.message}")
//                binding.root.snackbar("Exception getGiftIndex ${e.message}")
//                return@launchWhenResumed
//            }
//            Timber.d("apolloResponse getGiftIndex ${res.hasErrors()}")
//
//            val receiveGiftList = res.data?.allUserGifts?.edges
//            res.data?.allUserGifts?.edges?.forEach { it ->
//                Timber.d("apolloResponse getGiftIndex ${it?.node?.gift?.giftName}")
//            }
//
//            if (receiveGiftList?.size == null || receiveGiftList.isEmpty()) {
//                binding.giftCounter.visibility = View.GONE
//                //binding.bell.setImageDrawable(ContextCompat.getDrawable(requireContext(), com.i69app.R.drawable.notification1))
//            } else {
//                binding.giftCounter.visibility = View.VISIBLE
//                binding.giftCounter.text = "${receiveGiftList.size}"
//                var totalCount = 0
//                receiveGiftList.forEach {
//                    totalCount += it?.node?.gift?.cost?.toInt()!!
//                }
//                updateCoins(totalCount)
//                //binding.bell.setImageDrawable(ContextCompat.getDrawable(requireContext(), com.i69app.R.drawable.notification2))
//            }
//        }
//    }



    override fun setupClickListeners() {
        var bundle = Bundle()
        bundle.putString("userId", userId)
        viewModel?.onSendMsg = { requireActivity().onBackPressed() }
        binding.actionBack.setOnClickListener { requireActivity().onBackPressed() }
        viewModel?.onDrawer = { (activity as MainActivity).drawerSwitchState() }
        viewModel?.onEditProfile =
            { navController.navigate(com.i69app.R.id.action_userProfileFragment_to_userEditProfileFragment) }
        viewModel?.onGift = {
            // Toast.makeText(activity,"User can't bought gift yourself", Toast.LENGTH_LONG).show()

            //            Toast.makeText(activity,"User can't bought gift yourself", Toast.LENGTH_LONG).show()

            binding.purchaseButton.visibility = View.GONE
            binding.topl.visibility = View.VISIBLE

            if (GiftbottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                GiftbottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
//            buttonSlideUp.text = "Slide Down";

            } else {
                GiftbottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED;
//            buttonSlideUp.text = "Slide Up"

            }




//            navController.navigate(com.i69app.R.id.action_userProfileFragment_to_userGiftsFragment,bundle)


        }

        binding.bell.setOnClickListener {
            val dialog =
                NotificationDialogFragment(userToken, binding.counter, userId, binding.bell)
            dialog.show(childFragmentManager, "notifications")
        }
    }

    private fun adjustScreen() {
        var height = Utils.getScreenHeight()
        var calculated = (height * 70) / 100
        Timber.d("Height: $height Calculated 75%: $calculated")

        var params = binding?.userCollapseToolbar?.layoutParams
        params.height = calculated
        binding?.userCollapseToolbar?.layoutParams = params
    }
}