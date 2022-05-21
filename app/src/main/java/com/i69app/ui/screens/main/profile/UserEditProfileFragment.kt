package com.i69app.ui.screens.main.profile

import android.content.DialogInterface
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.i69app.BuildConfig
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.i69app.R
import com.i69app.data.config.Constants.INTEREST_MOVIE
import com.i69app.data.config.Constants.INTEREST_MUSIC
import com.i69app.data.config.Constants.INTEREST_SPORT_TEAM
import com.i69app.data.config.Constants.INTEREST_TV_SHOW
import com.i69app.data.models.Photo
import com.i69app.data.models.User
import com.i69app.ui.base.profile.BaseEditProfileFragment
import com.i69app.ui.interfaces.AlertDialogCallback
import com.i69app.ui.viewModels.UserViewModel
import com.i69app.utils.Resource
import com.i69app.utils.showAlertDialog
import com.i69app.utils.snackbar
import kotlinx.coroutines.withContext
import timber.log.Timber

@AndroidEntryPoint
class UserEditProfileFragment : BaseEditProfileFragment() {

    private val viewModel: UserViewModel by activityViewModels()
    private var profilePictureAmt: Int = 50
    var user: User? = null
    private var token: String? = null

    var Removed_Photos: ArrayList<Photo> = java.util.ArrayList()


    override fun setupScreen() {
        binding.titleLabel.text = getString(R.string.profile_edit_title)

        Removed_Photos = ArrayList()


        lifecycleScope.launch(Dispatchers.Main) {
            token = getCurrentUserToken()!!

            viewModel.getCoinSettings(token!!).observe(viewLifecycleOwner, {
                it?.let { coinSettings ->
                    coinSettings.forEach { coinSetting ->
                        if (coinSetting.method == com.i69app.data.enums.DeductCoinMethod.PROFILE_PICTURE.name) profilePictureAmt =
                            coinSetting.coinsNeeded
                    }
                }
            })

            viewModel.getDefaultPickers(token!!).observe(viewLifecycleOwner, {
                it?.let { defaultPickerValue ->
                    defaultPicker = defaultPickerValue
                    binding.defaultPicker = defaultPicker!!
                }
            })

            viewModel.getCurrentUser(getCurrentUserId()!!, token = token!!, true)
                .observe(viewLifecycleOwner, { userDetails ->
                    userDetails?.let {
                        user = it.copy()
                        binding.user = user!!
                        prefillEditProfile(user!!)
                    }
                })
        }
    }

    override fun getInterestedInValues(interestsType: Int): List<String> = when (interestsType) {
        INTEREST_MUSIC -> if (user?.music.isNullOrEmpty()) emptyList() else user?.music!!
        INTEREST_MOVIE -> if (user?.movies.isNullOrEmpty()) emptyList() else user?.movies!!
        INTEREST_TV_SHOW -> if (user?.tvShows.isNullOrEmpty()) emptyList() else user?.tvShows!!
        INTEREST_SPORT_TEAM -> if (user?.sportsTeams.isNullOrEmpty()) emptyList() else user?.sportsTeams!!
        else -> if (user?.sportsTeams.isNullOrEmpty()) emptyList() else user?.sportsTeams!!
    }


    override fun setInterestedInToViewModel(interestType: Int, interestValue: List<String>) {
        when (interestType) {
            INTEREST_MUSIC -> user?.music = interestValue.toMutableList()
            INTEREST_MOVIE -> user?.movies = interestValue.toMutableList()
            INTEREST_TV_SHOW -> user?.tvShows = interestValue.toMutableList()
            INTEREST_SPORT_TEAM -> user?.sportsTeams = interestValue.toMutableList()
        }
    }

    override fun callparentmethod(pos: Int, photo_url: String) {


        if (photo_url.startsWith(BuildConfig.BASE_URL)) {

            val previousPhotos = user!!.avatarPhotos!!

            previousPhotos.forEach { photo ->
                if (photo.url.replace(
                        "http://95.216.208.1:8000/media/",
                        "${BuildConfig.BASE_URL}media/"
                    ).equals(photo_url)
                ) {
                    Removed_Photos.add(photo)
                }
            }
//
//
//            Removed_Photos.add(photosAdapter.photos[position].id)
        }
    }


    override fun onDoneClick(increment: Boolean) {
        if (!isProfileValid()) return
        val previousPhotos = user!!.avatarPhotos!!

        user = getViewModelUser(user!!, increment = increment)
        val userData = getApiUser(user!!.copy())
        Timber.e("USER: $userData")
        if (photosAdapter.photos.size > userData.photosQuota) {
            showBuyDialog(photoQuota = userData.photosQuota, coinSpendAmt = profilePictureAmt)
            return
        }
        showProgressView()
        lifecycleScope.launch(Dispatchers.IO) {
            val newPhotosFilePath =
                photosAdapter.photos.filter { !it.startsWith(BuildConfig.BASE_URL) }
            val newUrlPhotos = photosAdapter.photos.filter { it.startsWith(BuildConfig.BASE_URL) }


            val toRemovePhotoIds = previousPhotos
                .filter { !newUrlPhotos.contains(it.url) }
                .map { it.id }

            if (!newPhotosFilePath.isNullOrEmpty()) {
                newPhotosFilePath.forEach { photo ->
                    val res = viewModel.uploadImage(userId = userData.id, token!!, filePath = photo)
                    Log.d("newPhotos", "" + res.data.toString())
                }
            }

            if (!toRemovePhotoIds.isNullOrEmpty()) {
                toRemovePhotoIds.forEach { photoId ->

                    Removed_Photos.forEach { photo ->

                        if (photo.id.equals(photoId)) {

                            val res1 =
                                viewModel.deleteUserPhotos(token = token!!, photoId = photoId)
                            Log.d("RemovePhoto", "" + res1.data.toString())

                        }

                    }

                }
            }
            Log.d("avatarPhotosINDEX", "" + photosAdapter.avtar_index)

            withContext(Dispatchers.Main) {
                when (val response = viewModel.updateProfile(user = userData, token = token!!)) {
                    is Resource.Success -> {
                        viewModel.getCurrentUser(userId = user!!.id, token = token!!, true)
                        hideProgressView()
                        requireActivity().onBackPressed()
                    }
                    is Resource.Error -> {
                        hideProgressView()
                        Timber.e("${response.message}")
                        binding.root.snackbar("${response.message}")
                    }
                }
            }
        }
    }

    override fun onRemoveBtnClick(ids: Int, s: String) {
        binding.root.snackbar("Clicked " + ids)
//        lifecycleScope.launch(Dispatchers.IO) {
//
//                    viewModel.deleteUserPhotos(token = token!!, photoId = ids.toString())
//
//        }
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
                    if (coinSpendAmt > user!!.purchaseCoins) {
                        findNavController().navigate(R.id.actionGoToPurchaseFragment)

                    } else {
                        lifecycleScope.launch(Dispatchers.Main) {
                            when (val response = viewModel.deductCoin(
                                userId = user!!.id,
                                token = token!!,
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


}