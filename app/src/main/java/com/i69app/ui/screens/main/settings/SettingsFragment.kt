package com.i69app.ui.screens.main.settings

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.i69app.R
import com.i69app.databinding.FragmentSettingsBinding
import com.i69app.singleton.App
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.screens.PrivacyOrTermsConditionsActivity
import com.i69app.ui.screens.SplashActivity
import com.i69app.ui.viewModels.UserViewModel
import kotlinx.coroutines.withContext

class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {

    private val viewModel: UserViewModel by activityViewModels()
    private var userId: String = ""
    private var userToken: String = ""

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentSettingsBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        navController = findNavController()
        lifecycleScope.launch {
            userId = getCurrentUserId()!!
            userToken = getCurrentUserToken()!!
        }
    }

    override fun setupClickListeners() {
        binding.toolbarHamburger.setOnClickListener {
            activity?.onBackPressed()
        }
        binding.buyCoinsContainer.setOnClickListener {
            navController.navigate(R.id.actionGoToPurchaseFragment)
        }
        binding.blockedContainer.setOnClickListener {
            navController.navigate(R.id.action_settingsFragment_to_blockedUsersFragment)
        }
        binding.privacyContainer.setOnClickListener {
            val intent = Intent(activity, PrivacyOrTermsConditionsActivity::class.java)
            intent.putExtra("type", "privacy")
            startActivity(intent)
        }
        binding.termsContainer.setOnClickListener {
            val intent = Intent(activity, PrivacyOrTermsConditionsActivity::class.java)
            intent.putExtra("type", "terms_and_conditions")
            startActivity(intent)
        }
        binding.logoutContainer.setOnClickListener {

            lifecycleScope.launch(Dispatchers.IO) {
                App.userPreferences.clear()
            }
            viewModel.logOut(userId = userId, token = userToken) { startNewActivity() }
        }
//        binding.deleteContainer.setOnClickListener {
//            showDeleteProfile()
//        }
    }

//    private fun showDeleteProfile() {
//        requireContext().showAlertDialog(getString(R.string.yes), getString(R.string.delete_account), getString(R.string.are_you_sure), object : AlertDialogCallback {
//            override fun onNegativeButtonClick(dialog: DialogInterface) {
//                dialog.dismiss()
//            }
//
//            override fun onPositiveButtonClick(dialog: DialogInterface) {
//                showProgressView()
//                deleteAccount()
//            }
//        })
//    }
//
//    private fun deleteAccount() {
//        lifecycleScope.launch(Dispatchers.Main) {
//            val chatUserId = getChatUserId()!!
//            val dialogIds = ArrayList(QbDialogHolder.dialogsMap.values).map { it.chatDialog }.map { it.dialogId }
//
//            viewModel.deleteAllDialogs(qbChatUserId = chatUserId, dialogIds) {
//
//                viewModel.logOut(userId = userId, token = userToken) {
//
//                    lifecycleScope.launch(Dispatchers.Main) {
//                        when (val response = viewModel.deleteProfile(userId, token = userToken)) {
//                            is Resource.Success -> {
//                                hideProgressView()
//                                startNewActivity()
//                            }
//                            is Resource.Error -> {
//                                hideProgressView()
//                                Timber.e("${response.message}")
//                                binding.root.snackbar("${response.message}")
//                            }
//                        }
//                    }
//
//                }
//
//            }
//        }
//    }


    private fun startNewActivity() {
        lifecycleScope.launch(Dispatchers.Main) {
            userPreferences.clear()
            val intent = Intent(requireActivity(), SplashActivity::class.java)
            requireActivity().startActivity(intent)
            requireActivity().finishAffinity()
        }
    }

}