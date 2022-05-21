package com.i69app.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.i69app.data.models.Photo
import com.i69app.ui.screens.main.search.userProfile.ImageFragment

class ImageSliderAdapter(fragmentManager: FragmentManager, private val movies: MutableList<Photo>?) :
    FragmentStatePagerAdapter(fragmentManager) {

//    private val MAX_VALUE = 200



    override fun getItem(position: Int): Fragment {
        return ImageFragment.newInstance(movies!![position])
    }


    override fun getCount(): Int {
        return movies!!.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return movies!![position].id.toString()
    }

}