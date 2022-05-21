package com.i69app.ui.screens.main.search.userProfile

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.i69app.data.config.Constants
import com.i69app.data.models.Photo
import com.i69app.databinding.ImagesliderFragmentBinding
import com.i69app.ui.adapters.ImageSliderAdapter
import com.i69app.ui.base.BaseActivity
import java.lang.reflect.Type
import java.util.*

fun getimageSliderIntent(context: Context, datas: String,pos: Int) = Intent(context, ImageSliderActivity::class.java).apply {
    putExtra(Constants.EXTRA_IMG_SLIDER, datas)
    putExtra(Constants.SLIDER_POSITION, pos)

}


class ImageSliderActivity : BaseActivity<ImagesliderFragmentBinding>() {

    lateinit var adapter : ImageSliderAdapter

    var pos = 0

    private var Data_: ArrayList<Photo> = ArrayList()


    var avatarPhotos: MutableList<Photo>? = mutableListOf()

    override fun getActivityBinding(inflater: LayoutInflater) = ImagesliderFragmentBinding.inflate(inflater)

    override fun setupTheme() {


        val type: Type = object : TypeToken<List<Photo?>?>() {}.getType()
        Data_ = Gson().fromJson(intent.getStringExtra(Constants.EXTRA_IMG_SLIDER), type)

        pos = intent.getIntExtra(Constants.SLIDER_POSITION,0)


        adapter = ImageSliderAdapter(supportFragmentManager, Data_)
        binding.container.adapter = adapter

        binding.recyclerTabLayout.setupWithViewPager(binding.container, true)
        binding.container.currentItem = pos


        binding.toolbarHamburger.setOnClickListener(View.OnClickListener {
            finish()
        })
    }

    override fun setupClickListeners() {
    }
}