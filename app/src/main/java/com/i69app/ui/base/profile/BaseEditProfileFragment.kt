package com.i69app.ui.base.profile

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.ConcatAdapter
import com.i69app.BuildConfig
import net.cachapa.expandablelayout.ExpandableLayout
import com.i69app.R
import com.i69app.data.config.Constants.INTEREST_MOVIE
import com.i69app.data.config.Constants.INTEREST_MUSIC
import com.i69app.data.config.Constants.INTEREST_SPORT_TEAM
import com.i69app.data.config.Constants.INTEREST_TV_SHOW
import com.i69app.data.models.Photo
import com.i69app.data.models.User
import com.i69app.data.remote.responses.DefaultPicker
import com.i69app.databinding.FragmentEditProfileBinding
import com.i69app.ui.adapters.AddPhotoAdapter
import com.i69app.ui.adapters.PhotosAdapter
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.screens.ImagePickerActivity
import com.i69app.ui.screens.interest.getInterestsListActivityIntent
import com.i69app.ui.screens.main.profile.UserEditProfileFragment
import com.i69app.ui.views.InterestsView
import com.i69app.ui.views.ToggleImageView
import com.i69app.utils.snackbar
import timber.log.Timber

abstract class BaseEditProfileFragment : BaseFragment<FragmentEditProfileBinding>(),PhotosAdapter.PhotoAdapterListener {

    lateinit var photosAdapter: PhotosAdapter
    var defaultPicker: DefaultPicker? = null
    var user_assign: User? = null
    var avtarindex = 0
    private var profilePictureAmt: Int = 50


    private val photosLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            val data = activityResult.data
            if (activityResult.resultCode == Activity.RESULT_OK) {
                val result = data?.getStringExtra("result")
                Timber.d("Result $result")

                if (result != null) {
                    photosAdapter.addItem(result)
                    avtarindex = avtarindex + 1
                }
            }
        }

    private val interestedInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            val data = activityResult.data
            if (activityResult.resultCode == Activity.RESULT_OK) {
                val interestType =
                    data?.getIntExtra(com.i69app.data.config.Constants.EXTRA_INTEREST_TYPE, -1)
                val interestValue =
                    data?.getStringArrayListExtra(com.i69app.data.config.Constants.EXTRA_INTEREST_VALUE)
                when (interestType) {
                    INTEREST_MUSIC -> interestValue?.let { binding.music.setInterests(it) }
                    INTEREST_MOVIE -> interestValue?.let { binding.movies.setInterests(it) }
                    INTEREST_TV_SHOW -> interestValue?.let { binding.tvShows.setInterests(it) }
                    INTEREST_SPORT_TEAM -> interestValue?.let { binding.sportTeams.setInterests(it) }
                }
                if (interestType != null && interestValue != null) setInterestedInToViewModel(
                    interestType,
                    interestValue
                )
            }
        }

    abstract fun callparentmethod(pos: Int, photo_url: String)

    abstract fun showBuyDialog(photoQuota: Int, coinSpendAmt: Int)


    abstract fun setupScreen()

    abstract fun getInterestedInValues(interestsType: Int): List<String>

    abstract fun setInterestedInToViewModel(interestType: Int, interestValue: List<String>)

    abstract fun onDoneClick(increment: Boolean)

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentEditProfileBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        binding.toolbar.inflateMenu(R.menu.done_menu)
        binding.toolbar.setNavigationIcon(R.drawable.ic_keyboard_right_arrow)
        binding.toolbar.setNavigationOnClickListener { moveUp() }
        initGroups()
        initTags()
        initPhotoGallery()
        setupScreen()

    }

    override fun setupClickListeners() {
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_done) onDoneClick(false)
            false
        }
    }

    private fun initGroups() {
        initExpandableLayout(binding.groupsExpand, binding.toggleGroupsExpand, binding.groups)
        initExpandableLayout(
            binding.interestsExpand,
            binding.toggleInterestsExpand,
            binding.interests
        )
    }

    private fun initExpandableLayout(
        button: View,
        toggleImageView: ToggleImageView,
        expandableLayout: ExpandableLayout
    ) {
        toggleImageView.onCheckedChangeListener =
            CompoundButton.OnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    expandableLayout.expand(false)
                    binding.scrollContainer.post {
                        binding.scrollContainer.smoothScrollTo(0, expandableLayout.bottom)
                    }
                } else {
                    expandableLayout.collapse()
                }
            }

        button.setOnClickListener {
            toggleImageView.toggle()
        }
    }

    private fun initTags() {
        initInterestsView(binding.music, INTEREST_MUSIC)
        initInterestsView(binding.movies, INTEREST_MOVIE)
        initInterestsView(binding.tvShows, INTEREST_TV_SHOW)
        initInterestsView(binding.sportTeams, INTEREST_SPORT_TEAM)
    }

    private fun initInterestsView(chips: InterestsView, interestsType: Int) {
        chips.setOnAddButtonClickListener {
            val interestedInValues = getInterestedInValues(interestsType)

            interestedInLauncher.launch(
                getInterestsListActivityIntent(
                    requireContext(),
                    interestsType,
                    interestedInValues
                )
            )
        }
    }

    private fun initPhotoGallery() {
        val addPhotosAdapter = AddPhotoAdapter {

            val limit = user_assign!!.photosQuota

            if (avtarindex < limit) {

                val intent = Intent(requireActivity(), ImagePickerActivity::class.java)
                photosLauncher.launch(intent)
            } else {

                showBuyDialog(user_assign!!.photosQuota,profilePictureAmt)


            }


        }
        photosAdapter = PhotosAdapter { position, photourl ->

            photosAdapter.removeItem(position)
            callparentmethod(position, photourl)


            if (!photourl.startsWith(BuildConfig.BASE_URL)) {
                avtarindex = avtarindex - 1
            }

        }

        val concatAdapter = ConcatAdapter()
        concatAdapter.addAdapter(0, addPhotosAdapter)
        concatAdapter.addAdapter(1, photosAdapter)

        binding.photosRecycler.adapter = concatAdapter
    }

    protected fun prefillEditProfile(user: User) {

        user_assign = user
        avtarindex = user_assign!!.avatarPhotos!!.size

        val photos = user.avatarPhotos?.map {
            it.url.replace(
                "http://95.216.208.1:8000/media/",
                "${BuildConfig.BASE_URL}media/"
            )
        }
        photos?.let {
            photosAdapter.updateList(it)
        }

        user.music?.let {
            binding.music.setInterests(it)
        }
        user.movies?.let {
            binding.movies.setInterests(it)
        }
        user.tvShows?.let {
            binding.tvShows.setInterests(it)
        }
        user.sportsTeams?.let {
            binding.sportTeams.setInterests(it)
        }
        if (photosAdapter != null) {
            photosAdapter.avtar_index = user.avatarIndex!!
            photosAdapter.notifyDataSetChanged()

        }

    }

    protected fun isProfileValid(isLogin: Boolean = false): Boolean {
        if (photosAdapter.photos.isNullOrEmpty()) {
            binding.root.snackbar(getString(R.string.photo_error))
            return false
        }
        if (isLogin && photosAdapter.photos.size > 3) {
            binding.root.snackbar(getString(R.string.max_photo_login_error))
            return false
        }
        if (binding.editProfileName.text.isNullOrEmpty()) {
            binding.root.snackbar(getString(R.string.name_cannot_be_empty))
            return false
        }
        if (binding.genderPicker.selectedItemPosition == -1) {
            binding.root.snackbar(getString(R.string.gender_cannot_be_empty))
            return false
        }
        if (binding.agePicker.selectedItemPosition == -1) {
            binding.root.snackbar(getString(R.string.age_cannot_be_empty))
            return false
        }
        if (binding.heightPicker.selectedItemPosition == -1) {
            binding.root.snackbar(getString(R.string.height_cannot_be_empty))
            return false
        }
        return true
    }

    protected fun getViewModelUser(
        user: User,
        login: Boolean = false,
        increment: Boolean = false
    ): User {
        val photos = ArrayList<Photo>()
        photosAdapter.photos.forEach { photo ->
            photos.add(Photo(id = "1", url = photo))
        }
        if (login) user.purchaseCoins = 50
        if (increment) user.photosQuota = user.photosQuota + 1
        user.avatarPhotos = photos
        return user
    }

    protected fun getApiUser(user: User): User {
        try {
            user.age?.let {
                user.age = defaultPicker!!.agePicker[it].id
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            user.avatarIndex?.let {
                user.avatarIndex = photosAdapter.avtar_index
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            user.ethnicity?.let {
                user.ethnicity = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            user.familyPlans?.let {
                user.familyPlans = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            user.height?.let {
                user.height = defaultPicker!!.heightsPicker[it].id
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            user.politics?.let {
                user.politics = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            user.religion?.let {
                user.religion = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            user.zodiacSign?.let {
                user.zodiacSign = defaultPicker!!.zodiacSignPicker[it].id
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return user
    }
}