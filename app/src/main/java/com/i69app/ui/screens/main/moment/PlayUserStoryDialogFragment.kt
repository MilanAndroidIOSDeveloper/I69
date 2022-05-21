package com.i69app.ui.screens.main.moment

import android.app.ActivityManager
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.exception.ApolloException
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textview.MaterialTextView
import com.i69app.*
import com.i69app.BuildConfig
import com.i69app.R
import com.i69app.data.models.ModelGifts
import com.i69app.di.modules.AppModule
import com.i69app.gifts.FragmentRealGifts
import com.i69app.gifts.FragmentVirtualGifts
import com.i69app.singleton.App
import com.i69app.singleton.App.Companion.userPreferences
import com.i69app.ui.adapters.StoryCommentListAdapter
import com.i69app.ui.adapters.StoryLikesAdapter
import com.i69app.ui.adapters.UserItemsAdapter
import com.i69app.ui.adapters.VideoStoryCommentListAdapter
import com.i69app.ui.screens.SplashActivity
import com.i69app.ui.screens.main.search.userProfile.SearchUserProfileFragment
import com.i69app.ui.viewModels.CommentsModel
import com.i69app.ui.viewModels.ReplysModel
import com.i69app.ui.viewModels.UserViewModel
import com.i69app.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


const val STATE_RESUME_WINDOW = "resumeWindow"
const val STATE_RESUME_POSITION = "resumePosition"
const val STATE_PLAYER_FULLSCREEN = "playerFullscreen"
const val STATE_PLAYER_PLAYING = "playerOnPlay"
class PlayUserStoryDialogFragment : DialogFragment(), StoryCommentListAdapter.ClickPerformListener{


    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var isFullscreen = false
    private var isPlayerPlaying = true

    private var countUp: Int = 100
    private lateinit var loadingDialog: Dialog

    private lateinit var views: View
    var progressBar1: ProgressBar? = null


    private lateinit var exoPlayer: SimpleExoPlayer
    private lateinit var dataSourceFactory: DataSource.Factory


    var player_view: PlayerView? = null

    var adapters: StoryLikesAdapter? = null

    private var timer1: CountDownTimerExt? = null


    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var GiftbottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private lateinit var LikebottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private val viewModel: UserViewModel by activityViewModels()

    var txtNearbyUserLikeCount: MaterialTextView? = null
    var txtMomentRecentComment: MaterialTextView? = null
    var rvSharedMoments: RecyclerView? = null
    var nodata: MaterialTextView? = null

    var items: ArrayList<CommentsModel> = ArrayList()


    var rvLikes: RecyclerView? = null



    var no_data: MaterialTextView? = null
    var txtheaderlike: MaterialTextView? = null

    var giftUserid: String? = null

    var fragVirtualGifts: FragmentVirtualGifts?= null
    var fragRealGifts: FragmentRealGifts?= null

    var userToken: String? = null

    var Uid: String? = null

    var objectID: Int? = null


    var adapter: VideoStoryCommentListAdapter? = null

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        views = inflater.inflate(R.layout.fragment_user_story_detail, container, false)
        if (savedInstanceState != null) {
            currentWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW)
            playbackPosition = savedInstanceState.getLong(STATE_RESUME_POSITION)
            isFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN)
            isPlayerPlaying = savedInstanceState.getBoolean(STATE_PLAYER_PLAYING)
        }
        return views
    }


    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        pausetimerandplayer()
    }


    fun pausetimerandplayer()

    {
        if(timer1 != null)
        {
            timer1!!.pause()

        }
        if (exoPlayer != null){
            exoPlayer.playWhenReady = false
        }
    }

    fun resumetimerandplayer()
    {

        if(timer1 != null)
        {
            timer1!!.start()

        }

        if (exoPlayer != null){
            exoPlayer.playWhenReady = true

        }
    }


    override fun onStart() {
        super.onStart()




        loadingDialog = requireContext().createLoadingDialog()
        val userPic = views.findViewById<ImageView>(R.id.userPic)
        val lblName = views.findViewById<MaterialTextView>(R.id.lblName)
        val txtTimeAgo = views.findViewById<MaterialTextView>(R.id.txtTimeAgo)
        val sendgiftto = views.findViewById<MaterialTextView>(R.id.sendgiftto)
        progressBar1 = views.findViewById<ProgressBar>(R.id.progressBar1)
        val imgUserStory = views.findViewById<ImageView>(R.id.imgUserStory)
        val imgNearbyUserComment = views.findViewById<ImageView>(R.id.imgNearbyUserComment)
        val img_close = views.findViewById<ImageView>(com.i69app.R.id.img_close)
        val imgNearbyUserGift = views.findViewById<ImageView>(R.id.imgNearbyUserGift)
        val giftsTabs = views.findViewById<TabLayout>(R.id.giftsTabs)
        val giftsPager = views.findViewById<ViewPager>(R.id.gifts_pager)
        txtNearbyUserLikeCount = views.findViewById<MaterialTextView>(R.id.txtNearbyUserLikeCount)
        txtMomentRecentComment = views.findViewById<MaterialTextView>(R.id.txtMomentRecentComment)
        val thumbnail = views.findViewById<ImageView>(R.id.thumbnail)
        val send_btn = views.findViewById<ImageView>(R.id.send_btn)
        val msg_write = views.findViewById<EditText>(R.id.msg_write)
        val imgNearbyUserLikes = views.findViewById<ImageView>(R.id.imgNearbyUserLikes)
        rvSharedMoments = views.findViewById<RecyclerView>(R.id.rvSharedMoments)
        nodata = views.findViewById<MaterialTextView>(R.id.no_data)
        player_view = views.findViewById<PlayerView>(com.i69app.R.id.player_view)
        player_view!!.setShutterBackgroundColor(Color.TRANSPARENT);
        player_view!!.setKeepContentOnPlayerReset(true)
        dataSourceFactory = DefaultDataSourceFactory(requireActivity(), Util.getUserAgent(requireActivity(), "i69"))

        rvLikes = views.findViewById<RecyclerView>(R.id.rvLikes)
        no_data = views.findViewById<MaterialTextView>(R.id.no_datas)
        txtheaderlike = views.findViewById<MaterialTextView>(R.id.txtheaderlike)
        val likes_l = views.findViewById<LinearLayout>(R.id.likes_l)
        val comment_l = views.findViewById<LinearLayout>(R.id.comment_l)
        val gift_l = views.findViewById<LinearLayout>(R.id.gift_l)

        Uid = arguments?.getString("Uid","")
        val url = arguments?.getString("url", "")

        Timber.d("playview ${arguments?.getString("url")}")
        // Uri object to refer the
        // resource from the videoUrl

        val uri: Uri = Uri.parse(url)
        //val uri: Uri = Uri.fromFile(File(arguments?.getString("path")))


        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMimeType(MimeTypes.VIDEO_MP4)
            .build()



        playView(mediaItem)

        val userurl = arguments?.getString("userurl", "")
        val username = arguments?.getString("username", "")
        val times = arguments?.getString("times", "")
        userToken = arguments?.getString("token", "")
        objectID= arguments?.getInt("objectID", 0)


        getStories()





        player_view!!.visibility = View.VISIBLE
        imgUserStory.visibility = View.GONE


        userPic.loadCircleImage(userurl!!)
        lblName.text = username
        txtTimeAgo.text = times
        sendgiftto.text = "SEND GIFT TO "+ username!!



        userPic.setOnClickListener(View.OnClickListener {
            var bundle = Bundle()
            bundle.putBoolean(SearchUserProfileFragment.ARGS_FROM_CHAT, false)
            bundle.putString("userId", giftUserid)

            pausetimerandplayer()


            findNavController().navigate(
                destinationId = R.id.action_global_otherUserProfileFragment,
                popUpFragId = null,
                animType = AnimationTypes.SLIDE_ANIM,
                inclusive = true,
                args = bundle
            )
            dismiss()
        })

        lblName.setOnClickListener(View.OnClickListener {
            val bundle = Bundle()
            bundle.putBoolean(SearchUserProfileFragment.ARGS_FROM_CHAT, false)
            bundle.putString("userId", giftUserid)

            pausetimerandplayer()

            findNavController().navigate(
                destinationId = R.id.action_global_otherUserProfileFragment,
                popUpFragId = null,
                animType = AnimationTypes.SLIDE_ANIM,
                inclusive = true,
                args = bundle
            )
            dismiss()
        })



        img_close.setOnClickListener(View.OnClickListener {
            pausetimerandplayer()
            dismiss()
        })


        comment_l.setOnClickListener(View.OnClickListener {
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                pausetimerandplayer()

            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED;
                resumetimerandplayer()


            }
        })


        gift_l.setOnClickListener(View.OnClickListener {
            if (GiftbottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                GiftbottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
//            buttonSlideUp.text = "Slide Down";
                pausetimerandplayer()



            } else {
                GiftbottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED;
//            buttonSlideUp.text = "Slide Up"
                resumetimerandplayer()


            }
        })


        likes_l.setOnClickListener(View.OnClickListener {
            if(Uid.equals(giftUserid))
            {
                if (LikebottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                    LikebottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
//            buttonSlideUp.text = "Slide Down";
                    pausetimerandplayer()


                } else {
                    LikebottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED;
//            buttonSlideUp.text = "Slide Up"
                    resumetimerandplayer()


                }
            }
            else
            {
                //            showProgressView()
                lifecycleScope.launchWhenResumed {
                    val res = try {
                        apolloClient(
                            requireContext(),
                            userToken!!
                        ).mutation(LikeOnStoryMutation(objectID!!,"story"))
                            .execute()
                    } catch (e: ApolloException) {
                        Timber.d("apolloResponse ${e.message}")
//                    binding.root.snackbar("Exception ${e.message}")
                        Toast.makeText(requireContext(),"Exception ${e.message}",Toast.LENGTH_LONG).show()

//                    hideProgressView()
                        return@launchWhenResumed
                    }
                    if (res.hasErrors()) {

                        if(res.errors!![0].message.equals("User doesn't exist"))
                        {
                            Toast.makeText(requireContext(),"" + res.errors!![0].message,Toast.LENGTH_LONG).show()

                            Handler().postDelayed({ nouserexist() }, 1500)


                        }
                        else {

                            Toast.makeText(requireContext(),"" + res.errors!![0].message,Toast.LENGTH_LONG).show()

                        }

                    }

                    if (res.hasErrors() == false) {
                        val usermoments = res.data?.genericLike
                        fireLikeStoryNotification(objectID.toString(), giftUserid)
                        Timber.d("LIKE")

                        RefreshStories()
                    }


                }
            }
        })





        val bottomSheet = views.findViewById<ConstraintLayout>(com.i69app.R.id.bottomSheet)
        bottomSheetBehavior = BottomSheetBehavior.from<ConstraintLayout>(bottomSheet)
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        Timber.d("Slide Up")
                        resumetimerandplayer()


                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        Timber.d("Slide Hidden")

                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        Timber.d("Slide Down")
                        pausetimerandplayer()



                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        Timber.d("Slide Dragging")

                    }
                    BottomSheetBehavior.STATE_SETTLING -> {

                    }
                }
            }
        })

        val giftbottomSheet = views.findViewById<ConstraintLayout>(R.id.giftbottomSheet)
        GiftbottomSheetBehavior = BottomSheetBehavior.from<ConstraintLayout>(giftbottomSheet)
        GiftbottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        Timber.d("Slide Up")
                        resumetimerandplayer()


                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {

                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        Timber.d("Slide Down")
                        pausetimerandplayer()


                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {

                    }
                    BottomSheetBehavior.STATE_SETTLING -> {

                    }
                }
            }
        })
        val likebottomSheet = views.findViewById<ConstraintLayout>(R.id.likebottomSheet)
        LikebottomSheetBehavior = BottomSheetBehavior.from<ConstraintLayout>(likebottomSheet)
        LikebottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        Timber.d("Slide Up")
                        resumetimerandplayer()

                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {

                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        Timber.d("Slide Down")
                        pausetimerandplayer()


                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {

                    }
                    BottomSheetBehavior.STATE_SETTLING -> {

                    }
                }
            }
        })
        sendgiftto.setOnClickListener(View.OnClickListener {

            val items: MutableList<ModelGifts.Data.AllRealGift> = mutableListOf()
            fragVirtualGifts?.giftsAdapter?.getSelected()?.let { it1 -> items.addAll(it1) }
            fragRealGifts?.giftsAdapter?.getSelected()?.let { it1 -> items.addAll(it1) }

            lifecycleScope.launchWhenCreated() {
                if (items.size > 0) {
                    showProgressView()
                    items.forEach { gift ->

                        var res: ApolloResponse<GiftPurchaseMutation.Data>? = null
                        try {
                            res = apolloClient(
                                requireContext(),
                                userToken!!
                            ).mutation(GiftPurchaseMutation(gift.id, giftUserid!!)).execute()
                        } catch (e: ApolloException) {
                            Timber.d("apolloResponse ${e.message}")
                            Toast.makeText(requireContext(),"Exception ${e.message}", Toast.LENGTH_LONG).show()
//                                views.snackbar("Exception ${e.message}")
                            //hideProgressView()
                            //return@launchWhenResumed
                        }
                        if (res?.hasErrors() == false) {
//                                views.snackbar("You bought ${res.data?.giftPurchase?.giftPurchase?.gift?.giftName} successfully!")
                            Toast.makeText(requireContext(),"You bought ${res.data?.giftPurchase?.giftPurchase?.gift?.giftName} successfully!",
                                Toast.LENGTH_LONG).show()

                            fireGiftBuyNotificationforreceiver(gift.id,giftUserid)

                        }
                        if(res!!.hasErrors())
                        {
                            if(res.errors!![0].message.equals("User doesn't exist"))
                            {
                                Toast.makeText(requireContext(),"" + res.errors!![0].message,
                                    Toast.LENGTH_LONG).show()

                                Handler().postDelayed({ nouserexist() }, 1500)
                            }
                            else {
                                Toast.makeText(requireContext(),"" + res.errors!![0].message,
                                    Toast.LENGTH_LONG).show()
                            }




                        }
                        Timber.d("apolloResponse ${res.hasErrors()} ${res.data?.giftPurchase?.giftPurchase?.gift?.giftName}")
                    }
                    hideProgressView()
                }
            }

        })


        giftsTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
                giftsPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
            }
        })
        giftsTabs.setupWithViewPager(giftsPager)
        setupViewPager(giftsPager)


        send_btn.setOnClickListener(View.OnClickListener {

            if (msg_write.text.toString().equals("")) {
//                binding.root.snackbar(getString(R.string.you_cant_add_empty_msg))
                Toast.makeText(requireContext(),getString(R.string.you_cant_add_empty_msg),Toast.LENGTH_LONG).show()

                return@OnClickListener

            }
            showProgressView()
            lifecycleScope.launchWhenResumed {
                val res = try {
                    apolloClient(
                        requireContext(),
                        userToken!!
                    ).mutation(
                        CommentOnStoryMutation(
                            msg_write.text.toString(),
                            objectID!!,"story"
                        )
                    )
                        .execute()
                } catch (e: ApolloException) {
                    Timber.d("apolloResponse ${e.message}")
                    Toast.makeText(requireContext(),"Exception ${e.message}",Toast.LENGTH_LONG).show()

                    hideProgressView()
                    return@launchWhenResumed
                }

                hideProgressView()

                if(res!!.hasErrors())
                {
                    if(res.errors!![0].message.equals("User doesn't exist"))
                    {
                        Toast.makeText(requireContext(),"" + res.errors!![0].message,
                            Toast.LENGTH_LONG).show()

                        Handler().postDelayed({ nouserexist() }, 1500)


                    }
                    else {
                        Toast.makeText(requireContext(),"" + res.errors!![0].message,
                            Toast.LENGTH_LONG).show()
                    }




                }
                if (res.hasErrors() == false) {
                    fireCommntStoryNotification(objectID!!.toString(), giftUserid!!)

                    msg_write.setText("")

                    val usermoments = res.data?.genericComment

                    Timber.d("CMNT")
                    if (items.size > 0) {
                        RefreshStories()

                    } else {

                        getStories()
                    }

                }



            }

        })





        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(requireContext(), userToken!!).query(GetUserDataQuery(Uid!!)).execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                Toast.makeText(requireContext(),"Exception ${e.message}",Toast.LENGTH_LONG).show()

                hideProgressView()
                return@launchWhenResumed
            }

            hideProgressView()


            if(res!!.hasErrors())
            {
                if(res.errors!![0].message.equals("User doesn't exist"))
                {
                    Toast.makeText(requireContext(),"" + res.errors!![0].message,
                        Toast.LENGTH_LONG).show()

                    Handler().postDelayed({ nouserexist() }, 1500)


                }
                else {
                    Toast.makeText(requireContext(),"" + res.errors!![0].message,
                        Toast.LENGTH_LONG).show()
                }




            }
            if (res?.hasErrors() == false) {
                val UserData = res.data?.user

                try {
                    if (UserData!!.avatar!! != null) {

                        try {
                            thumbnail.loadCircleImage(
                                UserData.avatar!!.url!!.replace(
                                    "http://95.216.208.1:8000/media/",
                                    "${BuildConfig.BASE_URL}media/"
                                )
                            )
                            Timber.d(
                                "URL " + UserData.avatar!!.url!!.replace(
                                    "http://95.216.208.1:8000/media/",
                                    "${BuildConfig.BASE_URL}media/"
                                )
                            )
                        } catch (e: Exception) {
                            e.stackTrace
                        }


                    }
                } catch (e: Exception) {
                }
            }

        }

    }
    fun nouserexist()
    {
        lifecycleScope.launch(Dispatchers.Main) {
            App.userPreferences.clear()
            clearAppData()
            val intent = Intent(requireActivity(), SplashActivity::class.java)
            startActivity(intent)
            requireActivity().finishAffinity()
        }
    }


    private fun clearAppData() {
        try {
            // clearing app data
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {

                val activityManager =
                    requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

                activityManager.clearApplicationUserData()

            } else {

                val packageName: String = requireActivity().applicationContext.packageName
                val runtime = Runtime.getRuntime()
                runtime.exec("pm clear $packageName")
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    private fun getStories() {

//        showProgressView()
        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(requireContext(), userToken!!).query(GetAllUserStoriesQuery(100,"",
                    objectID.toString()
                ))
                    .execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                Toast.makeText(requireContext(),"Exception ${e.message}",Toast.LENGTH_LONG).show()

//                hideProgressView()
                return@launchWhenResumed
            }
            Timber.d("apolloResponse allUserStories stories ${res.hasErrors()}")

            if(res!!.hasErrors())
            {
                if(res.errors!![0].message.equals("User doesn't exist"))
                {
                    Toast.makeText(requireContext(),"" + res.errors!![0].message,
                        Toast.LENGTH_LONG).show()

                    Handler().postDelayed({ nouserexist() }, 1500)


                }
                else {
                    Toast.makeText(requireContext(),"" + res.errors!![0].message,
                        Toast.LENGTH_LONG).show()
                }




            }

            if (res?.hasErrors() == false) {
                val allUserStories = res.data?.allUserStories!!.edges
                if (allUserStories.size != 0) {
                    txtNearbyUserLikeCount!!.text =
                        allUserStories.get(0)!!.node!!.likesCount.toString()
                    txtMomentRecentComment!!.text =
                        allUserStories.get(0)!!.node!!.commentsCount.toString() + " Comments"
                    txtheaderlike!!.text =
                        allUserStories.get(0)!!.node!!.likesCount.toString() + " Likes"
                    giftUserid = allUserStories.get(0)!!.node!!.user!!.id.toString()

                    val Likedata = allUserStories.get(0)!!.node!!.likes!!.edges

                    if (rvLikes!!.itemDecorationCount == 0) {
                        rvLikes!!.addItemDecoration(object : RecyclerView.ItemDecoration() {
                            override fun getItemOffsets(
                                outRect: Rect,
                                view: View,
                                parent: RecyclerView,
                                state: RecyclerView.State
                            ) {
                                outRect.top = 25
                            }
                        })
                    }
                    if (Likedata.size > 0) {
                        Timber.d("apolloResponse: ${Likedata[0]!!.node!!.id}")
                        no_data!!.visibility = View.GONE
                        rvLikes!!.visibility = View.VISIBLE

                        val items1: java.util.ArrayList<CommentsModel> = java.util.ArrayList()

                        Likedata.indices.forEach { i ->

                            val models = CommentsModel()

                            models.commenttext = Likedata[i]!!.node!!.user.fullName

                            models.userurl = Likedata[i]!!.node!!.user.avatar!!.url

                            items1.add(models)

                        }

                        adapters = StoryLikesAdapter(requireActivity(), items1)
                        rvLikes!!.adapter = adapters
                        rvLikes!!.layoutManager = LinearLayoutManager(activity)
                    } else {
                        no_data!!.visibility = View.VISIBLE
                        rvLikes!!.visibility = View.GONE
                    }

                    val Commentdata = allUserStories.get(0)!!.node!!.comments!!.edges

                    if (rvSharedMoments!!.itemDecorationCount == 0) {
                        rvSharedMoments!!.addItemDecoration(object : RecyclerView.ItemDecoration() {
                            override fun getItemOffsets(
                                outRect: Rect,
                                view: View,
                                parent: RecyclerView,
                                state: RecyclerView.State
                            ) {
                                outRect.top = 25
                            }
                        })
                    }
                    if (Commentdata!!.size > 0) {
                        Timber.d("apolloResponse: ${Commentdata?.get(0)?.node!!.commentDescription}")
                        nodata!!.visibility = View.GONE
                        rvSharedMoments!!.visibility = View.VISIBLE


                        items = ArrayList()






                        Commentdata.indices.forEach { i ->
                            val hm: MutableList<ReplysModel> = ArrayList()


                            val models = CommentsModel()

                            models.commenttext = Commentdata[i]!!.node!!.commentDescription

                            if (Commentdata[i]!!.node!!.user.avatarPhotos!!.size > 0) {
                                models.userurl = Commentdata[i]!!.node!!.user.avatar!!.url

                            } else {
                                models.userurl = ""
                            }
                            models.username = Commentdata[i]!!.node!!.user.fullName
                            models.timeago = Commentdata[i]!!.node!!.createdDate.toString()
                            models.cmtID = Commentdata[i]!!.node!!.pk.toString()
                            models.momentID = objectID?.toString()
                            models.uid = Commentdata[i]!!.node!!.user.id.toString()



                            models.replylist = hm
                            models.isExpanded = true



                            items.add(models)
                        }




                        adapter =
                            VideoStoryCommentListAdapter(
                                requireActivity(),
                                this@PlayUserStoryDialogFragment,
                                items,
                                this@PlayUserStoryDialogFragment
                            )
                        rvSharedMoments!!.adapter = adapter
                        rvSharedMoments!!.layoutManager = LinearLayoutManager(activity)
                    } else {
                        nodata!!.visibility = View.VISIBLE
                        rvSharedMoments!!.visibility = View.GONE

                    }

                }

            }
        }
    }

    private fun RefreshStories() {

//        showProgressView()
        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(requireContext(), userToken!!).query(GetAllUserStoriesQuery(100,"",
                    objectID.toString()
                ))
                    .execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                Toast.makeText(requireContext(),"Exception ${e.message}",Toast.LENGTH_LONG).show()

//                hideProgressView()
                return@launchWhenResumed
            }
            Timber.d("apolloResponse allUserStories stories ${res.hasErrors()}")


            if(res!!.hasErrors())
            {
                if(res.errors!![0].message.equals("User doesn't exist"))
                {
                    Toast.makeText(requireContext(),"" + res.errors!![0].message,
                        Toast.LENGTH_LONG).show()

                    Handler().postDelayed({ nouserexist() }, 1500)


                }
                else {
                    Toast.makeText(requireContext(),"" + res.errors!![0].message,
                        Toast.LENGTH_LONG).show()
                }




            }

            if (res?.hasErrors() == false) {
                val allUserStories = res.data?.allUserStories!!.edges
                if (allUserStories != null && allUserStories.size != 0) {
                    txtNearbyUserLikeCount!!.text = "" + allUserStories.get(0)!!.node!!.likesCount
                    txtMomentRecentComment!!.text =
                        "" + allUserStories.get(0)!!.node!!.commentsCount + " Comments"
                    txtheaderlike!!.text =
                        allUserStories.get(0)!!.node!!.likesCount.toString() + " Likes"
                    giftUserid = allUserStories.get(0)!!.node!!.user!!.id.toString()

                    val Likedata = allUserStories.get(0)!!.node!!.likes
                    val Commentdata = allUserStories.get(0)!!.node!!.comments!!.edges

                    if (rvSharedMoments!!.itemDecorationCount == 0) {
                        rvSharedMoments!!.addItemDecoration(object : RecyclerView.ItemDecoration() {
                            override fun getItemOffsets(
                                outRect: Rect,
                                view: View,
                                parent: RecyclerView,
                                state: RecyclerView.State
                            ) {
                                outRect.top = 25
                            }
                        })
                    }
                    if (Commentdata!!.size > 0) {
                        Timber.d("apolloResponse: ${Commentdata?.get(0)?.node!!.commentDescription}")
                        nodata!!.visibility = View.GONE
                        rvSharedMoments!!.visibility = View.VISIBLE


                        val items1: ArrayList<CommentsModel> = ArrayList()







                        Commentdata.indices.forEach { i ->
                            val hm: MutableList<ReplysModel> = ArrayList()


                            val models = CommentsModel()

                            models.commenttext = Commentdata[i]!!.node!!.commentDescription

                            if (Commentdata[i]!!.node!!.user.avatarPhotos!!.size > 0) {
                                models.userurl = Commentdata[i]!!.node!!.user.avatar!!.url

                            } else {
                                models.userurl = ""
                            }
                            models.username = Commentdata[i]!!.node!!.user.fullName
                            models.timeago = Commentdata[i]!!.node!!.createdDate.toString()
                            models.cmtID = Commentdata[i]!!.node!!.pk.toString()
                            models.momentID = objectID?.toString()
                            models.uid = Commentdata[i]!!.node!!.user.id.toString()



                            models.replylist = hm
                            models.isExpanded = true



                            items1.add(models)

                            if (items1.size - 1 == i) {
                                if (items.size != 0) {
                                    if (adapter != null) {
                                        adapter!!.updateList(items1)
                                    }

                                }
                            }
                        }


                    } else {
                        nodata!!.visibility = View.VISIBLE
                        rvSharedMoments!!.visibility = View.GONE

                    }

                }
            }

        }
    }
    fun fireLikeStoryNotification(pkids: String, userid: String?) {

        lifecycleScope.launchWhenResumed {


            val queryName = "sendNotification"
            val query = StringBuilder()
                .append("mutation {")
                .append("$queryName (")
                .append("userId: \"${userid}\", ")
                .append("notificationSetting: \"STLIKE\", ")
                .append("data: {pk:${pkids}}")
                .append(") {")
                .append("sent")
                .append("}")
                .append("}")
                .toString()

            val result= AppModule.provideGraphqlApi().getResponse<Boolean>(
                query,
                queryName, userToken)
            Timber.d("RSLT",""+result.message)

        }








    }
    fun fireCommntStoryNotification(pkids: String, userid: String?) {

        lifecycleScope.launchWhenResumed {


            val queryName = "sendNotification"
            val query = StringBuilder()
                .append("mutation {")
                .append("$queryName (")
                .append("userId: \"${userid}\", ")
                .append("notificationSetting: \"STCMNT\", ")
                .append("data: {pk:${pkids}}")
                .append(") {")
                .append("sent")
                .append("}")
                .append("}")
                .toString()

            val result= AppModule.provideGraphqlApi().getResponse<Boolean>(
                query,
                queryName, userToken)
            Timber.d("RSLT",""+result.message)

        }








    }

    fun fireGiftBuyNotificationforreceiver(gid: String, userid: String?) {

        lifecycleScope.launchWhenResumed {


            val queryName = "sendNotification"
            val query = StringBuilder()
                .append("mutation {")
                .append("$queryName (")
                .append("userId: \"${userid}\", ")
                .append("notificationSetting: \"GIFT RLVRTL\", ")
                .append("data: {giftId:${gid}}")
                .append(") {")
                .append("sent")
                .append("}")
                .append("}")
                .toString()

            val result= AppModule.provideGraphqlApi().getResponse<Boolean>(
                query,
                queryName, userToken)
            Timber.d("RSLT",""+result.message)

        }








    }
    protected fun showProgressView() {
        loadingDialog.show()
    }

    protected fun hideProgressView() {
        loadingDialog.dismiss()
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = UserItemsAdapter(childFragmentManager)
        fragRealGifts = FragmentRealGifts()
        fragVirtualGifts = FragmentVirtualGifts()

        adapter.addFragItem(fragRealGifts!!, getString(R.string.real_gifts))
        adapter.addFragItem(fragVirtualGifts!!, getString(R.string.virtual_gifts))
        viewPager.adapter = adapter
    }

    private fun initPlayer(){

        val mediaItem = MediaItem.Builder()
            .setUri("")
            .setMimeType(MimeTypes.VIDEO_MP4)
            .build()
        exoPlayer = SimpleExoPlayer.Builder(requireActivity()).build().apply {
            playWhenReady = isPlayerPlaying
            seekTo(currentWindow, playbackPosition)
            setMediaItem(mediaItem, false)
            prepare()
        }
        player_view!!.player = exoPlayer
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(STATE_RESUME_WINDOW, exoPlayer.currentWindowIndex)
        outState.putLong(STATE_RESUME_POSITION, exoPlayer.currentPosition)
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, isFullscreen)
        outState.putBoolean(STATE_PLAYER_PLAYING, isPlayerPlaying)
        super.onSaveInstanceState(outState)
    }
    private fun releasePlayer(){
        isPlayerPlaying = exoPlayer.playWhenReady
        playbackPosition = exoPlayer.currentPosition
        currentWindow = exoPlayer.currentWindowIndex
        exoPlayer.release()
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

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == ExoPlayer.STATE_READY && !durationSet) {
                    val realDurationMillis: Long = exoPlayer.getDuration()
                    durationSet = true
                    val duration = realDurationMillis
                    Timber.d("filee ${duration}")
                    progressBar1!!.max = realDurationMillis.toInt()
                    val millisInFuture = duration.toLong()

                    timer1 = object : CountDownTimerExt(millisInFuture, 100) {
                        override fun onTimerTick(millisUntilFinished: Long) {
                            Log.d("MainActivity", "onTimerTick $millisUntilFinished")
                            onTickProgressUpdate()

                        }

                        override fun onTimerFinish() {
                            Log.d("MainActivity", "onTimerFinish")
                            dismiss()

                        }

                    }
                    hideProgressView()
                    timer1.run { this!!.start() }

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

    private fun onTickProgressUpdate() {

        countUp += 100
        val progress = countUp
        Timber.d("prggress $progress")
        progressBar1!!.smoothProgress(progress)
    }

    override fun onreply(position: Int, models: CommentsModel) {
    }

    override fun oncommentLike(position: Int, models: CommentsModel) {
    }

    override fun onUsernameClick(position: Int, models: CommentsModel) {
    }


}