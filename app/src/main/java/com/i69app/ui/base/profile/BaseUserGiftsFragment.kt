package com.i69app.ui.base.profile

import android.util.TypedValue
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.i69app.R
import com.i69app.databinding.FragmentGiftsBinding
import com.i69app.gifts.FragmentReceiveGifts
import com.i69app.gifts.FragmentSendGifts
import com.i69app.ui.adapters.UserItemsAdapter
import com.i69app.ui.base.BaseFragment


abstract class BaseUserGiftsFragment : BaseFragment<FragmentGiftsBinding>() {

    var fragSendGifts: FragmentSendGifts? = null
    var fragReceiveGifts: FragmentReceiveGifts? = null
    abstract fun getUserId(): String
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
        setupTabIcons()
    }

    private fun setupTabIcons() {
        for (i in 0 until binding.giftsTabs.tabCount) {
            val customTab = binding.giftsTabs.getTabAt(i)?.view
            val tabText = customTab?.getChildAt(1) as TextView?
            //tabText.text = "  " + binding.giftsTabs.getTabAt(i)?.text
            tabText?.compoundDrawablePadding =
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, resources.displayMetrics)
                    .toInt()
            tabText?.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_pink_gift_svg, 0, 0, 0)
            //binding.giftsTabs.getTabAt(i)?.customView = tabText
        }
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = UserItemsAdapter(childFragmentManager)
        fragReceiveGifts = FragmentReceiveGifts(getUserId())
        fragSendGifts = FragmentSendGifts(getUserId())
        adapter.addFragItem(fragReceiveGifts!!, getString(R.string.received_gift))
        adapter.addFragItem(fragSendGifts!!, getString(R.string.send_gift))
        viewPager.adapter = adapter
    }
}