package com.i69app.ui.base.profile

import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.i69app.R
import com.i69app.databinding.FragmentGiftsBinding
import com.i69app.ui.adapters.UserItemsAdapter
import com.i69app.ui.base.BaseFragment
import com.i69app.gifts.FragmentRealGifts
import com.i69app.gifts.FragmentVirtualGifts

abstract class BaseGiftsFragment: BaseFragment<FragmentGiftsBinding>() {

    var fragVirtualGifts: FragmentVirtualGifts ?= null
    var fragRealGifts: FragmentRealGifts ?= null

    abstract fun setupScreen()

    override fun setupTheme() {
        binding.giftsTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
                binding.giftsPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
            }
        })
        binding.giftsTabs.setupWithViewPager(binding.giftsPager)
        setupScreen()
        setupViewPager(binding.giftsPager)
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = UserItemsAdapter(childFragmentManager)
        fragRealGifts = FragmentRealGifts()
        fragVirtualGifts = FragmentVirtualGifts()

        adapter.addFragItem(fragRealGifts!!, getString(R.string.real_gifts))
        adapter.addFragItem(fragVirtualGifts!!, getString(R.string.virtual_gifts))
        viewPager.adapter = adapter
    }

}