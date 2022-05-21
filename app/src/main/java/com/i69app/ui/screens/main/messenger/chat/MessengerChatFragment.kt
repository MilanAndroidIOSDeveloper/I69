package com.i69app.ui.screens.main.messenger.chat

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.quickblox.chat.QBChatService
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.chat.model.QBChatMessage
import com.quickblox.content.model.QBFile
import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.messages.MessagesListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.i69app.R
import com.i69app.data.enums.DeductCoinMethod
import com.i69app.data.models.*
import com.i69app.databinding.AlertFullImageBinding
import com.i69app.databinding.FragmentMessengerChatBinding
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.screens.ImagePickerActivity
import com.i69app.ui.screens.main.MainActivity
import com.i69app.ui.screens.main.messenger.listeners.QbChatDialogMessageListenerImpl
import com.i69app.ui.screens.main.search.userProfile.SearchUserProfileFragment
import com.i69app.ui.screens.main.search.userProfile.SearchUserProfileFragment.Companion.ARGS_FROM_CHAT
import com.i69app.ui.viewModels.SearchViewModel
import com.i69app.ui.viewModels.UserViewModel
import com.i69app.utils.*
import com.i69app.utils.qb.CHAT_HISTORY_ITEMS_PER_PAGE
import com.i69app.utils.*
import timber.log.Timber

@AndroidEntryPoint
class MessengerChatFragment : BaseFragment<FragmentMessengerChatBinding>() {

    private var textMessageCost: Int = 30
    private var imageMessageCost: Int = 50
    private val mViewModel: UserViewModel by activityViewModels()
    private val searchViewModel: SearchViewModel by activityViewModels()
    private lateinit var adapter: MessagesListAdapter<IMessage>

    private var chatDialog: QBChatDialog? = null
    private var userId: String? = null
    private var userToken: String? = null
    private var otherUser: User? = null
    private var currentUser: User? = null
    private var chatUserId: Int = 0
    private var skipPagination = 0

    private var chatMessageListener: ChatMessageListener = ChatMessageListener()

    private val photosLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        val data = activityResult.data
        if (activityResult.resultCode == Activity.RESULT_OK) {
            val result = data?.getStringExtra("result")
            Timber.d("Result $result")

            if (result != null) {
                /// Send Image Message
                if (currentUser?.purchaseCoins!!.toInt() >= imageMessageCost) {
                    showProgressView()

                    val chatWithNotificationRequest = ChatWithNotificationRequest(
                        currentUserToken = userToken,
                        receiverId = otherUser?.id,
                        chatDialog = chatDialog,
                        msgFilePath = result,
                        notificationTitle = getString(R.string.new_unread_messages),
                        notificationBody = String.format(getString(R.string.user_message), currentUser!!.fullName, getString(R.string.image))
                    )

                    mViewModel.sendMessageToServer(chatWithNotificationRequest) { chatMsg, error ->
                        if (error != null) {
                            binding.root.snackbar("${getString(R.string.something_went_wrong)} $error")
                            return@sendMessageToServer
                        }
                        processMessageResult(deductCoinMethod = com.i69app.data.enums.DeductCoinMethod.IMAGE_MESSAGE)
                        hideProgressView()
                        chatMsg!!.senderId = chatUserId
                        addMessage(chatMsg, true)
                    }
                } else {
                    showInSufficientCoinsSnackbar()
                }
            }
        }
    }


    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentMessengerChatBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        getTypeActivity<MainActivity>()?.setSupportActionBar(binding.toolbar)
        val supportActionBar = getTypeActivity<MainActivity>()?.supportActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)
        supportActionBar?.title = ""
        currentUser = null
        setHasOptionsMenu(true)
        initInputListener()

        val msgPreviewModel = mViewModel.getSelectedMessagePreview()
//        chatDialog = msgPreviewModel?.chatDialog
//        otherUser = msgPreviewModel?.user
        chatDialog = null
        otherUser = null
        setupOtherUserData()

        lifecycleScope.launch(Dispatchers.Main) {
            chatUserId = getChatUserId()!!
            userId = getCurrentUserId()
            userToken = getCurrentUserToken()

            mViewModel.getCoinSettings(token = userToken!!).observe(viewLifecycleOwner) {
                it?.let { coinSettings ->
                    coinSettings.forEach { coinSetting ->
                        when (coinSetting.method) {
                            DeductCoinMethod.MESSAGE.name -> textMessageCost =
                                coinSetting.coinsNeeded
                            DeductCoinMethod.IMAGE_MESSAGE.name -> imageMessageCost =
                                coinSetting.coinsNeeded
                        }
                    }
                }
            }

            mViewModel.getCurrentUser(userId!!, token = userToken!!, false).observe(viewLifecycleOwner) { user ->
                Timber.d("User $user")
                user?.let {
                    if (currentUser == null) {
                        currentUser = it
                        binding.coinsCounter.text = currentUser!!.purchaseCoins.toString()
                        val coinsTextResource =
                            if (currentUser!!.purchaseCoins == 0 || currentUser!!.purchaseCoins == 1) R.string.coin_left else R.string.coins_left
                        binding.coinsLeftTv.text = getString(coinsTextResource)
                        setupChat()
                        setupChatUI()
                        loadAllMessagesList()

                    } else {
                        currentUser = it
                        binding.coinsCounter.text = currentUser!!.purchaseCoins.toString()
                    }
                }
            }
        }
    }

    override fun setupClickListeners() {
        binding.closeBtn.setOnClickListener { requireActivity().onBackPressed() }
        binding.coinsLayout.setOnClickListener { navigateToPurchase() }
    }

    override fun onPause() {
        super.onPause()
        binding.input.inputEditText?.hideKeyboard()
    }

    private fun initInputListener() {
        binding.input.setInputListener { input ->
            binding.input.inputEditText?.hideKeyboard()
            return@setInputListener if (currentUser?.purchaseCoins!!.toInt() >= textMessageCost) {
                val msgText = input.toString()
                val chatNotificationBody = getChatMsgNotificationBody(msgText)

                val chatWithNotificationRequest = ChatWithNotificationRequest(
                    currentUserToken = userToken,
                    receiverId = otherUser?.id,
                    chatDialog = chatDialog,
                    msgText = msgText,
                    notificationTitle = getString(R.string.new_unread_messages),
                    notificationBody = String.format(getString(R.string.user_message), currentUser?.fullName, chatNotificationBody)
                )

                mViewModel.sendMessageToServer(chatWithNotificationRequest) { chatMsg, error ->
                    if (error != null) {
                        binding.root.snackbar("${getString(R.string.something_went_wrong)} $error")
                        return@sendMessageToServer
                    }
                    processMessageResult(deductCoinMethod = com.i69app.data.enums.DeductCoinMethod.MESSAGE)
                    chatMsg!!.senderId = chatUserId
                    addMessage(chatMsg, true)
                }
                true
            } else {
                showInSufficientCoinsSnackbar()
                false
            }
        }
        binding.input.setAttachmentsListener {
            val intent = Intent(requireActivity(), ImagePickerActivity::class.java)
            photosLauncher.launch(intent)
        }
    }

    private fun setupOtherUserData() {
        binding.userName.text = otherUser?.fullName
        binding.userProfileImg.loadCircleImage(otherUser?.avatarPhotos!!.get(otherUser!!.avatarIndex!!).url ?: "")
        binding.userProfileImgContainer.setOnClickListener { navigateToOtherUserProfile() }
    }

    private fun setupChatUI() {
        val holdersConfig = MessageHolders()
            .setIncomingTextLayout(R.layout.item_custom_incoming_text_message)
            .setOutcomingTextLayout(R.layout.item_custom_outcoming_text_message)
            .setIncomingImageLayout(R.layout.item_custom_incoming_image_message)
            .setOutcomingImageLayout(R.layout.item_custom_outcoming_image_message)

        adapter = MessagesListAdapter(currentUser?.id, holdersConfig) { imageView, url, payload ->
            var newUrl: String? = url
            // need this hack to make click listener on user avatar
            if (url == currentUser?.avatarPhotos!!.get(currentUser!!.avatarIndex!!).url) {
                newUrl = currentUser?.avatarPhotos!!.get(currentUser!!.avatarIndex!!).url
                imageView.loadCircleImage(newUrl ?: "")
                return@MessagesListAdapter

            } else if (url == otherUser?.avatarPhotos!!.get(otherUser!!.avatarIndex!!).url) {
                newUrl = otherUser?.avatarPhotos!!.get(otherUser!!.avatarIndex!!).url
                imageView.setOnClickListener {
                    navigateToOtherUserProfile()
                }
                imageView.loadCircleImage(newUrl ?: "")
                return@MessagesListAdapter
            }
            imageView.loadImage(newUrl ?: "")
        }
        adapter.setOnMessageClickListener { message ->
            if (message is MediaChatMessage) {
                val w = resources.displayMetrics.widthPixels
                val h = resources.displayMetrics.heightPixels
                showImageDialog(w, h, message.imageUrl)
            }
        }
        adapter.disableSelectionMode()
        binding.messagesList.setAdapter(adapter)
        binding.messagesList.setHasFixedSize(true)
    }

    private fun setupChat() {
        try {
            chatDialog?.initForChat(QBChatService.getInstance())
        } catch (e: Exception) {
            e.printStackTrace()
            binding.root.snackbar(getString(R.string.something_went_wrong))
            requireActivity().onBackPressed()
        }
        chatDialog?.addMessageListener(chatMessageListener)
    }

    private fun loadAllMessagesList() {
        mViewModel.loadChatHistory(
            chatDialog, skipPagination) { msgs, error ->
            adapter.clear()
            if (error != null) {
                binding.root.snackbar("${getString(R.string.something_went_wrong)} $error")
                hideProgressView()
                return@loadChatHistory
            }
            if (msgs != null) {
                msgs.forEach { message ->
                    addMessage(message, false)
                }
                MainActivity.updateAndDecreaseUnSeenMessages(chatDialog?.dialogId, msgs.last())
            }

            skipPagination += CHAT_HISTORY_ITEMS_PER_PAGE
            hideProgressView()
        }
    }

    private fun addMessage(message: QBChatMessage, updateUnseen: Boolean) {
        val user = if (message.senderId == chatUserId) currentUser!! else otherUser
        if (!message.attachments.isNullOrEmpty() && message.attachments.firstOrNull() != null) {
            val imageUrl = QBFile.getPrivateUrlForUID(message.attachments.first().id)
            adapter.addToStart(MediaChatMessage(user, message.dateSent * 1000, imageUrl), true)

        } else {
            adapter.addToStart(
                com.i69app.data.models.ChatMessage(
                    user,
                    message.dateSent * 1000,
                    message.body ?: ""
                ), true)
        }
        if (updateUnseen) MainActivity.updateAndDecreaseUnSeenMessages(chatDialog?.dialogId, message)
    }


    private fun processMessageResult(deductCoinMethod: com.i69app.data.enums.DeductCoinMethod) {
        lifecycleScope.launch(Dispatchers.Main) {
            when (val response = mViewModel.deductCoin(currentUser!!.id, token = userToken!!, deductCoinMethod)) {
                is Resource.Success -> mViewModel.getCurrentUser(currentUser!!.id, token = userToken!!, true)
                is Resource.Error -> binding.root.snackbar("${getString(R.string.something_went_wrong)} ${response.message}")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.message_menu, menu)
        binding.toolbar.overflowIcon = ContextCompat.getDrawable(requireContext(), R.drawable.drawable_report)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            (activity as? MainActivity)?.onBackPressed()
            true
        }
        R.id.item_block -> {
            blockAccount(otherUser)
            true
        }
        R.id.item_report -> {
            reportAccount(otherUser?.id)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun navigateToPurchase() {
        findNavController().navigate(R.id.actionGoToPurchaseFragment)
    }

    private fun navigateToOtherUserProfile() {
        searchViewModel.selectedUser.value = otherUser
        var bundle = Bundle()
        bundle.putBoolean(ARGS_FROM_CHAT, true)
        bundle.putString("userId", otherUser?.id)
        Handler(Looper.getMainLooper()).postDelayed({
            findNavController().navigate(
                destinationId = R.id.action_global_otherUserProfileFragment,
                popUpFragId = null,
                animType = AnimationTypes.SLIDE_ANIM,
                inclusive = false,
                args = bundle
            )
        }, 200)
    }

    private fun showInSufficientCoinsSnackbar() {
        binding.root.snackbar(
            requireContext().getString(R.string.not_enough_coins),
            duration = Snackbar.LENGTH_INDEFINITE
        ) {
            navigateToPurchase()
        }
    }

    private fun blockAccount(otherUser: User?) {
        lifecycleScope.launch(Dispatchers.Main) {
            when (val response = mViewModel.blockUser(userId, otherUser?.id, token = userToken)) {
                is Resource.Success -> {
                    mViewModel.getCurrentUser(userId!!, userToken!!, true)
                    hideProgressView()
                    binding.root.snackbar("${otherUser?.fullName} blocked!")
                }
                is Resource.Error -> {
                    hideProgressView()
                    Timber.e("${getString(R.string.something_went_wrong)} ${response.message}")
                    binding.root.snackbar("${getString(R.string.something_went_wrong)} ${response.message}")
                }
            }
        }
    }

    private fun reportAccount(otherUserId: String?) {
        lifecycleScope.launch(Dispatchers.Main) {
            reportUserAccount(token = userToken, currentUserId = userId, otherUserId = otherUserId, mViewModel = mViewModel) { message ->
                hideProgressView()
                binding.root.snackbar(message)
            }
        }
    }

    private fun showImageDialog(width: Int, height: Int, imageUrl: String) {
        val dialog = Dialog(requireContext())
        val dialogBinding = AlertFullImageBinding.inflate(layoutInflater, null, false)
        dialogBinding.fullImg.loadImage(imageUrl) {
            dialogBinding.alertTitle.setViewGone()
        }
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setLayout(width, height)
        dialog.show()
        dialogBinding.root.setOnClickListener {
            dialog.cancel()
        }
    }

    private inner class ChatMessageListener : QbChatDialogMessageListenerImpl() {
        override fun processMessage(dialogId: String, qbChatMessage: QBChatMessage, senderId: Int?) {
            Timber.d("Processing Received Message: $qbChatMessage")
            addMessage(qbChatMessage, false)
        }
    }

}