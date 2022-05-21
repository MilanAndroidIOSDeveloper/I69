package com.i69app.ui.screens.main.search.userProfile

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.exception.ApolloException
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textview.MaterialTextView
import com.i69app.*
import com.i69app.R
import com.i69app.data.models.ModelGifts
import com.i69app.di.modules.AppModule
import com.i69app.gifts.FragmentRealGifts
import com.i69app.gifts.FragmentVirtualGifts
import com.i69app.ui.adapters.StoryCommentListAdapter
import com.i69app.ui.adapters.StoryLikesAdapter
import com.i69app.ui.adapters.UserItemsAdapter
import com.i69app.ui.screens.main.MainActivity
import com.i69app.ui.screens.main.search.userProfile.SearchUserProfileFragment
import com.i69app.ui.viewModels.CommentsModel
import com.i69app.ui.viewModels.ReplysModel
import com.i69app.utils.*
import timber.log.Timber
import java.util.ArrayList


class PicViewerFragment : DialogFragment(){

    private lateinit var views: View



    private lateinit var loadingDialog: Dialog

    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var isPlayerPlaying = true

    private var exoPlayer: SimpleExoPlayer? = null
    private lateinit var dataSourceFactory: DataSource.Factory


    var player_view: PlayerView? = null


    override fun getTheme(): Int {
        return R.style.DialogTheme
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        views = inflater.inflate(R.layout.fragment_pic_viewer, container, false)

        return views
    }


    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Timber.d("onDestroyView")
        if (exoPlayer != null){
            exoPlayer!!.playWhenReady = false
            releasePlayer()
        }
        dismiss()
    }



    override fun onStart() {
        super.onStart()
        loadingDialog = requireContext().createLoadingDialog()
        player_view = views.findViewById<PlayerView>(com.i69app.R.id.player_view)
        player_view!!.setShutterBackgroundColor(Color.TRANSPARENT);
        player_view!!.setKeepContentOnPlayerReset(true)
        dataSourceFactory = DefaultDataSourceFactory(requireActivity(), Util.getUserAgent(requireActivity(), "i69"))
        val imgUserStory = views.findViewById<ImageView>(R.id.imgUserStory)
        val img_close = views.findViewById<ImageView>(com.i69app.R.id.img_close)

        val type= arguments?.getString("mediatype")
        val url = arguments?.getString("url", "")

        if(type.equals("image"))
        {
            imgUserStory.visibility = View.VISIBLE
            if(!url.equals(""))
            {
                imgUserStory.loadImage(url!!)

            }
            else
            {
                imgUserStory.loadImage(R.drawable.ic_default_user)

            }


        }
        else if(type.equals("video"))
        {
            player_view!!.visibility = View.VISIBLE
            val uri: Uri = Uri.parse(url)
            //val uri: Uri = Uri.fromFile(File(arguments?.getString("path")))


            val mediaItem = MediaItem.Builder()
                .setUri(uri)
                .setMimeType(MimeTypes.VIDEO_MP4)
                .build()



            playView(mediaItem)
        }






        img_close.setOnClickListener(View.OnClickListener {

            if (exoPlayer != null){
                exoPlayer!!.playWhenReady = false
                releasePlayer()
            }
            dismiss()
        })
    }

    protected fun showProgressView() {
        loadingDialog.show()
    }

    protected fun hideProgressView() {
        loadingDialog.dismiss()
    }

    private fun playView(mediaItem: MediaItem) {

        showProgressView()

        exoPlayer = SimpleExoPlayer.Builder(requireActivity()).build().apply {
            playWhenReady = isPlayerPlaying
            seekTo(currentWindow, playbackPosition)
            setMediaItem(mediaItem, false)
            prepare()
        }
        player_view!!.player = exoPlayer
        var durationSet = false

        exoPlayer!!.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == ExoPlayer.STATE_READY && !durationSet) {
                    hideProgressView()
//                    val realDurationMillis: Long = exoPlayer!!.getDuration()
//                    durationSet = true
//                    val duration = realDurationMillis
//                    Timber.d("filee ${duration}")
                }
            }

            fun onPlayWhenReadyCommitted() {
                // No op.

            }

            override fun onPlayerError(error: ExoPlaybackException) {
                // No op.
            }
        })


    }


    private fun releasePlayer(){
        isPlayerPlaying = exoPlayer!!.playWhenReady
        playbackPosition = exoPlayer!!.currentPosition
        currentWindow = exoPlayer!!.currentWindowIndex
        exoPlayer!!.release()
    }








    }







