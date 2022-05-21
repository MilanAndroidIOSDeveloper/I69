package com.i69app.ui.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.i69app.ui.screens.main.search.result.PageSearchResultFragment

class PageResultAdapter(fragment: Fragment, private val tabTitles: Array<String>) : FragmentStateAdapter(fragment) {

    override fun createFragment(position: Int): Fragment = PageSearchResultFragment.newInstance(position)

    override fun getItemCount(): Int = tabTitles.size

}