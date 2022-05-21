package com.i69app.ui.screens.main.profile.subitems

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.Gson
import com.i69app.data.models.User
import com.i69app.databinding.FragmentUserProfileInterestsBinding
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.views.TagsCloudView
import com.i69app.utils.EXTRA_USER_MODEL
import com.i69app.utils.setVisibility
import timber.log.Timber

class UserProfileInterestsFragment : BaseFragment<FragmentUserProfileInterestsBinding>() {

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentUserProfileInterestsBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        val user = Gson().fromJson(arguments?.getString(EXTRA_USER_MODEL), User::class.java) ?: return
        setupUserData(user)
    }

    override fun setupClickListeners() {
    }

    private fun setupUserData(user: User) {
        setupTagsLayout(binding.userMusic, binding.userMusicLayout, (if (!user.music.isNullOrEmpty()) user.music else null))
        setupTagsLayout(binding.userMovies, binding.userMoviesLayout, (if (!user.movies.isNullOrEmpty()) user.movies else null))
        setupTagsLayout(binding.userTvShows, binding.userTvShowsLayout, (if (!user.tvShows.isNullOrEmpty()) user.tvShows else null))
        setupTagsLayout(binding.userSportTeams, binding.userSportTeamsLayout, (if (!user.sportsTeams.isNullOrEmpty()) user.sportsTeams else null))
    }

    private fun setupTagsLayout(cloud: TagsCloudView, layout: View, items: List<String>?) {
        Timber.d("List ${items?.size}")
        val musicVisibility = !items.isNullOrEmpty()
        if (musicVisibility) cloud.setTags(items!!)
        layout.setVisibility(musicVisibility)
    }

}