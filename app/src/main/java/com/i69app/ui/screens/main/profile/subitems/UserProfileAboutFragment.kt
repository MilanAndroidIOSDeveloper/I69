package com.i69app.ui.screens.main.profile.subitems

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.i69app.data.models.IdWithValue
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.i69app.data.models.User
import com.i69app.data.remote.responses.DefaultPicker
import com.i69app.databinding.FragmentUserProfileAboutBinding
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.views.TagsCloudView
import com.i69app.utils.EXTRA_USER_MODEL
import com.i69app.utils.getSelectedValueFromDefaultPicker
import com.i69app.utils.isCurrentLanguageFrench
import com.i69app.utils.setVisibility
import timber.log.Timber

@AndroidEntryPoint
class UserProfileAboutFragment : BaseFragment<FragmentUserProfileAboutBinding>() {

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentUserProfileAboutBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        val user = Gson().fromJson(arguments?.getString(EXTRA_USER_MODEL), User::class.java) ?: return
        val defaultPickers = Gson().fromJson(arguments?.getString("default_picker"), DefaultPicker::class.java)
        setupUserData(user, defaultPickers)
    }

    override fun setupClickListeners() {
    }

    private fun setupUserData(user: User?, defaultPickers: DefaultPicker?) {
        setupUserAboutData(user)
        setupUserTagsData(user, defaultPickers)
        setupInterestData(user)
    }

    private fun setupUserAboutData(user: User?) {
        binding.userAbout.text = user?.about
        binding.userNameAboutSection.text = user?.fullName
        binding.userNameIntrestSection.text = user?.fullName

        //binding.userIntrest.text = user?.about
        //binding.userAboutLayout.setVisibility(!aboutLayoutVisibility)
    }

    private fun setupUserTagsData(user: User?, defaultPicker: DefaultPicker?) {
        if (user?.tags?.isNotEmpty() == true) {
            val list = ArrayList<String>()

            lifecycleScope.launch(Dispatchers.Main) {
                user.tags.forEach {
                    var value = ""
                    defaultPicker?.tagsPicker?.forEachIndexed { _, idWithValue ->
                        if (idWithValue.id == it) {
                            value = if (isCurrentLanguageFrench()) idWithValue.valueFr else idWithValue.value
                            binding.userTags.addChip(IdWithValue(idWithValue.id, idWithValue.value, idWithValue.valueFr))
                            return@forEachIndexed
                        }
                    }
                    if (value.isNotEmpty()) list.add(value)
                }

                withContext(Dispatchers.Main) {
                    user.tags.forEach {
                        binding.userTags.setSelectedChip(it)
                    }
                }
            }
        } else {
            //binding.userTagsLayout.visibility = View.GONE
        }
    }

    private fun setupInterestData(user: User?) {
        setupTagsLayout(binding.userMusic, binding.userMusicLayout, (if (!user?.music.isNullOrEmpty()) user?.music else null))
        setupTagsLayout(binding.userMovies, binding.userMoviesLayout, (if (!user?.movies.isNullOrEmpty()) user?.movies else null))
        setupTagsLayout(binding.userTvShows, binding.userTvShowsLayout, (if (!user?.tvShows.isNullOrEmpty()) user?.tvShows else null))
        setupTagsLayout(binding.userSportTeams, binding.userSportTeamsLayout, (if (!user?.sportsTeams.isNullOrEmpty()) user?.sportsTeams else null))
    }

    private fun setupTagsLayout(cloud: TagsCloudView, layout: View, items: List<String>?) {
        Timber.d("List ${items?.size}")
        val musicVisibility = !items.isNullOrEmpty()
        if (musicVisibility) cloud.setTags(items!!)
        layout.setVisibility(musicVisibility)
    }
}