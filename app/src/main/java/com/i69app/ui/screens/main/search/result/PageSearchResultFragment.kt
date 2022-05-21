package com.i69app.ui.screens.main.search.result

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.i69app.R
import com.i69app.data.models.User
import com.i69app.databinding.FragmentPageSearchResultBinding
import com.i69app.ui.adapters.UsersSearchListAdapter
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.screens.main.search.userProfile.SearchUserProfileFragment
import com.i69app.ui.viewModels.SearchViewModel
import com.i69app.utils.*
import com.i69app.utils.AnimationTypes

@AndroidEntryPoint
class PageSearchResultFragment : BaseFragment<FragmentPageSearchResultBinding>(), UsersSearchListAdapter.UserSearchListener {

    companion object {
        private const val ARG_DATA_BY_PAGE_ID = "ARG_PAGE_ID"

        fun newInstance(page: Int): PageSearchResultFragment {
            val args = Bundle()
            args.putInt(ARG_DATA_BY_PAGE_ID, page)
            val fragment = PageSearchResultFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private var mPage: Int = 0
    private val mViewModel: SearchViewModel by activityViewModels()
    private lateinit var usersAdapter: UsersSearchListAdapter


    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentPageSearchResultBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        mPage = requireArguments().getInt(ARG_DATA_BY_PAGE_ID)
        navController = findNavController()
        initSearch()

        lifecycleScope.launch {
            val userToken = getCurrentUserToken()!!

            mViewModel.getDefaultPickers(userToken).observe(viewLifecycleOwner, { pickers ->
                pickers?.let { picker ->
                    usersAdapter = UsersSearchListAdapter(this@PageSearchResultFragment, picker)
                    binding.usersRecyclerView.adapter = usersAdapter

                    val users: ArrayList<User> = when (mPage) {
                        0 -> mViewModel.getRandomUsers()
                        1 -> mViewModel.getPopularUsers()
                        else -> mViewModel.getMostActiveUsers()
                    }

                    if (users.isNullOrEmpty()) {
                        binding.noUsersLabel.setViewVisible()

                    } else {
                        binding.noUsersLabel.setViewGone()
                        usersAdapter.updateItems(users)
                    }
                }
            })
        }
    }

    override fun setupClickListeners() {
    }

    private fun initSearch() {
        binding.keyInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filteredUsers = when (mPage) {
                    0 -> filterUsers(s.toString(), mViewModel.getRandomUsers())
                    1 -> filterUsers(s.toString(), mViewModel.getPopularUsers())
                    2 -> filterUsers(s.toString(), mViewModel.getMostActiveUsers())
                    else -> arrayListOf()
                }
                usersAdapter.updateItems(filteredUsers)
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun filterUsers(text: String, fullListOfUsers: List<User>): List<User> {
        return if (text.trim().isEmpty()) {
            fullListOfUsers

        } else {

            val filteredList: ArrayList<User> = ArrayList()
            fullListOfUsers.forEach {
                if (it.fullName.lowercase().contains(text.lowercase())) filteredList.add(it)
            }
            filteredList
        }
    }

    override fun onItemClick(position: Int, user: User) {
        mViewModel.selectedUser.value = user
        var bundle = Bundle()
        bundle.putBoolean(SearchUserProfileFragment.ARGS_FROM_CHAT, false)
        bundle.putString("userId", user.id)
        findNavController().navigate(
            destinationId = R.id.action_global_otherUserProfileFragment,
            popUpFragId = null,
            animType = AnimationTypes.SLIDE_ANIM,
            inclusive = true,
            args = bundle
        )
    }

    override fun onUnlockFeatureClick() {
        navController.navigate(R.id.actionGoToPurchaseFragment)
    }


}