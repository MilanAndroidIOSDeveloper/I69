package com.i69app.ui.screens.main.messenger.list

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import com.apollographql.apollo3.exception.ApolloException
import com.i69app.*
import com.i69app.databinding.FragmentMessengerListBinding
import com.i69app.databinding.ItemRequestPreviewLongBinding
import com.i69app.singleton.App
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.screens.SplashActivity
import com.i69app.ui.screens.main.MainActivity
import com.i69app.ui.screens.main.messenger.list.MessengerListAdapter.MessagesListListener
import com.i69app.ui.viewModels.UserViewModel
import com.i69app.utils.*
import com.quickblox.chat.query.QueryDeleteMessages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class MessengerListFragment : BaseFragment<FragmentMessengerListBinding>(), MessagesListListener {

    private lateinit var job: Job
    private var firstMessage: GetAllRoomsQuery.Edge? = null
    private var broadcastMessage: GetAllRoomsQuery.Edge? = null
    private var allRoom: MutableList<GetAllRoomsQuery.Edge?> = mutableListOf()
    private var isRunning = false
    private val viewModel: UserViewModel by activityViewModels()
    private lateinit var messengerListAdapter: MessengerListAdapter
    var endCursor: String = ""
    var hasNextPage: Boolean = false

    private var userId: String? = null
    private var userToken: String? = null
    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentMessengerListBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        navController = findNavController()

        lifecycleScope.launch {
            userId = getCurrentUserId()!!
            userToken = getCurrentUserToken()!!
            Timber.i("usertokenn $userToken")
        }
        Timber.i("usertokenn 2 $userToken")


        binding.messengerList.addItemDecoration(DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL))


        messengerListAdapter = MessengerListAdapter(this@MessengerListFragment, userId)
        binding.messengerList.adapter = messengerListAdapter

        getTypeActivity<MainActivity>()?.enableNavigationDrawer()
        //getFirstMessage()
        //getBroadcastMessage()
        updateList()

        isRunning = false

        lifecycleScope.launch {
            viewModel.shouldUpdateAdapter.collect {
                Timber.tag(MainActivity.CHAT_TAG).i("Collecting Data: Update ($it)")
                if (it) updateList()
            }
        }

        job = lifecycleScope.launch {
            viewModel.newMessageFlow.collect { message ->
                message?.let { newMessage ->
                    val index = allRoom.indexOfFirst {
                        it?.node?.id == newMessage.roomId.id
                    }
                    val selectedRoom = allRoom[index]!!
                    val room = GetAllRoomsQuery.Edge(
                        GetAllRoomsQuery.Node(
                            id = selectedRoom.node?.id!!,
                            name = selectedRoom.node.name,
                            lastModified = newMessage.timestamp,
                            unread = selectedRoom.node.unread?.toInt()?.plus(1)?.toString(),
                            messageSet = GetAllRoomsQuery.MessageSet(
                                edges = selectedRoom.node.messageSet.edges.toMutableList()
                                    .apply {
                                        set(
                                            0, GetAllRoomsQuery.Edge1(
                                                GetAllRoomsQuery.Node1(
                                                    content = newMessage.content,
                                                    id = newMessage.id,
                                                    roomId = GetAllRoomsQuery.RoomId(id = newMessage.roomId.id),
                                                    timestamp = newMessage.timestamp,
                                                    read = newMessage.read
                                                )
                                            )
                                        )
                                    }
                            ),
                            userId = selectedRoom.node.userId,
                            target = selectedRoom.node.target,
                        )
                    )
                    allRoom.set(index = index, room)
                    messengerListAdapter.updateList(allRoom)
                }
            }
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val arguments = arguments
        if (arguments != null) {


            val  roomID = arguments.get("roomIDNotify") as String?
            if(roomID!=null)
            {
                getParticularRoom(roomID)
                arguments.run {
                    remove("roomIDNotify")
                    clear()
                }
            }

        }
        return super.onCreateView(inflater, container, savedInstanceState)

    }


    override fun onPause() {
        job.cancel()
        super.onPause()
    }

    override fun setupClickListeners() {
        binding.toolbarHamburger.setOnClickListener { (activity as MainActivity).drawerSwitchState() }
        binding.goToSearchBtn.setOnClickListener { activity?.onBackPressed() }
    }

    private fun makePreviewAnimation() {
        binding.goToSearchBtn.setViewVisible()
        binding.messengerListPreview.setViewVisible()
        binding.messengerList.setVisibleOrInvisible(false)
        binding.messengerListPreview.setViewVisible()
        val display = requireActivity().windowManager!!.defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        binding.subTitle.defaultAnimate(100, 200)
        setupPreviewItem(binding.firstAnimPreview, R.drawable.icon_boy)
        setupPreviewItem(binding.secondAnimPreview, R.drawable.icon_girl)
        setupPreviewItem(binding.thirdAnimPreview, R.drawable.icon_girl_2)

        binding.firstAnimPreview.root.animateFromLeft(200, 300, metrics.widthPixels / 3)
        binding.secondAnimPreview.root.animateFromLeft(200, 500, metrics.widthPixels / 3)
        binding.thirdAnimPreview.root.animateFromLeft(200, 700, metrics.widthPixels / 3)
    }

    private fun setupPreviewItem(
        requestPreviewBinding: ItemRequestPreviewLongBinding,
        preview: Int
    ) {
        requestPreviewBinding.previewImg.setImageResource(preview)
    }

    private fun getParticularRoom(roomID: String?)
    {
        Timber.d("ROOMID="+roomID)
        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(requireContext(), userToken!!).query(GetparticularRoomsQuery(roomID!!))
                    .execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                binding.root.snackbar("Exception to get room ${e.message}")
                hideProgressView()
                return@launchWhenResumed
            }

            hideProgressView()

            if (res.hasErrors()) {

                if(res.errors!![0].message.equals("User doesn't exist"))
                {
                    binding.root.snackbar("" + res.errors!![0].message)

                    Handler().postDelayed({ nouserexist() }, 1500)


                }
                else {
                    binding.root.snackbar("" + res.errors!![0].message)
                }

            }
            if (res.hasErrors() == false) {
                val Rooms = res.data?.room

                val chatBundle = Bundle()



                if (Rooms!!.userId.id.equals(userId)) {
                    chatBundle.putString("otherUserId", Rooms.target.id)

                    chatBundle.putString("UserName", Rooms.userId.fullName)



                    if (Rooms.target.avatar != null) {
                        chatBundle.putString("otherUserPhoto", Rooms.target.avatar.url ?: "")
                    } else {
                        chatBundle.putString("otherUserPhoto", "")
                    }

                    chatBundle.putString("otherUserName", Rooms.target.fullName)
                    chatBundle.putInt("otherUserGender", Rooms.target.gender ?: 0)
                    chatBundle.putString("ChatType", "Normal")

                } else {
                    chatBundle.putString("otherUserId", Rooms.userId.id)

                    chatBundle.putString("UserName", Rooms.target.fullName)


                    if (Rooms.userId.avatar != null) {
                        chatBundle.putString("otherUserPhoto", Rooms.userId.avatar.url ?: "")
                    } else {
                        chatBundle.putString("otherUserPhoto", "")
                    }
                    chatBundle.putString("otherUserName", Rooms.userId.fullName ?: "")
                    chatBundle.putInt("otherUserGender", Rooms.userId.gender ?: 0)
                    chatBundle.putString("ChatType", "Normal")

                }

                chatBundle.putInt("chatId", Rooms.id.toInt())
                findNavController().navigate(R.id.globalUserToNewChatAction, chatBundle)


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


    private fun updateList() {

        showProgressView()
        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(requireContext(), userToken!!).query(GetAllRoomsQuery(99))
                    .execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                binding.root.snackbar("Exception all moments ${e.message}")
                hideProgressView()
                return@launchWhenResumed
            }
            if (res.hasErrors()) {

                if (res.errors!![0].message.equals("User doesn't exist")) {
                    binding.root.snackbar("" + res.errors!![0].message)

                    Handler().postDelayed({ nouserexist() }, 1500)


                } else {
                    binding.root.snackbar("" + res.errors!![0].message)
                }

            }

            if (res.hasErrors() == false) {
                val resFirstMessage = try {
                    apolloClient(requireContext(), userToken!!).query(GetFirstMessageQuery())
                        .execute()
                } catch (e: ApolloException) {
                    Timber.d("apolloResponse ${e.message}")
                    binding.root.snackbar("Exception getFirstMessage ${e.message}")
                    hideProgressView()
                    return@launchWhenResumed
                }



                if (resFirstMessage.data?.firstmessage != null) {
                    firstMessage = GetAllRoomsQuery.Edge(GetAllRoomsQuery.Node(
                        id = "firstName",
                        name = resFirstMessage.data?.firstmessage?.firstmessageContent!!,
                        lastModified = resFirstMessage.data?.firstmessage?.firstmessageTimestamp,
                        unread = resFirstMessage.data?.firstmessage?.unread?.toString(),
                        messageSet = GetAllRoomsQuery.MessageSet(
                            edges = mutableListOf<GetAllRoomsQuery.Edge1>().apply {
                                add(
                                    GetAllRoomsQuery.Edge1(
                                        GetAllRoomsQuery.Node1(
                                            content = "",
                                            id = "firstName",
                                            roomId = GetAllRoomsQuery.RoomId(id = ""),
                                            timestamp = resFirstMessage.data?.firstmessage?.firstmessageTimestamp!!,
                                            read = ""
                                        )
                                    )
                                )
                            }
                        ),
                        userId = GetAllRoomsQuery.UserId(
                            null,
                            resFirstMessage.data?.firstmessage?.firstmessageContent!!,
                            null,
                            null,
                            null,
                            null
                        ),
                        target = GetAllRoomsQuery.Target(null, null, null, null, null, null),
                    ))
                }
                val resBroadcast = try {
                    apolloClient(requireContext(), userToken!!).query(GetBroadcastMessageQuery())
                        .execute()
                } catch (e: ApolloException) {
                    Timber.d("apolloResponse ${e.message}")
                    binding.root.snackbar("Exception getBroadcastMessage ${e.message}")
                    hideProgressView()
                    return@launchWhenResumed
                }
                if (resBroadcast.data?.broadcast != null) {
                    broadcastMessage = GetAllRoomsQuery.Edge(GetAllRoomsQuery.Node(
                        id = "broadcast",
                        name = resBroadcast.data?.broadcast?.broadcastContent!!,
                        lastModified = resBroadcast.data?.broadcast?.broadcastTimestamp,
                        unread = resBroadcast.data?.broadcast?.unread,
                        messageSet = GetAllRoomsQuery.MessageSet(
                            edges = mutableListOf<GetAllRoomsQuery.Edge1>().apply {
                                add(
                                    GetAllRoomsQuery.Edge1(
                                        GetAllRoomsQuery.Node1(
                                            content = "",
                                            id = "broadcast",
                                            roomId = GetAllRoomsQuery.RoomId(id = ""),
                                            timestamp = resBroadcast.data?.broadcast?.broadcastTimestamp!!,
                                            read = ""
                                        )
                                    )
                                )
                            }
                        ),
                        userId = GetAllRoomsQuery.UserId(
                            null,
                            "Team i69",
                            null,
                            null,
                            null,
                            null
                        ),
                        target = GetAllRoomsQuery.Target(null, null, null, null, null, null),
                    ))
                }



                allRoom = res.data?.rooms!!.edges.toMutableList()


                var totoalunread = 0
                allRoom.indices.forEach { i ->


                    val data = allRoom[i]
                    if (totoalunread == 0) {
                        totoalunread = data!!.node!!.unread!!.toInt()
                    } else {
                        totoalunread = totoalunread + data!!.node!!.unread!!.toInt()

                    }


                }

                try {
                    getMainActivity().binding.navView.updateMessagesCount(totoalunread)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (firstMessage != null) {
                    allRoom.add(firstMessage)
                }
                if (broadcastMessage != null) {
                    allRoom.add(0, broadcastMessage)
                }
                messengerListAdapter.updateList(allRoom)
                if (allRoom.size != 0) {

                    if (res.data!!.rooms!!.pageInfo.endCursor != null) {
                        endCursor = res.data!!.rooms!!.pageInfo.endCursor!!

                    }

                    hasNextPage = res.data?.rooms!!.pageInfo.hasNextPage
                }

//
                val itemTouchHelper = ItemTouchHelper(object : SwipeHelper(binding.messengerList) {

                    override fun instantiateUnderlayButton(position: Int): List<UnderlayButton> {


                        var buttons = listOf<UnderlayButton>()
                        val deleteButton = deleteButton(position)

                        if (position != allRoom.size - 1) {
                            when (position) {

                                position -> buttons = listOf(deleteButton)

                                else -> Unit
                            }

                        }

                        return buttons

                    }
                })

                itemTouchHelper.attachToRecyclerView(binding.messengerList)

                if (allRoom.isNullOrEmpty()) {
                    if (isRunning)
                        return@launchWhenResumed
                    isRunning = true
                    makePreviewAnimation()
                    return@launchWhenResumed
                }


                if (allRoom.size > 0) {
                    Timber.d("apolloResponse: ${allRoom.get(0)?.node!!.name}")
                    Timber.d("apolloResponse: ${allRoom.get(0)?.node!!.id}")
                    Timber.d("apolloResponse: ${allRoom.get(0)?.node!!.lastModified}")
                    Timber.d("apolloResponse: ${allRoom.get(0)?.node!!.target.username}")
                }
            }
            hideProgressView()
        }
    }
    private fun deleteButton(position: Int) : SwipeHelper.UnderlayButton {
        return SwipeHelper.UnderlayButton(
            requireContext(),
            "Delete",
            15.0f,
            R.color.colorPrimary,
            object : SwipeHelper.UnderlayButtonClickListener {
                override fun onClick() {

                    if (allRoom.size != 0 && position < allRoom.size - 1) {
                        toast("Deleted item $position")

                        if(allRoom.get(position)!!.node!!.id.equals("broadcast"))
                        {
                            showProgressView()

                            lifecycleScope.launchWhenResumed {
                                val res = try {
                                    apolloClient(requireContext(), userToken!!).mutation(
                                        DeleteBroadcastFromRoomListMutation()
                                    )
                                        .execute()
                                } catch (e: ApolloException) {
                                    Timber.d("apolloResponse ${e.message}")
                                    binding.root.snackbar("Exception all moments ${e.message}")
                                    hideProgressView()
                                    return@launchWhenResumed
                                }

                                if (res.hasErrors()) {

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


                                if (res.hasErrors() == false) {

                                    val success = res.data!!.deleteBroadcast!!.success
                                    if (success!!) {
                                        updateList()

                                    }
                                    binding.root.snackbar("" + res.data!!.deleteBroadcast!!.message)
                                }

                        }


                        }
                        else
                        {
                            showProgressView()

                            lifecycleScope.launchWhenResumed {
                                val roomid=allRoom.get(position)!!.node!!.id.toInt()
                                val res = try {
                                    apolloClient(requireContext(), userToken!!).mutation(
                                        DeleteMessagesFromRoomListMutation(roomid)
                                    )
                                        .execute()
                                } catch (e: ApolloException) {
                                    Timber.d("apolloResponse ${e.message}")
                                    binding.root.snackbar("Exception all moments ${e.message}")
                                    hideProgressView()
                                    return@launchWhenResumed
                                }
                                if (res.hasErrors()) {

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
                                if (res.hasErrors() == false) {
                                    val success = res.data!!.deleteMessages!!.success
                                    if (success!!) {
                                        updateList()

                                    }
                                    binding.root.snackbar("" + res.data!!.deleteMessages!!.message)
                                }
                            }

                        }
                    }
                }    })
    }

    override fun onItemClick(AllroomEdge: GetAllRoomsQuery.Edge) {
        viewModel.setSelectedMessagePreview(AllroomEdge)
        val chatBundle = Bundle()

        if (AllroomEdge.node?.id == "firstName") {
            chatBundle.putString("otherUserId", "")
            chatBundle.putString("otherUserPhoto", "")
            chatBundle.putString("otherUserName", AllroomEdge.node.userId.fullName)
            chatBundle.putInt("otherUserGender", 0)
            chatBundle.putString("ChatType", "firstName")
            chatBundle.putInt("chatId", 0)
            findNavController().navigate(R.id.globalUserToNewChatAction, chatBundle)
        }
        else if(AllroomEdge.node?.id == "broadcast")
        {
            chatBundle.putString("otherUserId", "")
            chatBundle.putString("otherUserPhoto", "")
            chatBundle.putString("otherUserName", AllroomEdge.node.userId.fullName)
            chatBundle.putInt("otherUserGender", 0)
            chatBundle.putString("ChatType", "broadcast")
            chatBundle.putInt("chatId", 0)
            findNavController().navigate(R.id.globalUserToNewChatAction, chatBundle)
        }
        else {

            if (AllroomEdge.node!!.userId.id.equals(userId)) {

                chatBundle.putString("UserName", AllroomEdge.node.userId.fullName)


                chatBundle.putString("otherUserId", AllroomEdge.node.target.id)
                if (AllroomEdge.node.target.avatar != null) {
                    chatBundle.putString("otherUserPhoto", AllroomEdge.node.target.avatar.url ?: "")
                } else {
                    chatBundle.putString("otherUserPhoto", "")
                }

                chatBundle.putString("otherUserName", AllroomEdge.node.target.fullName)
                chatBundle.putInt("otherUserGender", AllroomEdge.node.target.gender ?: 0)
                chatBundle.putString("ChatType", "Normal")

            } else {

                chatBundle.putString("UserName", AllroomEdge.node.target.fullName)


                chatBundle.putString("otherUserId", AllroomEdge.node.userId.id)
                if (AllroomEdge.node.userId.avatar != null) {
                    chatBundle.putString("otherUserPhoto", AllroomEdge.node.userId.avatar.url ?: "")
                } else {
                    chatBundle.putString("otherUserPhoto", "")
                }
                chatBundle.putString("otherUserName", AllroomEdge.node.userId.fullName ?: "")
                chatBundle.putInt("otherUserGender", AllroomEdge.node.userId.gender ?: 0)
                chatBundle.putString("ChatType", "Normal")

            }

            chatBundle.putInt("chatId", AllroomEdge.node.id.toInt())
            findNavController().navigate(R.id.globalUserToNewChatAction, chatBundle)
        }
    }

    fun getMainActivity() = activity as MainActivity

}