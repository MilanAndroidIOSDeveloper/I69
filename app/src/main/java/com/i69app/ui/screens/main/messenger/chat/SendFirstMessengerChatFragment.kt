package com.i69app.ui.screens.main.messenger.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.quickblox.chat.QBChatService
import com.stfalcon.chatkit.messages.MessageInput
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.i69app.R
import com.i69app.data.models.ChatWithNotificationRequest
import com.i69app.data.models.User
import com.i69app.databinding.FragmentMessengerChatBinding
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.screens.main.MainActivity
import com.i69app.ui.viewModels.SearchViewModel
import com.i69app.ui.viewModels.UserViewModel
import com.i69app.utils.Resource
import com.i69app.utils.getChatMsgNotificationBody
import com.i69app.utils.loadCircleImage
import com.i69app.utils.snackbar

@AndroidEntryPoint
class SendFirstMessengerChatFragment : BaseFragment<FragmentMessengerChatBinding>(), MessageInput.InputListener {

    private val viewModel: SearchViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private var userId: String? = null
    private var userToken: String? = null


    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentMessengerChatBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        lifecycleScope.launch {
            userId = getCurrentUserId()!!
            userToken = getCurrentUserToken()!!
        }
        setupOtherUserData()
    }

    override fun setupClickListeners() {
        binding.closeBtn.setOnClickListener { requireActivity().onBackPressed() }
        binding.input.setInputListener(this)
    }

    private fun setupOtherUserData() {
        val otherUser = viewModel.selectedUser.value!!
        binding.userName.text = otherUser.fullName
        binding.userProfileImg.loadCircleImage(otherUser.avatarPhotos!!.get(otherUser.avatarIndex!!).url ?: "")
    }

    override fun onSubmit(input: CharSequence?): Boolean {
        val currentUser = userViewModel.getCurrentUser(userId!!, token = userToken!!, false).value!!
        val mUser = viewModel.selectedUser.value!!
        val msgText = input.toString()
        showProgressView()

        userViewModel.createNewChatDialog(receiverId = mUser.id) { chatDialog, errorMessage ->
            if (errorMessage != null) {
                hideProgressView()
                binding.root.snackbar("${getString(R.string.something_went_wrong)} $errorMessage")

            } else {
                chatDialog!!.initForChat(QBChatService.getInstance())
                val chatNotificationBody = getChatMsgNotificationBody(msgText)

                val chatWithNotificationRequest = ChatWithNotificationRequest(
                    currentUserToken = userToken!!,
                    receiverId = mUser.id,
                    chatDialog = chatDialog,
                    msgText = msgText,
                    notificationTitle = getString(R.string.new_unread_messages),
                    notificationBody = String.format(getString(R.string.user_message), currentUser.fullName, chatNotificationBody)
                )

                userViewModel.sendMessageToServer(chatWithNotificationRequest) { _, error ->
                    if (error != null) {
                        binding.root.snackbar("${getString(R.string.something_went_wrong)} $error")
                        return@sendMessageToServer
                    }
                    updateLikes(currentUser = currentUser, otherUser = mUser)
                }
            }
        }

        return false
    }

    private fun updateLikes(currentUser: User, otherUser: User) {
        val currentUserLikes = ArrayList<String>()
        currentUserLikes.addAll(currentUser.likes.map { it.id })
        currentUserLikes.add(otherUser.id)

        val otherUserLikes = ArrayList<String>()
        otherUserLikes.addAll(otherUser.likes.map { it.id })
        otherUserLikes.add(currentUser.id)

        lifecycleScope.launch(Dispatchers.Main) {
            updateUserLikes(currentUser.id, userLikes = currentUserLikes) {

                lifecycleScope.launch(Dispatchers.Main) {
                    val currentUserChatId = getChatUserId()!!
                    updateUserLikes(otherUser.id, userLikes = otherUserLikes) {
                        MainActivity.loadAllMessagesList(userViewModel, currentUserChatId, userId = userId!!, token = userToken!!, callback = {
                            hideProgressView()
                            binding.root.snackbar(requireContext().getString(R.string.awesome_you_have_initiated))
                            requireActivity().onBackPressed()
                        })
                    }
                }

            }
        }
    }

    private suspend fun updateUserLikes(userId: String, userLikes: ArrayList<String>, callback: () -> Unit) {
        when (val response = userViewModel.updateUserLikes(userId, token = userToken!!, userLikes = userLikes)) {
            is Resource.Success -> callback()
            is Resource.Error -> {
                hideProgressView()
                binding.root.snackbar("${getString(R.string.something_went_wrong)} ${response.message}")
            }
        }
    }

}