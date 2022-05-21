package com.i69app.ui.screens.auth.profile

import android.content.DialogInterface
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.i69app.R
import com.i69app.data.config.Constants.INTEREST_MOVIE
import com.i69app.data.config.Constants.INTEREST_MUSIC
import com.i69app.data.config.Constants.INTEREST_SPORT_TEAM
import com.i69app.data.config.Constants.INTEREST_TV_SHOW
import com.i69app.data.models.User
import com.i69app.singleton.App
import com.i69app.ui.base.profile.BaseEditProfileFragment
import com.i69app.ui.interfaces.AlertDialogCallback
import com.i69app.ui.viewModels.AuthViewModel
import com.i69app.utils.Resource
import com.i69app.utils.showAlertDialog
import com.i69app.utils.snackbar
import timber.log.Timber

@AndroidEntryPoint
class CompleteProfileFragment : BaseEditProfileFragment() {

    private val viewModel: AuthViewModel by activityViewModels()


    override fun callparentmethod(pos: Int, photo_url: String) {

    }

    override fun showBuyDialog(photoQuota: Int, coinSpendAmt: Int) {
        requireContext().showAlertDialog(
            positionBtnText = getString(R.string.yes),
            title = getString(R.string.app_name),
            subTitle = String.format(
                getString(R.string.upload_image_warning),
                photoQuota,
                coinSpendAmt
            ),
            listener = object : AlertDialogCallback {
                override fun onNegativeButtonClick(dialog: DialogInterface) {
                    dialog.dismiss()
                }

                override fun onPositiveButtonClick(dialog: DialogInterface) {
                    dialog.dismiss()
                    if (coinSpendAmt > binding.user!!.purchaseCoins) {
                        findNavController().navigate(R.id.actionGoToPurchaseFragment)

                    } else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            when (val response = viewModel.deductCoin(
                                userId = binding.user!!.id,
                                token =viewModel.token!!,
                                com.i69app.data.enums.DeductCoinMethod.PROFILE_PICTURE
                            )) {
                                is Resource.Success -> onDoneClick(true)
                                is Resource.Error -> binding.root.snackbar("${getString(R.string.something_went_wrong)} ${response.message}")
                            }
                        }
                    }
                }
            }
        )
    }


    override fun setupScreen() {
        binding.titleLabel.text = getString(R.string.profile_complete_title)




        viewModel.getDefaultPickers(viewModel.token!!).observe(viewLifecycleOwner, {
            it?.let { defaultPickerValue ->
                defaultPicker = defaultPickerValue
                binding.defaultPicker = defaultPicker!!
            }
        })

        binding.user = viewModel.getAuthUser()!!
        prefillEditProfile(viewModel.getAuthUser()!!)
    }

    override fun getInterestedInValues(interestsType: Int): List<String> = when (interestsType) {
        INTEREST_MUSIC -> if (viewModel.getAuthUser()?.music.isNullOrEmpty()) emptyList() else viewModel.getAuthUser()?.music!!
        INTEREST_MOVIE -> if (viewModel.getAuthUser()?.movies.isNullOrEmpty()) emptyList() else viewModel.getAuthUser()?.movies!!
        INTEREST_TV_SHOW -> if (viewModel.getAuthUser()?.tvShows.isNullOrEmpty()) emptyList() else viewModel.getAuthUser()?.tvShows!!
        INTEREST_SPORT_TEAM -> if (viewModel.getAuthUser()?.sportsTeams.isNullOrEmpty()) emptyList() else viewModel.getAuthUser()?.sportsTeams!!
        else -> if (viewModel.getAuthUser()?.sportsTeams.isNullOrEmpty()) emptyList() else viewModel.getAuthUser()?.sportsTeams!!
    }

    override fun setInterestedInToViewModel(interestType: Int, interestValue: List<String>) {
        when (interestType) {
            INTEREST_MUSIC -> viewModel.getAuthUser()?.music = interestValue.toMutableList()
            INTEREST_MOVIE -> viewModel.getAuthUser()?.movies = interestValue.toMutableList()
            INTEREST_TV_SHOW -> viewModel.getAuthUser()?.tvShows = interestValue.toMutableList()
            INTEREST_SPORT_TEAM -> viewModel.getAuthUser()?.sportsTeams = interestValue.toMutableList()
        }
    }

    override fun onDoneClick(increment: Boolean) {
        if (!isProfileValid(isLogin = true)) return
        showProgressView()
        viewModel.setAuthUser(getViewModelUser(viewModel.getAuthUser()!!, login = true))
        val user = getApiUser(viewModel.getAuthUser()!!.copy())
        Timber.e("USER: $user")
        lifecycleScope.launch(Dispatchers.IO) {
            user.avatarPhotos?.forEach { photo ->
                Log.d("avatarPhotos",""+user.avatarPhotos)
                Log.d("avatarPhotosINDEX",""+user.avatarIndex)

                val res=viewModel.uploadImage(userId = user.id, viewModel.token!!, filePath = photo.url)
                Log.d("newPhotos",""+res.data.toString())

            }
            withContext(Dispatchers.Main) {
                when (val response = viewModel.updateProfile(user = user, viewModel.token!!)) {
                    is Resource.Success -> {
                        userPreferences.saveUserIdToken(userId = response.data!!.data!!.id, token = viewModel.token!!,user.fullName)
                        App.updateFirebaseToken(viewModel.userUpdateRepository)

                        App.updateOneSignal(viewModel.userUpdateRepository)
                        hideProgressView()
                        moveTo(CompleteProfileFragmentDirections.actionProfileCompleteToWelcome())
                    }
                    is Resource.Error -> {
                        hideProgressView()
                        Timber.e("${getString(R.string.sign_in_failed)} ${response.message}")
                        binding.root.snackbar("${getString(R.string.sign_in_failed)} ${response.message}")
                    }
                }
            }
        }
    }

    override fun onRemoveBtnClick(position: Int, s: String) {

    }


}