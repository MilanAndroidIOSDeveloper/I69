package com.i69app.ui.screens.main.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.i69app.R
import com.i69app.data.models.BlockedUser
import com.i69app.data.models.User
import com.i69app.databinding.FragmentBlockedUsersSettingsBinding
import com.i69app.ui.adapters.BlockUsersListAdapter
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.viewModels.UserViewModel
import com.i69app.utils.Resource
import com.i69app.utils.snackbar
import timber.log.Timber

class BlockedUsersFragment : BaseFragment<FragmentBlockedUsersSettingsBinding>(), BlockUsersListAdapter.BlockListener {

    private val viewModel: UserViewModel by activityViewModels()
    private lateinit var adapter: BlockUsersListAdapter
    private var currentUser: User? = null

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentBlockedUsersSettingsBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        adapter = BlockUsersListAdapter(this)
        binding.blockedUsers.adapter = adapter

        lifecycleScope.launch(Dispatchers.Main) {
            val currentUserId = getCurrentUserId()!!
            val token = getCurrentUserToken()!!

            viewModel.getCurrentUser(currentUserId, token = token, false).observe(viewLifecycleOwner) { user ->
                user?.let {
                    currentUser = it
                    adapter.updateList(currentUser!!.blockedUsers)
                }
            }
        }
    }

    override fun setupClickListeners() {
        binding.toolbarHamburger.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    override fun unblock(user: BlockedUser) {
        showProgressView()

        lifecycleScope.launch(Dispatchers.Main) {
            val currentUserId = getCurrentUserId()!!
            val token = getCurrentUserToken()!!

            when (val response = viewModel.unblockUser(userId = currentUserId, token = token, blockedId = user.id)) {
                is Resource.Success -> {
                    viewModel.getCurrentUser(currentUserId, token = token, true)
                    hideProgressView()
                }
                is Resource.Error -> {
                    hideProgressView()
                    Timber.e("${getString(R.string.something_went_wrong)} ${response.message}")
                    binding.root.snackbar("${getString(R.string.something_went_wrong)} ${response.message}")
                }
            }
        }
    }

}