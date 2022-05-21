package com.i69app.ui.screens.main.messenger.chat

import android.app.Activity
import android.app.ActivityManager
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.exception.ApolloException
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.i69app.*
import com.i69app.data.models.ModelGifts
import com.i69app.databinding.AlertFullImageBinding
import com.i69app.databinding.FragmentNewMessengerChatBinding
import com.i69app.di.modules.AppModule
import com.i69app.gifts.FragmentRealGifts
import com.i69app.gifts.FragmentVirtualGifts
import com.i69app.singleton.App
import com.i69app.ui.adapters.NewChatMessagesAdapter
import com.i69app.ui.adapters.UserItemsAdapter
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.screens.ImagePickerActivity
import com.i69app.ui.screens.SplashActivity
import com.i69app.ui.screens.main.MainActivity
import com.i69app.ui.screens.main.search.userProfile.PicViewerFragment
import com.i69app.ui.screens.main.search.userProfile.SearchUserProfileFragment
import com.i69app.ui.viewModels.UserViewModel
import com.i69app.utils.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class MessengerNewChatFragment : BaseFragment<FragmentNewMessengerChatBinding>(),
    NewChatMessagesAdapter.ChatMessageListener {

    private lateinit var adapter: NewChatMessagesAdapter
    private var edges: MutableList<GetChatMessagesByRoomIdQuery.Edge?>? = mutableListOf()
    private lateinit var deferred: Deferred<Unit>
    private var userId: String? = null
    private var userToken: String? = null
    private val viewModel: UserViewModel by activityViewModels()

    var otherUserid: String? = null
    var usernames: String? = null


    var ChatType: String? = null
    var fragVirtualGifts: FragmentVirtualGifts? = null
    var fragRealGifts: FragmentRealGifts? = null
    private lateinit var GiftbottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>


    private val photosLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            val data = activityResult.data
            if (activityResult.resultCode == Activity.RESULT_OK) {
                val result = data?.getStringExtra("result")
                Timber.d("Result $result")

                if (result != null) {
                    UploadUtility(this@MessengerNewChatFragment).uploadFile(
                        result,
                        authorization = userToken
                    ) { url ->
                        Timber.d("responseurll $url")
                        var input = url
                        if (url?.startsWith("/media/chat_files/") == true) {
                            input = "${BuildConfig.BASE_URL}$url"
                        }
                        sendMessageToServer(input)
                    }
                }
            }
        }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNewMessengerChatBinding =
        FragmentNewMessengerChatBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        lifecycleScope.launch {
            userId = getCurrentUserId()!!
            userToken = getCurrentUserToken()!!
            Timber.i("usertokenn $userToken")
        }

        getTypeActivity<MainActivity>()?.setSupportActionBar(binding.toolbar)
        val supportActionBar = getTypeActivity<MainActivity>()?.supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        supportActionBar?.title = ""
        //currentUser = null
        setHasOptionsMenu(true)
        setupData()
        getuserdata()
        lifecycleScope.launch {


            viewModel.newMessageFlow.collect { message ->
                val avatarPhotos =
                    edges?.find { it?.node?.userId?.id == message?.userId?.id }?.node?.userId?.avatarPhotos
                message?.let { message ->
                    edges?.add(
                        0, GetChatMessagesByRoomIdQuery.Edge(
                            GetChatMessagesByRoomIdQuery.Node(
                                id = message.id,
                                content = message.content,
                                roomId = GetChatMessagesByRoomIdQuery.RoomId(
                                    id = message.roomId.id,
                                    name = message.roomId.name
                                ),
                                timestamp = message.timestamp,
                                userId = GetChatMessagesByRoomIdQuery.UserId(
                                    id = message.userId.id,
                                    username = message.userId.username,
                                    avatarIndex = message.userId.avatarIndex,
                                    avatarPhotos = avatarPhotos
                                ),
                            )
                        )
                    )
                    notifyAdapter(edges = edges as ArrayList<GetChatMessagesByRoomIdQuery.Edge?>?)
                }
            }
        }

    }

    override fun setupClickListeners() {

    }


    fun getuserdata()
    {

        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(requireContext(), userToken!!).query(GetUserDataQuery(userId!!)).execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                Toast.makeText(requireContext(), "Exception ${e.message}", Toast.LENGTH_LONG).show()

                return@launchWhenResumed
            }

            if(res.hasErrors())
            {
                if(res.errors!![0].message.equals("User doesn't exist"))
                {
                    Toast.makeText(requireContext(),"" + res.errors!![0].message,
                        Toast.LENGTH_LONG).show()

                    Handler().postDelayed({ nouserexist() }, 1500)


                }
                else {
                    Toast.makeText(requireContext(),"" + res.errors!![0].message,
                        Toast.LENGTH_LONG).show()
                }




            }
            if (res.hasErrors() == false) {
                val UserData = res.data?.user

                try {
                    if (UserData?.purchaseCoins != null) {
                        if(UserData.purchaseCoins == 0)
                        {
                            if (UserData.giftCoins != null) {
                                binding.coinsCounter.text = "" + UserData.giftCoins + " Coin left"
                            }
                            else {


                                binding.coinsCounter.text = "" + UserData.purchaseCoins + " Coin left"
                            }


                        }
                        else {


                            binding.coinsCounter.text = "" + UserData.purchaseCoins + " Coin left"
                        }

                    }
                } catch (e: Exception) {
                }
            }
        }
    }

    private fun setupData() {

        ChatType = arguments?.getString("ChatType")
        if(ChatType.equals("firstName"))
        {
            getFirstMessages()
            binding.inputLayout.setVisibility(View.GONE)
            binding.coinsLayout.setVisibility(View.GONE)
            binding.userName.text = requireArguments().getString("otherUserName")
            binding.userProfileImg.loadCircleImage(R.drawable.logo)

        }
        else if(ChatType.equals("broadcast"))
        {
            getBrodcastMessages()

            binding.inputLayout.setVisibility(View.GONE)
            binding.coinsLayout.setVisibility(View.GONE)
            binding.userName.text = requireArguments().getString("otherUserName")
            binding.userProfileImg.loadCircleImage(R.drawable.logo)

        }
        else if(ChatType.equals("Normal"))
        {
            getChatMessages()
            initInputListener()
            binding.inputLayout.setVisibility(View.VISIBLE)
            binding.coinsLayout.setVisibility(View.VISIBLE)
            otherUserid = arguments?.getString("otherUserId")


            usernames = arguments?.getString("UserName")
            binding.userName.text = requireArguments().getString("otherUserName")
            binding.sendgiftto.text = "SEND GIFT TO " + requireArguments().getString("otherUserName")

            val url = requireArguments().getString("otherUserPhoto")

            binding.userProfileImg.loadCircleImage(url!!)
            binding.userProfileImgContainer.setOnClickListener { navigateToOtherUserProfile() }

            isOtherUserOnline()

            binding.userProfileImg.setOnClickListener(View.OnClickListener {

                gotoChatUserProfile()
            })

            binding.userName.setOnClickListener(View.OnClickListener {
                gotoChatUserProfile()
            })
            val gender = requireArguments().getInt("otherUserGender", 0)
            binding.input.updateGiftIcon(gender)


        }

        binding.closeBtn.setOnClickListener(View.OnClickListener {
            moveUp()
        })

    }

    private fun navigateToOtherUserProfile() {

    }

    fun gotoChatUserProfile() {
        val bundle = Bundle()
        bundle.putBoolean(SearchUserProfileFragment.ARGS_FROM_CHAT, false)
        bundle.putString("userId", otherUserid)

        findNavController().navigate(
            destinationId = R.id.action_global_otherUserProfileFragment,
            popUpFragId = null,
            animType = AnimationTypes.SLIDE_ANIM,
            inclusive = true,
            args = bundle
        )
    }

    private fun initInputListener() {
        Timber.d("check input string ${arguments?.getInt("chatId")} ${arguments?.getString("otherUserId")}")
        //sendMessageToServer("hardcode text message")
        binding.input.setInputListener { input ->
            binding.input.inputEditText?.hideKeyboard()
            sendMessageToServer(input.toString())
            Timber.d(
                "check input string $input ${arguments?.getInt("chatId")} ${
                    arguments?.getString(
                        "otherUserId"
                    )
                }"
            )
            return@setInputListener false
        }
        binding.input.setAttachmentsListener {
            val intent = Intent(requireActivity(), ImagePickerActivity::class.java)
            intent.putExtra("video_duration_limit", 180)
            photosLauncher.launch(intent)
        }
        binding.input.setGiftButtonListener {


            if (GiftbottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                GiftbottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
//            buttonSlideUp.text = "Slide Down";


            } else {
                GiftbottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
//            buttonSlideUp.text = "Slide Up"


            }


//            if (arguments?.getString("otherUserId")?.isEmpty() == true) return@setGiftButtonListener
//
//            val bundle = Bundle()
//            bundle.putString("userId", arguments?.getString-("otherUserId"))
//            Handler(Looper.getMainLooper()).postDelayed({
//                findNavController().navigate(destinationId = R.id.action_to_userGiftsFragment,
//                    popUpFragId = null,
//                    animType = AnimationTypes.SLIDE_ANIM,
//                    inclusive = false,
//                    args = bundle)
//            }, 200)
        }

        GiftbottomSheetBehavior =
            BottomSheetBehavior.from<ConstraintLayout>(binding.giftbottomSheet)
        GiftbottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
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

            lifecycleScope.launchWhenCreated {
                if (items.size > 0) {
                    showProgressView()
                    items.forEach { gift ->

                        var res: ApolloResponse<GiftPurchaseMutation.Data>? = null
                        try {
                            res = apolloClient(
                                requireContext(),
                                userToken!!
                            ).mutation(GiftPurchaseMutation(gift.id, otherUserid!!)).execute()
                        } catch (e: ApolloException) {
                            Timber.d("apolloResponse ${e.message}")
                            Toast.makeText(
                                requireContext(),
                                "Exception ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
//                                views.snackbar("Exception ${e.message}")
                            //hideProgressView()
                            //return@launchWhenResumed
                        }
                        if (res?.hasErrors() == false) {
//                                views.snackbar("You bought ${res.data?.giftPurchase?.giftPurchase?.gift?.giftName} successfully!")
                            Toast.makeText(
                                requireContext(),
                                "You bought ${res.data?.giftPurchase?.giftPurchase?.gift?.giftName} successfully!",
                                Toast.LENGTH_LONG
                            ).show()

                            fireGiftBuyNotificationforreceiver(gift.id, otherUserid)

                        }
                        if (res!!.hasErrors()) {
                            if(res.errors!![0].message.equals("User doesn't exist"))
                            {
//                                binding.root.snackbar("" + res.errors!![0].message)

                                Toast.makeText(
                                    requireContext(),
                                    "" + res.errors!![0].message,
                                    Toast.LENGTH_LONG
                                ).show()

                               Handler().postDelayed({ nouserexist() }, 1500)
                            }
                            else {
//                                binding.root.snackbar("" + res.errors!![0].message)

                                Toast.makeText(
                                    requireContext(),
                                    "" + res.errors!![0].message,
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        }
                        Timber.d("apolloResponse ${res.hasErrors()} ${res.data?.giftPurchase?.giftPurchase?.gift?.giftName}")
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
    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = UserItemsAdapter(childFragmentManager)
        fragRealGifts = FragmentRealGifts()
        fragVirtualGifts = FragmentVirtualGifts()

        adapter.addFragItem(fragRealGifts!!, getString(R.string.real_gifts))
        adapter.addFragItem(fragVirtualGifts!!, getString(R.string.virtual_gifts))
        viewPager.adapter = adapter
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

            val result = AppModule.provideGraphqlApi().getResponse<Boolean>(
                query,
                queryName, userToken
            )
            Timber.d("RSLT", "" + result.message)

        }
    }

    private fun notifyAdapter(edges: ArrayList<GetChatMessagesByRoomIdQuery.Edge?>?) {
        //if (binding.rvChatMessages.adapter == null) {
        adapter =
            NewChatMessagesAdapter(
                requireActivity(),
                userId,
                this@MessengerNewChatFragment,
                edges
            )
        (binding.rvChatMessages.layoutManager as LinearLayoutManager).apply {
            reverseLayout = true
            stackFromEnd = true
            binding.rvChatMessages.layoutManager = this
        }
        binding.rvChatMessages.adapter = adapter
        /*} else {
            adapter.updateList(edges)
        }*/
        if (adapter.itemCount > 0) {
            binding.rvChatMessages.layoutManager?.scrollToPosition(0)
        }
    }

    private fun isOtherUserOnline() {

        showProgressView()
        lifecycleScope.launch {
            try {
                val id = requireArguments().getString("otherUserId")
                val res =
                    apolloClient(requireContext(), userToken!!).query(IsOnlineQuery(id!!)).execute()
                if (!res.hasErrors()) {
                    binding.otherUserOnlineStatus.visibility =
                        if (res.data?.isOnline?.isOnline == true) View.VISIBLE else View.GONE
                    Timber.d("apolloResponse isOnline ${res.data?.isOnline?.isOnline}")
                }
                if (res.hasErrors()) {

                    if(res.errors!![0].message.equals("User doesn't exist"))
                    {
//                        binding.root.snackbar("" + res.errors!![0].message)

                        Toast.makeText(
                            requireContext(),
                            "" + res.errors!![0].message,
                            Toast.LENGTH_LONG
                        ).show()

                        Handler().postDelayed({ nouserexist() }, 1500)


                    }
                    else {
//                        binding.root.snackbar("" + res.errors!![0].message)

                        Toast.makeText(
                            requireContext(),
                            "" + res.errors!![0].message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }

            } catch (e: ApolloException) {
                Timber.d("apolloResponse isOnline ${e.message}")
            }
            hideProgressView()
        }
    }
    private fun getFirstMessages() {
        edges = mutableListOf()
        showProgressView()
        lifecycleScope.launch {
            try {
                val res = apolloClient(requireContext(), userToken!!).query(
                    GetFirstMessageListQuery()
                ).execute()


                if (res.hasErrors()) {

                    if(res.errors!![0].message.equals("User doesn't exist"))
                    {
//                        binding.root.snackbar("" + res.errors!![0].message)

                        Toast.makeText(
                            requireContext(),
                            "" + res.errors!![0].message,
                            Toast.LENGTH_LONG
                        ).show()


                        Handler().postDelayed({ nouserexist() }, 1500)


                    }
                    else {
//                        binding.root.snackbar("" + res.errors!![0].message)
                        Toast.makeText(
                            requireContext(),
                            "" + res.errors!![0].message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }

                if (res.hasErrors() == false)
                {
                    val datas = res.data!!.firstmessageMsgs!!.edges

                    datas.forEach { Edge ->

                        val msg=  GetChatMessagesByRoomIdQuery.Edge(
                            GetChatMessagesByRoomIdQuery.Node(
                                id = Edge!!.node!!.byUserId.id!!,
                                content = Edge.node!!.content,
                                roomId = GetChatMessagesByRoomIdQuery.RoomId(
                                    id = "",
                                    name = ""
                                ),
                                timestamp = Edge.node.timestamp,
                                userId = GetChatMessagesByRoomIdQuery.UserId(
                                    id = Edge.node.byUserId.id,
                                    username = Edge.node.byUserId.username,
                                    avatarIndex = Edge.node.byUserId.avatarIndex,
                                    null
                                ),
                            )
                        )
                        edges!!.add(msg)
                    }

                    if (!res.hasErrors()) {
                        Timber.d("apolloResponse success ${edges?.size}")
                        notifyAdapter(edges as ArrayList<GetChatMessagesByRoomIdQuery.Edge?>?)
                    } else {
                        Timber.d("apolloResponse error ${res.errors?.get(0)?.message}")
                    }
                }


            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                //binding.root.snackbar("Exception all moments $GetAllMomentsQuery")
                return@launch
            }
            hideProgressView()
        }
    }
    private fun getBrodcastMessages() {
        edges = mutableListOf()
        showProgressView()
        lifecycleScope.launch {
            try {
                val res = apolloClient(requireContext(), userToken!!).query(
                    GetBroadcastMessageListQuery()
                ).execute()


                if (res.hasErrors()) {

                    if(res.errors!![0].message.equals("User doesn't exist"))
                    {
//                        binding.root.snackbar("" + res.errors!![0].message)

                        Toast.makeText(
                            requireContext(),
                            "" + res.errors!![0].message,
                            Toast.LENGTH_LONG
                        ).show()


                        Handler().postDelayed({ nouserexist() }, 1500)


                    }
                    else {
//                        binding.root.snackbar("" + res.errors!![0].message)
                        Toast.makeText(
                            requireContext(),
                            "" + res.errors!![0].message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
                if (res.hasErrors() == false) {
                    val datas = res.data!!.broadcastMsgs!!.edges

                    datas.forEach { Edge ->

                        val msg = GetChatMessagesByRoomIdQuery.Edge(
                            GetChatMessagesByRoomIdQuery.Node(
                                id = Edge!!.node!!.byUserId.id!!,
                                content = Edge.node!!.content,
                                roomId = GetChatMessagesByRoomIdQuery.RoomId(
                                    id = "",
                                    name = ""
                                ),
                                timestamp = Edge.node.timestamp,
                                userId = GetChatMessagesByRoomIdQuery.UserId(
                                    id = Edge.node.byUserId.id,
                                    username = Edge.node.byUserId.username,
                                    avatarIndex = Edge.node.byUserId.avatarIndex,
                                    null
                                ),
                            )
                        )
                        edges!!.add(msg)
                    }


                    /*edges?.forEach {
                    Timber.d("apolloResponse getChatMessages ${it?.node?.text}")
                }*/
                    if (!res.hasErrors()) {
                        Timber.d("apolloResponse success ${edges?.size}")
                        notifyAdapter(edges as ArrayList<GetChatMessagesByRoomIdQuery.Edge?>?)
                    } else {
                        Timber.d("apolloResponse error ${res.errors?.get(0)?.message}")
                    }
                }
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                //binding.root.snackbar("Exception all moments $GetAllMomentsQuery")
                return@launch
            }
            hideProgressView()
        }
    }

    private fun getChatMessages() {
        showProgressView()
        lifecycleScope.launch {
            try {
                val roomId = arguments?.getInt("chatId")
                Timber.d("apolloResponse roomId $roomId")
                val res = apolloClient(requireContext(), userToken!!).query(
                    GetChatMessagesByRoomIdQuery(
                        roomId.toString(),
                        99
                    )
                ).execute()

                if (res.hasErrors()) {

                    if(res.errors!![0].message.equals("User doesn't exist"))
                    {
                        Toast.makeText(
                            requireContext(),
                            "" + res.errors!![0].message,
                            Toast.LENGTH_LONG
                        ).show()

                        Handler().postDelayed({ nouserexist() }, 1500)


                    }
                    else {
//                        binding.root.snackbar("" + res.errors!![0].message)
                        Toast.makeText(
                            requireContext(),
                            "" + res.errors!![0].message,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }

                if (res.hasErrors() == false) {
                    edges = res.data?.messages?.edges?.toMutableList()


                        Timber.d("apolloResponse success ${edges?.size}")
                        notifyAdapter(edges as ArrayList<GetChatMessagesByRoomIdQuery.Edge?>?)

                }
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                //binding.root.snackbar("Exception all moments $GetAllMomentsQuery")
                return@launch
            }
            hideProgressView()
        }
    }

    private fun sendMessageToServer(input: String?) {

        Timber.d("apolloResponse 1 $input")
        lifecycleScope.launch {
            send(input!!)
        }
        Timber.d("apolloResponse 8")
    }

    suspend fun send(input: String) {
        val chatId = arguments?.getInt("chatId", 0)!!
        Timber.d("apolloResponse 3 c $chatId and crrent id $userId")

        var res: ApolloResponse<SendChatMessageMutation.Data>? = null
        try {
            res = apolloClient(requireActivity(), userToken!!).mutation(SendChatMessageMutation(input, chatId)).execute()
            Timber.d("apolloResponse 1111 c ${res.hasErrors()} ${res.data?.sendMessage?.message}")
        } catch (e: ApolloException) {
            Timber.d("apolloResponse ${e.message}")
        } catch (ex: Exception) {
            Timber.d("General exception ${ex.message}")
        }

        if (res!!.hasErrors()) {

            if(res.errors!![0].message.equals("User doesn't exist"))
            {
//                binding.root.snackbar("" + res.errors!![0].message)
                Toast.makeText(
                    requireContext(),
                    "" + res.errors!![0].message,
                    Toast.LENGTH_LONG
                ).show()


                Handler().postDelayed({ nouserexist() }, 1500)


            }
            else {
                Toast.makeText(
                    requireContext(),
                    "" + res.errors!![0].message,
                    Toast.LENGTH_LONG
                ).show()

            }

        }

        hideProgressView()
        if (res.hasErrors() == false) {
            getuserdata()
            fireChatNotificationforreceiver(chatId,otherUserid)
            getChatMessages()
            Timber.d("apolloResponse ${res.hasErrors()} ${res.data?.sendMessage?.message}")
        }

        binding.input.inputEditText.text = null
    }


    fun fireChatNotificationforreceiver(chatId: Int?, userid: String?) {

        lifecycleScope.launchWhenResumed {

            val queryName = "sendNotification"
            val query = StringBuilder()
                .append("mutation {")
                .append("$queryName (")
                .append("userId: \"${userid}\", ")
                .append("notificationSetting: \"SNDMSG\", ")
                .append("data: {id:${chatId}}")
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

    //flow.collect {
    //Timber.d("reealltime ${it.data?.chatSubscription?.message?.text}")
    //val adapter = binding.rvChatMessages.adapter as NewChatMessagesAdapter
    //adapter.addMessage(it.data?.chatSubscription?.message)
    //}

    private suspend fun cancelChatRoom() {
        Timber.d("reealltime detached 3")
        deferred.cancel()
        Timber.d("reealltime detached 4")
        try {
            val result = deferred.await()
            Timber.d("reealltime detached 5")
        } catch (e: CancellationException) {
            Timber.d("reealltime cancel room exception ${e.message}")
            // handle CancellationException if need
        } finally {
            // make some cleanup if need
            Timber.d("reealltime detached 6")
        }
    }

    override fun onChatMessageClick(position: Int, message: GetChatMessagesByRoomIdQuery.Edge?) {

        val url = message?.node?.content
        if (url?.contains("media/chat_files") == true) {
            var fullUrl = url
            if (url.startsWith("/media/chat_files/")) {
                fullUrl = "${BuildConfig.BASE_URL}$url"
            }
            val uri = Uri.parse(fullUrl)
            val lastSegment = uri.lastPathSegment
            val ext = lastSegment?.substring(lastSegment.lastIndexOf(".") + 1)
            Timber.d("clickk $lastSegment $ext")
            if (ext.contentEquals("jpg") || ext.contentEquals("png") || ext.contentEquals("jpeg")) {
//                val w = resources.displayMetrics.widthPixels
//                val h = resources.displayMetrics.heightPixels
//                showImageDialog(w, h, fullUrl)
                val dialog = PicViewerFragment()
                val b = Bundle()
                b.putString("url", fullUrl)
                b.putString("mediatype", "image")

                dialog.arguments = b
                dialog.show(childFragmentManager, "ChatpicViewer")
            } else {
                val dialog = PicViewerFragment()
                val b = Bundle()
                b.putString("url", fullUrl)
                b.putString("mediatype", "video")

                dialog.arguments = b
                dialog.show(childFragmentManager, "ChatvideoViewer")
//                val downloadedFile = File(
//                    requireActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
//                    lastSegment!!
//                )
//                if (downloadedFile.exists()) {
//                    downloadedFile.openFile(requireActivity())
//                } else {
//                    DownloadUtil(this@MessengerNewChatFragment).downloadFile(
//                        fullUrl,
//                        lastSegment
//                    ) { downloadedFile ->
//                        Timber.d("downnfile ${downloadedFile?.exists()} ${downloadedFile?.absolutePath}")
//                        if (downloadedFile?.exists() == true) {
//                            downloadedFile.openFile(requireActivity())
//                        }
//                    }
//                }
            }
        }
    }

    override fun onChatUserAvtarClick() {
        gotoChatUserProfile()
    }

    private fun showImageDialog(width: Int, height: Int, imageUrl: String?) {
        val dialog = Dialog(requireContext())
        val dialogBinding = AlertFullImageBinding.inflate(layoutInflater, null, false)
        dialogBinding.fullImg.loadImage(imageUrl!!, {
            dialogBinding.alertTitle.setViewGone()
        }, {
            dialogBinding.alertTitle.text = it?.message
        })
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setLayout(width, height)
        dialog.show()
        dialogBinding.root.setOnClickListener {
            dialog.cancel()
        }
    }
}