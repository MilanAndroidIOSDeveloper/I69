package com.i69app.ui.screens.main.search.userProfile

import android.app.ActivityManager
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.exception.ApolloException
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.i69app.BuildConfig
import com.i69app.CreateChatMutation
import com.i69app.GiftPurchaseMutation
import com.i69app.R
import com.i69app.data.models.MessagePreviewModel
import com.i69app.data.models.ModelGifts
import com.i69app.data.models.Photo
import com.i69app.databinding.AlertFullImageBinding
import com.i69app.databinding.FragmentUserProfileBinding
import com.i69app.di.modules.AppModule
import com.i69app.gifts.FragmentRealGifts
import com.i69app.gifts.FragmentVirtualGifts
import com.i69app.profile.vm.VMProfile
import com.i69app.singleton.App
import com.i69app.ui.adapters.UserItemsAdapter
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.screens.SplashActivity
import com.i69app.ui.viewModels.UserViewModel
import com.i69app.utils.*
import com.i69app.utils.qb.QbDialogHolder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


@AndroidEntryPoint
class SearchUserProfileFragment : BaseFragment<FragmentUserProfileBinding>() {
    companion object {
        const val ARGS_FROM_CHAT = "from_chat"
    }

    private val viewModel: VMProfile by activityViewModels()
    private val viewModels: UserViewModel by activityViewModels()

    private var fromChat: Boolean = false
    private var otherUserId: String? = ""
    private var otherUserName: String? = ""
    private var otherFirstName: String? = ""

    private var chatBundle = Bundle()

    private lateinit var GiftbottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    var fragVirtualGifts: FragmentVirtualGifts?= null
    var fragRealGifts: FragmentRealGifts?= null
    private var userToken: String? = null
    private var LuserId: String? = null

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentUserProfileBinding.inflate(inflater, container, false).apply {
        viewModel.isMyUser = false
        this.vm = viewModel
    }
    private val addSliderImageIntent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->

        Timber.d("RESULT"+result)

    }
    override fun setupTheme() {

        lifecycleScope.launch {
            userToken = getCurrentUserToken()!!
            LuserId = getCurrentUserId()!!

            Timber.i("usertokenn $userToken")
        }

        fromChat = requireArguments().getBoolean(ARGS_FROM_CHAT)
        otherUserId = requireArguments().getString("userId")
        chatBundle.putString("otherUserId", otherUserId)
        chatBundle.putString("otherUserPhoto", "")

        viewModel.getProfile(otherUserId)


        viewModel.data.observe(this){ data ->
            Timber.d("observe: $data")
            if(data!=null)
            {
                if(data.user != null)
                {
                    otherUserName = data.user?.username
                    otherFirstName= data.user?.fullName
                    binding.sendgiftto.text = "SEND GIFT TO "+ otherFirstName!!
                    if(data.user!!.avatarPhotos!=null && data.user!!.avatarPhotos!!.size != 0)
                    {
                        binding.userImgHeader.setIndicatorVisibility(View.VISIBLE)


                        try {
                            binding.userImgHeader.setImageListener { position, imageView ->

                                if(position<= data.user!!.avatarPhotos!!.size)
                                {
                                    data.user?.avatarPhotos?.get(position)?.let {
                                        imageView.loadImage(it.url.replace("http://95.216.208.1:8000/media/","${BuildConfig.BASE_URL}media/"))
                                    }
                                }
                                imageView.setOnClickListener {
                                    if(data.user!!.avatarPhotos!! != null && data.user!!.avatarPhotos!!.size != 0) {

                                        val dataarray: ArrayList<Photo> = ArrayList()
                                        data.user!!.avatarPhotos!!.indices.forEach { i ->

                                            val photo_ = data.user!!.avatarPhotos!![i]
                                            dataarray.add(photo_)
                                        }


                                        addSliderImageIntent.launch(
                                            getimageSliderIntent(requireActivity(), Gson().toJson(dataarray),position))


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
                            binding.userImgHeader.pageCount =  data.user!!.avatarPhotos!!.size

                        } catch (e: Exception) {
                        }
                    }
                    else
                    {

                        for (f in 0 until binding.userImgHeader.pageCount) {



                            binding.userImgHeader.removeViewAt(f)
                            binding.userImgHeader.setIndicatorVisibility(View.GONE)


                        }

                    }
                    chatBundle.putString("otherUserName", data.user?.fullName)
                    chatBundle.putInt("otherUserGender", data.user!!.gender ?: 0)
                }
            }


            if(data != null)
            {
                if(data.user != null)
                {
                    if(data.user!!.avatarPhotos != null)
                    {

                        if (data.user?.avatarPhotos!!.size!=0)
                        {
                            if ((data.user?.avatarPhotos?.size!! > 0) && (data.user?.avatarIndex!! < data.user!!.avatarPhotos!!.size)) {
                                chatBundle.putString("otherUserPhoto", data?.user!!.avatarPhotos!!.get(
                                    data?.user!!.avatarIndex!!
                                ).url.replace(
                                    "http://95.216.208.1:8000/media/",
                                    "${BuildConfig.BASE_URL}media/"
                                ))

                                binding.userProfileImg.loadCircleImage(
                                    data?.user!!.avatarPhotos!!.get(
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



            }






//            binding.userImgHeader.pageCount = data?.user?.avatarPhotos?.size ?: 1
            binding.profileTabs.setupWithViewPager(binding.userDataViewPager)
            binding.userDataViewPager.adapter = viewModel.setupViewPager(childFragmentManager, data?.user, data?.defaultPicker)
            if (!checkUserIsAlreadyInChat()){
                binding.initChatMsgBtn.visibility = View.VISIBLE
            }else binding.initChatMsgBtn.visibility = View.GONE
        }
        binding.actionGifts1.visibility = View.GONE
//        binding.actionGifts.visibility = View.VISIBLE
        binding.actionCoins.visibility = View.GONE

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
        binding.sendgiftto.setOnClickListener(View.OnClickListener {

            val items: MutableList<ModelGifts.Data.AllRealGift> = mutableListOf()
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
                                userToken!!
                            ).mutation(GiftPurchaseMutation(gift.id, otherUserId!!)).execute()
                        } catch (e: ApolloException) {
                            Timber.d("apolloResponse ${e.message}")
                            Toast.makeText(requireContext(),"Exception ${e.message}", Toast.LENGTH_LONG).show()
//                                views.snackbar("Exception ${e.message}")
                            //hideProgressView()
                            //return@launchWhenResumed
                        }
                        if (res?.hasErrors() == false) {
//                                views.snackbar("You bought ${res.data?.giftPurchase?.giftPurchase?.gift?.giftName} successfully!")
                            Toast.makeText(requireContext(),"You bought ${res.data?.giftPurchase?.giftPurchase?.gift?.giftName} successfully!",
                                Toast.LENGTH_LONG).show()

                            fireGiftBuyNotificationforreceiver(gift.id,otherUserId!!)

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
                    hideProgressView()
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

    fun fireGiftBuyNotificationforreceiver(gid: String, userid: String?) {

        lifecycleScope.launchWhenResumed {


            val queryName = "sendNotification"
            val query = StringBuilder()
                .append("mutation {")
                .append("$queryName (")
                .append("userId: \"${userid}\", ")
                .append("notificationSetting: \"GIFT RLVRTL\", ")
                .append("data: {giftId:${gid}}")
                .append(") {")
                .append("sent")
                .append("}")
                .append("}")
                .toString()

            val result= AppModule.provideGraphqlApi().getResponse<Boolean>(
                query,
                queryName, userToken)
            Timber.d("RSLT",""+result.message)

        }








    }
    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = UserItemsAdapter(childFragmentManager)
        fragRealGifts = FragmentRealGifts()
        fragVirtualGifts = FragmentVirtualGifts()

        adapter.addFragItem(fragRealGifts!!, getString(R.string.real_gifts))
        adapter.addFragItem(fragVirtualGifts!!, getString(R.string.virtual_gifts))
        viewPager.adapter = adapter
    }
    override fun onDetach() {
        super.onDetach()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun showImageDialog(imageUrl: String) {
        val dialog = Dialog(requireContext(), android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
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

    private fun navToUserChat() {
        findNavController().navigate(
            destinationId = R.id.globalUserToChatAction,
            popUpFragId = R.id.searchUserProfileFragment,
            animType = null,
            inclusive = true
        )
    }

    override fun setupClickListeners() {
        /*binding.actionBack.setOnClickListener { requireActivity().onBackPressed() }*/
        with(viewModel){
            onReport = {
                showProgressView()
                reportUser(otherUserId).observe(this@SearchUserProfileFragment){
                    hideProgressView()
                    //binding.userMainContent.snackbar(it)
                }
            }
            onSendMsg = {
//                goToMessageScreen()
                createNewChat()

            }
            onGift = {
                //findNavController().navigate(R.id.action_userProfileFragment_to_userGiftsFragment)

//                var bundle = Bundle()
//                bundle.putString("userId", otherUserId)
//                Handler(Looper.getMainLooper()).postDelayed({
//                    findNavController().navigate(
//                        destinationId = R.id.action_userProfileFragment_to_userGiftsFragment,
//                        popUpFragId = null,
//                        animType = AnimationTypes.SLIDE_ANIM,
//                        inclusive = false,
//                        args = bundle
//                    )
//                }, 200)
                binding.purchaseButton.visibility = View.VISIBLE
                binding.topl.visibility = View.GONE
                if (GiftbottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                    GiftbottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
//            buttonSlideUp.text = "Slide Down";

                } else {
                    GiftbottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED;
//            buttonSlideUp.text = "Slide Up"

                }
            }
            onBackPressed = {
                onDetach()
                requireActivity().onBackPressed()
            }
        }
    }


    private fun createNewChat () {

        lifecycleScope.launchWhenCreated() {
            showProgressView()

            var res: ApolloResponse<CreateChatMutation.Data>? = null
            val idOfUserYouWantToChatWith = otherUserName
            Timber.d("apolloResponse $idOfUserYouWantToChatWith")
            try {
                res = apolloClient(
                    requireContext(),
                    getCurrentUserToken()!!
                ).mutation(CreateChatMutation(idOfUserYouWantToChatWith!!)).execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                //binding.root.snackbar("Exception ${e.message}")
                //hideProgressView()
                //return@launchWhenResumed
            }
            if (res?.hasErrors() == false) {
                //binding.root.snackbar("You bought ${res?.data?.giftPurchase?.giftPurchase?.gift?.giftName} successfully!")
                val chatroom = res.data?.createChat?.room
                Timber.d("apolloResponse success ${chatroom?.id}")

                if (chatroom!!.userId.id.equals(LuserId)) {

                    chatBundle.putString("UserName", chatroom.userId.fullName)


                    chatBundle.putString("otherUserId", chatroom.target.id)

                    chatBundle.putString("otherUserId", chatroom.target.id)
                    if (chatroom.target.avatar != null) {
                        chatBundle.putString("otherUserPhoto", chatroom.target.avatar.url ?: "")
                    } else {
                        chatBundle.putString("otherUserPhoto", "")
                    }

                    chatBundle.putString("otherUserName", chatroom.target.fullName)
                    chatBundle.putInt("otherUserGender", chatroom.target.gender ?: 0)
                    chatBundle.putString("ChatType", "Normal")

                } else {

                    chatBundle.putString("UserName", chatroom.target.fullName)


                    chatBundle.putString("otherUserId", chatroom.userId.id)
                    if (chatroom.userId.avatar != null) {
                        chatBundle.putString("otherUserPhoto", chatroom.userId.avatar.url ?: "")
                    } else {
                        chatBundle.putString("otherUserPhoto", "")
                    }
                    chatBundle.putString("otherUserName", chatroom.userId.fullName ?: "")
                    chatBundle.putInt("otherUserGender", chatroom.userId.gender ?: 0)
                    chatBundle.putString("ChatType", "Normal")

                }
                chatBundle.putInt("chatId", chatroom.id.toInt())
                findNavController().navigate(R.id.globalUserToNewChatAction, chatBundle)
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
            //Timber.d("apolloResponse ${res?.hasErrors()} ${res?.data?.createChat?.room} ${res?.data?.createChat?.room?.isNew}")
            hideProgressView()
        }
    }


    private fun goToMessageScreen() {
        if (checkUserIsAlreadyInChat()) {
            if (fromChat) {
                navToUserChat()
            } else {
                val msgPreviewModel: MessagePreviewModel? = QbDialogHolder.getChatDialogByUserId(otherUserId)
                msgPreviewModel?.let {
                    viewModel.selectedMsgPreview = it
                    findNavController().navigate(R.id.globalUserToChatAction)
                }
            }
        } else {
            findNavController().navigate(R.id.action_global_sendFirstMessengerChatFragment)
        }
    }

    private fun checkUserIsAlreadyInChat(): Boolean {
        val dialogsUsersIds = ArrayList(QbDialogHolder.dialogsMap.values).map { it.user }.map { it?.id }
        return dialogsUsersIds.contains(otherUserId)
    }

    private fun adjustScreen(){
        var height = Utils.getScreenHeight()
        var calculated = (height * 70)/100
        Timber.d("Height: $height Calculated 75%: $calculated")

        var params = binding?.userCollapseToolbar?.layoutParams
        params.height = calculated
        binding?.userCollapseToolbar?.layoutParams = params
    }
}