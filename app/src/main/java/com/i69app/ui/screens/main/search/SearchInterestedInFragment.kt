package com.i69app.ui.screens.main.search

import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.apollographql.apollo3.exception.ApolloException
import com.i69app.GetNotificationCountQuery
import dagger.hilt.android.AndroidEntryPoint
import com.i69app.R
import com.i69app.data.enums.InterestedInGender
import com.i69app.ui.adapters.SearchInterestedAdapter
import com.i69app.ui.base.search.BaseSearchFragment
import com.i69app.utils.snackbar
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class SearchInterestedInFragment : BaseSearchFragment() {

    companion object {
        fun setShowAnim(show: Boolean) {
            showAnim = show
        }
    }


    override fun setScreenTitle() {
        binding.title.text = getString(R.string.interested_in)
    }



    override fun initDrawerStatus() {
        try {
            getMainActivity().enableNavigationDrawer()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItems(): List<SearchInterestedAdapter.MenuItem> = listOf(
        SearchInterestedAdapter.MenuItem(R.string.serious_relationship, R.drawable.ic_relationship),
        SearchInterestedAdapter.MenuItem(R.string.casual_dating, R.drawable.ic_causal_menu_dating),
        SearchInterestedAdapter.MenuItem(R.string.new_friends, R.drawable.ic_new_friend_unchecked),
        SearchInterestedAdapter.MenuItem(R.string.roommates_2_lines, R.drawable.ic_roommates_unchecked),
        SearchInterestedAdapter.MenuItem(R.string.business_contacts, R.drawable.ic_business_contacts_unchecked)
    )

    override fun onAdapterItemClick(pos: Int) {
        val item = when (pos) {
            0 -> com.i69app.data.enums.InterestedInGender.SERIOUS_RELATIONSHIP_ONLY_MALE
            1 -> com.i69app.data.enums.InterestedInGender.CAUSAL_DATING_ONLY_MALE
            2 -> com.i69app.data.enums.InterestedInGender.NEW_FRIENDS_ONLY_MALE
            3 -> com.i69app.data.enums.InterestedInGender.ROOM_MATES_ONLY_MALE
            4 -> com.i69app.data.enums.InterestedInGender.BUSINESS_CONTACTS_ONLY_MALE
            else -> com.i69app.data.enums.InterestedInGender.SERIOUS_RELATIONSHIP_ONLY_MALE
        }
        navController.navigate(R.id.action_searchInterestedInFragment_to_searchGenderFragment, bundleOf("interested_in" to item))
    }

}