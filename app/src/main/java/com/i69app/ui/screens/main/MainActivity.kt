package com.i69app.ui.screens.main

import android.Manifest
import android.app.*
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.content.Context
import android.content.Intent
import android.location.Location
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.app.NotificationManagerCompat
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.apollographql.apollo3.exception.ApolloException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.i69app.*
import com.i69app.data.models.MessagePreviewModel
import com.i69app.data.models.User
import com.i69app.data.remote.repository.UserDetailsRepository
import com.i69app.data.remote.repository.UserUpdateRepository
import com.i69app.databinding.ActivityMainBinding
import com.i69app.profile.db.dao.UserDao
import com.i69app.singleton.App
import com.i69app.ui.base.BaseActivity
import com.i69app.ui.screens.PrivacyOrTermsConditionsActivity
import com.i69app.ui.screens.SplashActivity
import com.i69app.ui.screens.main.messenger.listeners.ChatConnectionListener
import com.i69app.ui.screens.main.messenger.listeners.QbChatDialogMessageListenerImpl
import com.i69app.ui.viewModels.UserViewModel
import com.i69app.utils.*
import com.i69app.utils.qb.ChatHelper
import com.i69app.utils.qb.QbDialogHolder
import com.i69app.utils.qb.dialog.DialogsManager
import com.i69app.utils.qb.onError
import com.quickblox.chat.QBChatService
import com.quickblox.chat.QBIncomingMessagesManager
import com.quickblox.chat.QBSystemMessagesManager
import com.quickblox.chat.exception.QBChatException
import com.quickblox.chat.listeners.QBChatDialogMessageListener
import com.quickblox.chat.listeners.QBSystemMessageListener
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.users.model.QBUser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.retryWhen
import org.jivesoftware.smack.ConnectionListener
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random


@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val mViewModel: UserViewModel by viewModels()
    private lateinit var navController: NavController

    @Inject
    lateinit var userDetailsRepository: UserDetailsRepository

    @Inject
    lateinit var userUpdateRepository: UserUpdateRepository

    @Inject
    lateinit var userDao: UserDao

    private var mUser: User? = null
    private var userId: String? = null
    private var userToken: String? = null
    private var chatUserId: Int = 0
    private var qbCurrentUser: QBUser? = null
    private var chatConnectionListener: ConnectionListener? = null

    private var systemMessagesManager: QBSystemMessagesManager? = null
    private var incomingMessagesManager: QBIncomingMessagesManager? = null

    var systemMessagesListener: SystemMessagesListener = SystemMessagesListener()
    private var allDialogsMessageListener: QBChatDialogMessageListener = AllDialogsMessageListener()
    private var dialogMessageListener: DialogMangerListener = DialogMangerListener()

    private lateinit var job: Job
    private val viewModel: UserViewModel by viewModels()

    private val permissionReqLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
            run {
                val granted = permission.entries.all {
                    it.value == true
                }
                if (granted) {
                    val locationService =
                        LocationServices.getFusedLocationProviderClient(this@MainActivity)
                    locationService.lastLocation.addOnSuccessListener { location: Location? ->
                        val lat: Double? = location?.latitude

                        val lon: Double? = location?.longitude
//                toast("lat = $lat lng = $lon")
                        if (lat != null && lon != null) {
                            // Update Location
                            lifecycleScope.launch(Dispatchers.Main) {
                                mViewModel.updateLocation(
                                    userId = userId!!,
                                    location = arrayOf(lat, lon),
                                    token = userToken!!
                                )
                            }
                        }
                    }
                }
            }
        }

    override fun getActivityBinding(inflater: LayoutInflater) =
        ActivityMainBinding.inflate(inflater)

    override fun setupTheme() {
        navController = Navigation.findNavController(this, R.id.main_host)
        setViewModel(mViewModel, binding)

        notificationOpened = true
        updateNavItem(R.drawable.ic_default_user)
        updateFirebaseToken(userUpdateRepository)

        initConnectionListener()

        lifecycleScope.launch(Dispatchers.Main) {
            userId = getCurrentUserId()!!
            userToken = getCurrentUserToken()!!
            Timber.d("UserId $userId!!")
            Timber.d("UserId1 $userToken!!")





                mViewModel.getCurrentUser(userId!!, token = userToken!!, true)
                    .observe(this@MainActivity) { user ->
                        Timber.d("User $user")
                        user?.let {
                            if (mUser == null) {
                                mUser = it
                                mUser!!.id = "$userId"
                                if(mUser?.avatarPhotos?.size != 0)
                                {
                                    updateNavItem(
                                        mUser?.avatarPhotos?.get(mUser!!.avatarIndex!!)?.url?.replace(
                                            "http://95.216.208.1:8000/media/",
                                            "${BuildConfig.BASE_URL}media/")
                                    )
                                }



                                mViewModel.prepareAndCheckUser(user) { error ->
                                    Timber.d("Preparing $error")

                                    lifecycleScope.launch(Dispatchers.IO) {
                                        if (!error.isNullOrEmpty() && error.isDigitsOnly()) {
                                            userPreferences.saveChatUserId(error.toInt())
                                        }
                                        chatUserId = getChatUserId() ?: 0
                                        qbCurrentUser = QBUser()
                                        qbCurrentUser!!.login = mUser!!.id
                                        qbCurrentUser!!.id = chatUserId
                                        qbCurrentUser!!.fullName = mUser!!.fullName
                                        qbCurrentUser!!.password = mUser!!.id

                                        initChatMessagesList()
                                    }
                                }
                                return@observe
                            }
                            mUser = it
                            if(mUser?.avatarPhotos?.size != 0) {
                                updateNavItem(
                                    mUser?.avatarPhotos?.get(mUser!!.avatarIndex!!)?.url?.replace(
                                        "http://95.216.208.1:8000/media/",
                                        "${BuildConfig.BASE_URL}media/"
                                    )
                                )
                            }
                        }
                    }


                lifecycleScope.launch(Dispatchers.IO) {
                    userToken = getCurrentUserToken()!!
                    try {
                        val data=apolloClientSubscription(this@MainActivity, userToken!!).subscription(
                            ChatRoomSubscription(userToken!!)
                        ).toFlow()
                            .retryWhen { cause, attempt ->
                                Timber.d("reealltime retry $attempt ${cause.message}")
                                delay(attempt * 1000)
                                true
                            }.collect { newMessage ->
                                if (newMessage.hasErrors()) {
                                    Timber.d("reealltime response error = ${newMessage.errors?.get(0)?.message}")
                                } else {
                                    Timber.d("reealltime onNewMessage ${newMessage.data?.onNewMessage?.message?.timestamp}")
                                    Log.d(
                                        "reealltime",
                                        "reealltime ${newMessage.data?.onNewMessage?.message?.content}"
                                    )
                                    Log.d(
                                        "reealltime",
                                        "reealltime ${newMessage.data?.onNewMessage?.message?.timestamp}"
                                    )
                                    viewModel?.onNewMessage(newMessage = newMessage.data?.onNewMessage?.message)
                                }
                            }
                        Timber.d("reealltime 2")
                    } catch (e2: Exception) {
                        e2.printStackTrace()
                        Timber.d("reealltime exception= ${e2.message}")
                    }
                }


        }






        setupNavigation()


    }
    fun nouserexist()
    {
        lifecycleScope.launch(Dispatchers.Main) {
            App.userPreferences.clear()
            clearAppData()
            val intent = Intent(this@MainActivity, SplashActivity::class.java)
            startActivity(intent)
            finishAffinity()
        }
    }


    private fun clearAppData() {
        try {
            // clearing app data
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {

                val activityManager =
                    getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

                activityManager.clearApplicationUserData()

            } else {

                val packageName: String = applicationContext.packageName
                val runtime = Runtime.getRuntime()
                runtime.exec("pm clear $packageName")
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    fun getmsgsubscriptionlistner() {
        job = lifecycleScope.launch {
            viewModel.newMessageFlow.collect { message ->
                message?.let { newMessage ->
                    if (userId != message.userId.id) {
//                        sendNotification(message)
                        updatechatbadge()
                    }

                }
            }
        }
    }

    fun updateFirebaseToken(userUpdateRepository: UserUpdateRepository) {

        Firebase.messaging.getToken().addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.d("App", "Fetching FCM registration token failed")
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            Timber.d("FirebaseToken", "" + token)
            token?.let {
                GlobalScope.launch {
                    val userId = App.userPreferences.userId.first()
                    val userToken = App.userPreferences.userToken.first()

                    if (userId != null && userToken != null) {
                        var res = userUpdateRepository.updateFirebasrToken(userId, token, userToken)
                        getmsgsubscriptionlistner()

                        Timber.d("TOKEN")
                    }
                }
            }

        })
    }


    private fun sendNotification(message: ChatRoomSubscription.Message) {




        notificationOpened = false
        val intent = Intent(App.getAppContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
        val id2 = message.roomId.id

        intent.putExtra("isChatNotification", "yes")
        intent.putExtra("roomIDs", id2)


        var pendingIntent: PendingIntent? = null
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val soundUri: Uri

        soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

//        soundUri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.iphone_ringtone);


        val mBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(message.userId.fullName)
                .setContentText(message.content)
                .setSound(soundUri)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setPriority(PRIORITY_HIGH)


        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.app_name)
            val description = getString(R.string.app_name)
            val importance = IMPORTANCE_HIGH
            val channel = NotificationChannel(getString(R.string.app_name), name, importance)
            channel.description = description
            val attributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()
            channel.enableLights(true)
            channel.enableVibration(true)
            channel.setSound(soundUri, attributes)

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationManager = NotificationManagerCompat.from(this)


        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(Random.nextInt(), mBuilder.build())


    }

    override fun onPause() {

        super.onPause()
    }

    private fun updatechatbadge() {

        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(this@MainActivity, userToken!!).query(GetAllRoomsQuery(20))
                    .execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                binding.root.snackbar("Exception all room API ${e.message}")
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

            if (res.hasErrors() == false) {
                if (res.data?.rooms != null) {
                    val allRoom = res.data?.rooms!!.edges
                    if (allRoom.isNullOrEmpty()) {
                        return@launchWhenResumed
                    }

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
                        binding.navView.updateMessagesCount(totoalunread)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

            }
        }
    }


    override fun setupClickListeners() {

    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        unregisterQbChatListeners()
    }

    private fun setupNavigation() {
        binding.navView.itemClickListener = { pos ->
            goToMainActions(pos)
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Timber.tag("Destination")
                .d("id = ${destination.id}, name = ${destination.navigatorName}")
            binding.navView.setVisibility(true)
            val selected = when (destination.id) {
                R.id.searchInterestedInFragment -> 0
                R.id.userMomentsFragment -> 1
                R.id.newMomentsFragment -> 2
                R.id.messengerListFragment -> 3
                R.id.userProfileFragment -> 4
                R.id.messengerChatFragment -> {
                    binding.navView.setVisibility(false)
                    3
                }
                R.id.sendFirstMessengerChatFragment -> {
                    binding.navView.setVisibility(false)
                    4
                }
                else -> -1
            }
            binding.navView.selectItem(selected)
        }

        updateLocation()
        binding.mainNavView.getHeaderView(0).findViewById<View>(R.id.btnHeaderClose)
            .setOnClickListener {
                disableNavigationDrawer()
            }
        binding.mainNavView.itemIconTintList = null
        binding.mainNavView.setNavigationItemSelectedListener {
            Handler(Looper.getMainLooper()).postDelayed({
                when (it.itemId) {
                    R.id.nav_item_search -> openSearchScreen()
                    R.id.nav_item_buy_coin -> navController.navigate(R.id.actionGoToPurchaseFragment)
                    R.id.nav_item_contact -> this.startEmailIntent(
                        com.i69app.data.config.Constants.ADMIN_EMAIL,
                        "",
                        ""
                    )
                    R.id.nav_item_privacy -> {
                        val intent = Intent(this, PrivacyOrTermsConditionsActivity::class.java)
                        intent.putExtra("type", "privacy")
                        startActivity(intent)
                    }
                    R.id.nav_item_settings -> openSettingsScreen()
                }
            }, 200)

            binding.drawerLayout.closeNavigationDrawer()
            return@setNavigationItemSelectedListener true
        }

        goToMainActions(3)
    }

    private fun observeNotification() {
        if ((intent.hasExtra("isNotification") && intent.getStringExtra("isNotification") != null)) {

            val bundle = Bundle()
            bundle.putString("ShowNotification", "true")
            navController.navigate(R.id.action_user_moments_fragment, bundle)


        } else if ((intent.hasExtra("isChatNotification") && intent.getStringExtra("isChatNotification") != null)) {


            if ((intent.hasExtra("roomIDs") && intent.getStringExtra("roomIDs") != null)) {
                val rID = intent.getStringExtra("roomIDs")

                val bundle = Bundle()
                bundle.putString("roomIDNotify", rID)
                navController.navigate(R.id.messengerListFragment, bundle)

            }

//            binding.root.snackbar("Chat Message Notification cliked.")


        } else if (intent.hasExtra(ARGS_SCREEN) && intent.getStringExtra(ARGS_SCREEN) != null) {
            if (intent.hasExtra(ARGS_SENDER_ID) && intent.getStringExtra(ARGS_SENDER_ID) != null) {
                val senderId = intent.getStringExtra(ARGS_SENDER_ID)
                onNotificationClick(senderId!!)

            } else {
                openMessagesScreen()
            }
        }
    }

    private fun onNotificationClick(senderId: String) {
//        val msgPreviewModel: MessagePreviewModel? = QbDialogHolder.getChatDialogById(senderId)
//        msgPreviewModel?.let {
//            viewModel?.setSelectedMessagePreview(it)
//            navController.navigate(R.id.globalUserToChatAction)
//        }
    }

    fun drawerSwitchState() {
        binding.drawerLayout.drawerSwitchState()
    }

    fun enableNavigationDrawer() {
        binding.drawerLayout.enableNavigationDrawer()
    }

    fun disableNavigationDrawer() {
        binding.drawerLayout.disableNavigationDrawer()
    }

    private fun updateNavItem(userAvatar: Any?) {
        binding.navView.setItems(
            arrayListOf(
                Pair(R.drawable.ic_search_inactive, R.drawable.ic_search_active),
                Pair(R.drawable.ic_home_inactive, R.drawable.ic_home_active),
                Pair(R.drawable.ic_add_btn, R.drawable.icon_add_black_button),
                Pair(R.drawable.ic_chat_inactive, R.drawable.ic_chat_active),
                Pair(userAvatar, userAvatar)
            )
        )
    }

    private fun updateLocation() {
        /*Permissions.check(this, Manifest.permission.ACCESS_FINE_LOCATION, null, object : PermissionHandler() {
            override fun onGranted() {
                val locationService = LocationServices.getFusedLocationProviderClient(this@MainActivity)
                locationService.lastLocation.addOnSuccessListener { location: Location? ->
                    val lat: Double? = location?.latitude
                    val lon: Double? = location?.longitude
                    toast("lat = $lat lng = $lon")
                    if (lat != null && lon != null) {
                        // Update Location
                        lifecycleScope.launch(Dispatchers.Main) {
                            mViewModel.updateLocation(userId = userId!!, location = arrayOf(lat, lon), token = userToken!!)
                        }
                    }
                }
            }
        })*/
        if (hasPermissions(applicationContext, locPermissions)) {
            val locationService = LocationServices.getFusedLocationProviderClient(this@MainActivity)
            locationService.lastLocation.addOnSuccessListener { location: Location? ->
                val lat: Double? = location?.latitude
                val lon: Double? = location?.longitude
//                toast("lat = $lat lng = $lon")
                if (lat != null && lon != null) {
                    // Update Location
                    lifecycleScope.launch(Dispatchers.Main) {
                        mViewModel.updateLocation(
                            userId = userId!!,
                            location = arrayOf(lat, lon),
                            token = userToken!!
                        )
                    }
                }
            }
        } else {
            permissionReqLauncher.launch(locPermissions)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        if (!notificationOpened) {
            notificationOpened = true
            observeNotification()
        }

    }

    private fun goToMainActions(position: Int) {
        when (position) {
            0 -> openSearchScreen()
            1 -> openUserMoments()
            2 -> openNewUserMoment()
            3 -> openMessagesScreen()
            4 -> openProfileScreen()
        }
    }

    private fun openSearchScreen() {
        navController.navigate(R.id.action_global_search_interested_in)
    }

    public fun openUserMoments() {
        val bundle = Bundle()
        bundle.putString("ShowNotification", "false")
        navController.navigate(R.id.action_user_moments_fragment, bundle)
    }

    private fun openNewUserMoment() {
        navController.navigate(R.id.action_new_user_moment_fragment)
    }

    private fun openMessagesScreen() {
        navController.navigate(R.id.messengerListAction)
    }

    private fun openProfileScreen() {
        navController.navigate(R.id.action_global_user_profile)
    }

    private fun openSettingsScreen() {
        navController.navigate(R.id.actionGoToSettingsFragment)
    }


    // Chat Section (Companion Object)
    companion object {
        const val CHAT_TAG = "SH_CHAT"
        const val ARGS_SCREEN = "screen"
        const val ARGS_MESSAGE_SCREEN = "message_screen"
        const val ARGS_SENDER_ID = "sender_id"
        const val ARGS_CHANNEL_ID = "5f2f7e32-cf68-4a8e-b27b-41b692aab5b1"

        var notificationOpened = false
        private var viewModel: UserViewModel? = null
        private var binding: ActivityMainBinding? = null
        private val dialogsManager: DialogsManager = DialogsManager()


        fun setViewModel(updatedViewModel: UserViewModel, updatedBinding: ActivityMainBinding) {
            viewModel = updatedViewModel
            binding = updatedBinding
        }

        fun loadAllMessagesList(
            mViewModel: UserViewModel,
            currentUserChatId: Int,
            userId: String,
            token: String,
            callback: (() -> Unit)? = null,
            loadingCallback: (() -> Unit)? = null
        ) {
            callback?.let { loadingCallback?.invoke() }

            mViewModel.loadAllDialogs(
                token = token,
                userId = userId,
                currentUserChatId = currentUserChatId
            ) {
                Timber.tag(CHAT_TAG).i("Loaded All Dialogs. Now It will update.")
                updateDialogs()
                callback?.invoke()
            }
        }

        fun updateDialogs() {
            val listOfDialogs = ArrayList(QbDialogHolder.dialogsMap.values)
            updateUnseenMessages(listOfDialogs)
        }

        fun updateAndDecreaseUnSeenMessages(dialogId: String?, chatMessage: QBChatMessage) =
            dialogsManager.updateAndDecreaseUnSeenMessages(dialogId, chatMessage)

        private fun updateUnseenMessages(previewMsgList: ArrayList<MessagePreviewModel>?) {
            if (!previewMsgList.isNullOrEmpty()) {
                var unseenMsgCount = 0
                previewMsgList.forEach { msgPreview ->
                    if (msgPreview.chatDialog.unreadMessageCount > 0) unseenMsgCount =
                        unseenMsgCount.plus(1)
                }
                binding!!.navView.updateMessagesCount(unseenMsgCount)
                viewModel!!.updateAdapterFlow()
            }
        }
    }


    private suspend fun initChatMessagesList() {
        registerQbChatListeners()
        withContext(Dispatchers.Main) {
            loadAllMessagesList(
                mViewModel,
                qbCurrentUser!!.id,
                userId = userId!!,
                token = userToken!!,
                callback = {
                    hideProgressView()
                    observeNotification()
                },
                loadingCallback = { showProgressView() }
            )
        }
    }

    private fun initConnectionListener() {
        chatConnectionListener = object : ChatConnectionListener() {
            override fun reconnectionSuccessful() {
                super.reconnectionSuccessful()
                loadAllMessagesList(
                    mViewModel,
                    qbCurrentUser!!.id,
                    userId = userId!!,
                    token = userToken!!
                )
            }
        }
    }

    private fun registerQbChatListeners() {
        ChatHelper.addConnectionListener(chatConnectionListener)
        try {
            systemMessagesManager = QBChatService.getInstance().systemMessagesManager
            incomingMessagesManager = QBChatService.getInstance().incomingMessagesManager
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.tag(CHAT_TAG).e("Error ${e.message}")
            reLoginToChat()
        }
        Timber.tag(CHAT_TAG).w("System Message: $systemMessagesManager")
        Timber.tag(CHAT_TAG).w("Incoming: $incomingMessagesManager")

        if (incomingMessagesManager == null) {
            reLoginToChat()
            return
        }

        systemMessagesManager?.addSystemMessageListener(systemMessagesListener)
        incomingMessagesManager?.addDialogMessageListener(allDialogsMessageListener)
        dialogsManager.addManagingDialogsCallbackListener(dialogMessageListener)
    }

    private fun unregisterQbChatListeners() {
        ChatHelper.removeConnectionListener(chatConnectionListener)
        incomingMessagesManager?.removeDialogMessageListrener(allDialogsMessageListener)
        systemMessagesManager?.removeSystemMessageListener(systemMessagesListener)
        dialogsManager.removeManagingDialogsCallbackListener(dialogMessageListener)
    }

    private fun reLoginToChat() {
        mViewModel.loginToChat(qbCurrentUser!!) {
            if (it.isNullOrEmpty() || it.isDigitsOnly()) {
                registerQbChatListeners()
                loadAllMessagesList(
                    mViewModel,
                    qbCurrentUser!!.id,
                    userId = userId!!,
                    token = userToken!!
                )
            }
        }
    }


    inner class DialogMangerListener : DialogsManager.ManagingDialogsCallbacks {
        override fun onDialogCreated(chatDialog: QBChatDialog) {
            Timber.tag(CHAT_TAG).i("Dialog Created: $chatDialog")
            loadAllMessagesList(
                mViewModel,
                qbCurrentUser!!.id,
                userId = userId!!,
                token = userToken!!
            )
        }

        override fun onDialogUpdated(chatDialog: String?) {
            Timber.tag(CHAT_TAG).i("Dialog Updated $chatDialog")
            updateDialogs()
        }

        override fun onNewDialogLoaded(chatDialog: QBChatDialog) {
            Timber.tag(CHAT_TAG).i("Dialog Loaded $chatDialog")
            updateDialogs()
        }
    }

    inner class AllDialogsMessageListener : QbChatDialogMessageListenerImpl() {
        override fun processMessage(
            dialogId: String,
            qbChatMessage: QBChatMessage,
            senderId: Int?
        ) {
            Timber.tag(CHAT_TAG).d("Processing received message: ${qbChatMessage.body}")
            if (senderId != qbCurrentUser?.id) {
                dialogsManager.onGlobalMessageReceived(
                    dialogId,
                    qbChatMessage,
                    currentUserChatId = chatUserId,
                    token = userToken!!,
                    userDetailsRepository
                )
            }
        }
    }

    inner class SystemMessagesListener : QBSystemMessageListener {
        override fun processMessage(qbChatMessage: QBChatMessage) {
            dialogsManager.onSystemMessageReceived(
                qbChatMessage,
                currentUserChatId = chatUserId,
                token = userToken!!,
                userDetailsRepository
            )
        }

        override fun processError(e: QBChatException, qbChatMessage: QBChatMessage) {
            e.onError("SystemMessagesListener")
        }
    }

    private val locPermissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    ///// Chat Messages

}