package com.i69app.ui.screens.main.search

import androidx.core.os.bundleOf
import dagger.hilt.android.AndroidEntryPoint
import com.i69app.R
import com.i69app.data.enums.InterestedInGender
import com.i69app.data.enums.InterestedInGender.*
import com.i69app.ui.adapters.SearchInterestedAdapter
import com.i69app.ui.base.search.BaseSearchBackFragment
import com.i69app.ui.base.search.BaseSearchFragment

@AndroidEntryPoint
class SearchGenderFragment : BaseSearchBackFragment() {

    override fun setScreenTitle() {
        binding.title.text = getString(R.string.gender_label)
    }

    override fun initDrawerStatus() {
        getMainActivity().disableNavigationDrawer()
    }

    override fun getItems(): List<SearchInterestedAdapter.MenuItem> = listOf(
        SearchInterestedAdapter.MenuItem(R.string.man, R.drawable.ic_man_unchecked),
        SearchInterestedAdapter.MenuItem(R.string.woman, R.drawable.ic_woman_unchecked),
        SearchInterestedAdapter.MenuItem(R.string.both, R.drawable.ic_both_unchecked)
    )

    override fun onAdapterItemClick(pos: Int) {
        val interestedIn = getInterestedIn(pos)
        navController.navigate(R.id.action_searchGenderFragment_to_searchFiltersFragment, bundleOf("interested_in" to interestedIn))
    }

    private fun getInterestedIn(pos: Int) = when (arguments?.getSerializable("interested_in") as com.i69app.data.enums.InterestedInGender) {
        SERIOUS_RELATIONSHIP_ONLY_MALE -> when (pos) {
            0 -> SERIOUS_RELATIONSHIP_ONLY_MALE
            1 -> SERIOUS_RELATIONSHIP_ONLY_FEMALE
            else -> SERIOUS_RELATIONSHIP_BOTH
        }
        CAUSAL_DATING_ONLY_MALE -> when (pos) {
            0 -> CAUSAL_DATING_ONLY_MALE
            1 -> CAUSAL_DATING_ONLY_FEMALE
            else -> CAUSAL_DATING_BOTH
        }
        NEW_FRIENDS_ONLY_MALE -> when (pos) {
            0 -> NEW_FRIENDS_ONLY_MALE
            1 -> NEW_FRIENDS_ONLY_FEMALE
            else -> NEW_FRIENDS_BOTH
        }
        ROOM_MATES_ONLY_MALE -> when (pos) {
            0 -> ROOM_MATES_ONLY_MALE
            1 -> ROOM_MATES_ONLY_FEMALE
            else -> ROOM_MATES_BOTH
        }
        else -> when (pos) {
            0 -> BUSINESS_CONTACTS_ONLY_MALE
            1 -> BUSINESS_CONTACTS_ONLY_FEMALE
            else -> BUSINESS_CONTACTS_BOTH
        }
    }

}