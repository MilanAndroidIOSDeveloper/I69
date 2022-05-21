package com.i69app.ui.screens.main.search.userProfile

import android.os.Bundle
import android.view.LayoutInflater

import android.view.ViewGroup
import com.i69app.BuildConfig
import com.i69app.data.models.Photo

import com.i69app.databinding.ImagesliderItemBinding
import com.i69app.ui.base.BaseFragment
import com.i69app.utils.loadImage

class ImageFragment : BaseFragment<ImagesliderItemBinding>() {


    companion object {

        // Method for creating new instances of the fragment
        fun newInstance(imgdata: Photo): ImageFragment {

            // Store the movie data in a Bundle object
            val args = Bundle()
            args.putString("ImgId",imgdata.id)
            args.putString("ImgUrl",imgdata.url)


            // Create a new MovieFragment and set the Bundle as the arguments
            // to be retrieved and displayed when the view is created
            val fragment = ImageFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = ImagesliderItemBinding.inflate(inflater, container, false).apply {

    }

    override fun setupTheme() {

        val args = arguments

        binding.imageView.loadImage(
            args!!.getString("ImgUrl")!!.replace("http://95.216.208.1:8000/media/","${BuildConfig.BASE_URL}media/"))
//        titleTextView.text = args.getString(MovieHelper.KEY_TITLE)
//        ratingTextView.text = String.format("%d/10", args.getInt(MovieHelper.KEY_RATING))
//        overviewTextView.text = args.getString(MovieHelper.KEY_OVERVIEW)
//
//        // Download the image and display it using Picasso
//        Picasso.with(activity)
//            .load(
//                resources.getIdentifier(
//                    args.getString(""),
//                    "drawable",
//                    activity.packageName
//                )
//            )
//            .into(posterImageView)
    }

    override fun setupClickListeners() {
    }
}