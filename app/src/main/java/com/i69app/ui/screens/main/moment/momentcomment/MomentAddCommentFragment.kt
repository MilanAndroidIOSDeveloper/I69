package com.i69app.ui.screens.main.moment.momentcomment


import android.app.ActivityManager
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.exception.ApolloException
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textview.MaterialTextView
import com.i69app.*
import com.i69app.data.models.ModelGifts
import com.i69app.databinding.FragmentMomentsAddcommentsBinding
import com.i69app.di.modules.AppModule
import com.i69app.gifts.FragmentRealGifts
import com.i69app.gifts.FragmentVirtualGifts
import com.i69app.singleton.App
import com.i69app.ui.adapters.CommentListAdapter
import com.i69app.ui.adapters.CommentReplyListAdapter
import com.i69app.ui.adapters.UserItemsAdapter
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.screens.SplashActivity
import com.i69app.ui.screens.main.search.userProfile.SearchUserProfileFragment
import com.i69app.ui.viewModels.CommentsModel
import com.i69app.ui.viewModels.ReplysModel
import com.i69app.ui.viewModels.UserViewModel
import com.i69app.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList


class MomentAddCommentFragment : BaseFragment<FragmentMomentsAddcommentsBinding>(),
    CommentListAdapter.ClickPerformListener, CommentReplyListAdapter.ClickonListener {

    private lateinit var giftbottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private var userToken: String? = null
    private var userName: String? = null
    private var userId: String? = null
    private lateinit var fragVirtualGifts: FragmentVirtualGifts
    private lateinit var fragRealGifts: FragmentRealGifts
    var giftUserid: String? = null

    private var muserID: String? = ""
    private var mID: String? = ""
    private var filesUrl: String? = ""
    private var likess: String? = ""
    private var Comments: String? = ""
    private var Desc_: String? = ""
    private var Desc1_: List<String?>? = ArrayList()
    private var fullnames: String? = ""
    private var gender: String? = ""


    var builder: SpannableStringBuilder? = null
    var adapter: CommentListAdapter? = null


    var items: ArrayList<CommentsModel> = ArrayList()

    var Replymodels: CommentsModel? = null

    private val viewModel: UserViewModel by activityViewModels()

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentMomentsAddcommentsBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        navController = findNavController()

        lifecycleScope.launch {
            userToken = getCurrentUserToken()!!
            userName = getCurrentUserName()!!
            userId = getCurrentUserId()
            Timber.i("usertokenn $userToken")
        }
        Timber.i("usertokenn 2 $userToken")


        binding.imgback.setOnClickListener(View.OnClickListener {
            activity?.onBackPressed()
        })

        setSendGiftLayout()

        binding.imgNearbyUserGift.setOnClickListener(View.OnClickListener {
            if (giftbottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                giftbottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                giftbottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
            /* var bundle = Bundle()
             bundle.putString("userId", userId)
             navController.navigate(R.id.action_userProfileFragment_to_userGiftsFragment, bundle)*/
        })



        binding.sendBtn.setOnClickListener(View.OnClickListener {


            if (binding.msgWrite.text.toString().equals("")) {
                binding.root.snackbar(getString(R.string.you_cant_add_empty_msg))
                return@OnClickListener

            }

            if (binding.msgWrite.text.toString().startsWith("@") && binding.msgWrite.text.toString()
                    .trim().contains(
                        Replymodels!!.username!!, true
                    )
            ) {
                showProgressView()
                lifecycleScope.launchWhenResumed {
                    val res = try {
                        apolloClient(
                            requireContext(),
                            userToken!!
                        ).mutation(
                            CommentReplyOnMomentMutation(
                                binding.msgWrite.text.toString()
                                    .replace("@" + Replymodels!!.username!!, "").trim(),
                                Replymodels!!.momentID!!, Replymodels!!.cmtID!!
                            )
                        )
                            .execute()
                    } catch (e: ApolloException) {
                        Timber.d("apolloResponse ${e.message}")
                        binding.root.snackbar("Exception ${e.message}")
                        hideProgressView()
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
                    hideProgressView()
                    if (res.hasErrors() == false) {
                        val usermoments = res.data?.commentMoment!!.comment!!.momemt


                        binding.lblItemNearbyUserCommentCount.setText("" + usermoments.comment!!)


                        binding.msgWrite.setText("")

                        getAllCommentsofMomentsRefresh()
                    }
                }
            } else {
                showProgressView()
                lifecycleScope.launchWhenResumed {
                    val res = try {
                        apolloClient(
                            requireContext(),
                            userToken!!
                        ).mutation(
                            CommentonmomentMutation(
                                mID.toString(),
                                binding.msgWrite.text.toString()
                            )
                        )
                            .execute()
                    } catch (e: ApolloException) {
                        Timber.d("apolloResponse ${e.message}")
                        binding.root.snackbar("Exception ${e.message}")
                        hideProgressView()
                        return@launchWhenResumed
                    }

                    hideProgressView()


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
                        binding.msgWrite.setText("")

                        val usermoments = res.data?.commentMoment!!.comment!!.momemt


                        binding.lblItemNearbyUserCommentCount.setText("" + usermoments.comment!!)

                        if (items.size > 0) {
                            getAllCommentsofMomentsRefresh()

                        } else {
                            getAllCommentsofMoments();
                        }
                        fireCommentNotificationforreceiver(muserID, mID)

                    }
                }
            }
        })

        binding.imgNearbyUserLikes.setOnClickListener(View.OnClickListener {


            showProgressView()
            lifecycleScope.launchWhenResumed {
                val res = try {
                    apolloClient(
                        requireContext(),
                        userToken!!
                    ).mutation(LikeOnMomentMutation(mID!!))
                        .execute()
                } catch (e: ApolloException) {
                    Timber.d("apolloResponse ${e.message}")
                    binding.root.snackbar("Exception ${e.message}")
                    hideProgressView()
                    return@launchWhenResumed
                }

                hideProgressView()


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
                    val usermoments = res.data?.likeMoment!!.like!!.momemt
                    binding.txtNearbyUserLikeCount.setText("" + usermoments.like!!)


                    fireLikeNotificationforreceiver(muserID, mID)
                }

            }


        })

        binding.lblItemNearbyName.setOnClickListener(View.OnClickListener {
            var bundle = Bundle()
            bundle.putBoolean(SearchUserProfileFragment.ARGS_FROM_CHAT, false)
            bundle.putString("userId", muserID)
            findNavController().navigate(
                destinationId = R.id.action_global_otherUserProfileFragment,
                popUpFragId = null,
                animType = AnimationTypes.SLIDE_ANIM,
                inclusive = true,
                args = bundle
            )
        })


        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(requireContext(), userToken!!).query(GetUserDataQuery(userId!!)).execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                binding.root.snackbar("Exception ${e.message}")
                hideProgressView()
                return@launchWhenResumed
            }

            hideProgressView()


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

                val UserData = res.data?.user

                try {
                    if (UserData!!.avatar!! != null) {

                        try {
                            binding.thumbnail.loadCircleImage(
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


        val url = "${BuildConfig.BASE_URL}media/${filesUrl}"

        binding.imgSharedMoment.loadImage(url)

        binding.txtNearbyUserLikeCount.setText(likess)

        binding.lblItemNearbyUserCommentCount.setText(Comments)

        binding.lblItemNearbyName.setText(fullnames!!.uppercase())

        binding.txtMomentRecentComment.setText(builder);
        binding.txtMomentRecentComment.setMovementMethod(LinkMovementMethod.getInstance())

        if (gender != null) {
            if (gender.equals("A_0")) {
                binding.imgNearbyUserGift.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        requireActivity().resources,
                        R.drawable.yellow_gift_male,
                        null
                    )
                )

            } else if (gender.equals("A_1")) {
                binding.imgNearbyUserGift.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        requireActivity().resources,
                        R.drawable.red_gift_female,
                        null
                    )
                )

            } else if (gender.equals("A_2")) {
                binding.imgNearbyUserGift.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        requireActivity().resources,
                        R.drawable.purple_gift_nosay,
                        null
                    )
                )

            }
//        else
//        {
//            binding.imgNearbyUserGift.setImageDrawable(ResourcesCompat.getDrawable(requireActivity().resources,R.drawable.pink_gift_noavb,null))
//
//        }
        }
        getAllCommentsofMoments()

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

    private fun setSendGiftLayout() {
        val giftBottomSheet = binding.root.findViewById<ConstraintLayout>(R.id.giftbottomSheet)
        val sendgiftto = giftBottomSheet.findViewById<MaterialTextView>(R.id.sendgiftto)
        val giftsTabs = giftBottomSheet.findViewById<TabLayout>(R.id.giftsTabs)
        val giftsPager = giftBottomSheet.findViewById<ViewPager>(R.id.gifts_pager)
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
        sendgiftto.text = "SEND GIFT TO $fullnames"
        sendgiftto.setOnClickListener {

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
                            Toast.makeText(
                                requireContext(),
                                "Exception ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            //                                views.snackbar("Exception ${e.message}")
                            //hideProgressView()
                            //return@launchWhenResumed
                        }
                        if (res!!.hasErrors() == false) {
                            //                                views.snackbar("You bought ${res.data?.giftPurchase?.giftPurchase?.gift?.giftName} successfully!")
                            Toast.makeText(
                                requireContext(),
                                "You bought ${res.data?.giftPurchase?.giftPurchase?.gift?.giftName} successfully!",
                                Toast.LENGTH_LONG
                            ).show()

                            fireGiftBuyNotificationforreceiver(gift.id, giftUserid)

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
                    }
                    hideProgressView()
                }
            }

        }
        giftbottomSheetBehavior = BottomSheetBehavior.from(giftBottomSheet)
        giftbottomSheetBehavior.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        Timber.d("Slide Up")
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {

                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        Timber.d("Slide Down")
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {

                    }
                    BottomSheetBehavior.STATE_SETTLING -> {

                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {

                    }
                }
            }
        })
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = UserItemsAdapter(childFragmentManager)
        fragRealGifts = FragmentRealGifts()
        fragVirtualGifts = FragmentVirtualGifts()

        adapter.addFragItem(fragRealGifts!!, getString(R.string.real_gifts))
        adapter.addFragItem(fragVirtualGifts!!, getString(R.string.virtual_gifts))
        viewPager.adapter = adapter
    }


    private fun fireGiftBuyNotificationforreceiver(gid: String, userid: String?) {
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

    private fun getAllCommentsofMoments() {
        items = ArrayList()

        showProgressView()
        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(requireContext(), userToken!!).query(GetAllCommentOfMomentQuery(mID!!))
                    .execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                binding.root.snackbar("getAllComments ${e.message}")
                hideProgressView()
                return@launchWhenResumed
            }

            hideProgressView()


            if (res.hasErrors()) {

                if (res.errors!![0].message.equals("User doesn't exist")) {
                    Toast.makeText(
                        requireContext(),
                        "" + res.errors!![0].message,
                        Toast.LENGTH_LONG
                    ).show()

                    Handler().postDelayed({ nouserexist() }, 1500)


                } else {

                    Toast.makeText(
                        requireContext(),
                        "" + res.errors!![0].message,
                        Toast.LENGTH_LONG
                    ).show()

                }

            }
            if (res.hasErrors() == false) {
            val allusermoments = res.data?.allComments

            if (binding.rvSharedMoments.itemDecorationCount == 0) {
                binding.rvSharedMoments.addItemDecoration(object : RecyclerView.ItemDecoration() {
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
            if (allusermoments?.size!! > 0) {
                Timber.d("apolloResponse: ${allusermoments?.get(0)?.commentDescription}")
                binding.noData.visibility = View.GONE
                binding.rvSharedMoments.visibility = View.VISIBLE



                allusermoments.indices.forEach { i ->
                    val hm: MutableList<ReplysModel> = ArrayList()


                    val models = CommentsModel()

                    models.commenttext = allusermoments[i]!!.commentDescription

                    if (allusermoments[i]!!.user.avatarPhotos?.size!! > 0) {
                        models.userurl = allusermoments[i]!!.user.avatarPhotos?.get(0)?.url

                    } else {
                        models.userurl = ""
                    }
                    models.username = allusermoments[i]!!.user.fullName
                    models.timeago = allusermoments[i]!!.createdDate.toString()
                    models.cmtID = allusermoments[i]!!.pk.toString()
                    models.momentID = mID
                    models.cmtlikes = allusermoments[i]!!.like.toString()
                    models.uid = allusermoments[i]!!.user.id.toString()

                    for (f in 0 until allusermoments[i]!!.replys!!.size) {


                        val md = ReplysModel()

                        md.replytext = allusermoments[i]!!.replys!![f]!!.commentDescription
                        md.userurl =
                            allusermoments[i]!!.replys!![f]!!.user.avatarPhotos?.get(0)?.url
                        md.usernames = allusermoments[i]!!.replys!![f]!!.user.fullName
                        md.timeago = allusermoments[i]!!.createdDate.toString()
                        md.uid = allusermoments[i]!!.user.id.toString()


                        hm.add(f, md)


                    }

                    models.replylist = hm
                    models.isExpanded = true



                    items.add(models)
                }




                adapter =
                    CommentListAdapter(
                        requireActivity(),
                        this@MomentAddCommentFragment,
                        items,
                        this@MomentAddCommentFragment
                    )
                binding.rvSharedMoments.adapter = adapter
                binding.rvSharedMoments.layoutManager = LinearLayoutManager(activity)
            } else {
                binding.noData.visibility = View.VISIBLE
                binding.rvSharedMoments.visibility = View.GONE

            }
        }
        }
    }


    private fun getAllCommentsofMomentsRefresh() {

        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(requireContext(), userToken!!).query(GetAllCommentOfMomentQuery(mID!!))
                    .execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                binding.root.snackbar("getAllComments ${e.message}")
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
                val allusermoments = res.data?.allComments

                if (binding.rvSharedMoments.itemDecorationCount == 0) {
                    binding.rvSharedMoments.addItemDecoration(object :
                        RecyclerView.ItemDecoration() {
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
                if (allusermoments?.size!! > 0) {
                    Timber.d("apolloResponse: ${allusermoments?.get(0)?.commentDescription}")
                    binding.noData.visibility = View.GONE
                    binding.rvSharedMoments.visibility = View.VISIBLE


                    val items1: ArrayList<CommentsModel> = ArrayList()



                    allusermoments.indices.forEach { i ->
                        val hm: MutableList<ReplysModel> = ArrayList()


                        val models = CommentsModel()

                        models.commenttext = allusermoments[i]!!.commentDescription
                        if (allusermoments[i]!!.user.avatarPhotos?.size!! > 0) {
                            models.userurl = allusermoments[i]!!.user.avatarPhotos?.get(0)?.url

                        } else {
                            models.userurl = ""
                        }
                        models.username = allusermoments[i]!!.user.fullName
                        models.timeago = allusermoments[i]!!.createdDate.toString()
                        models.cmtID = allusermoments[i]!!.pk.toString()
                        models.momentID = mID
                        models.cmtlikes = allusermoments[i]!!.like.toString()
                        models.uid = allusermoments[i]!!.user.id.toString()

                        for (f in 0 until allusermoments[i]!!.replys!!.size) {


                            val md = ReplysModel()

                            md.replytext = allusermoments[i]!!.replys!![f]!!.commentDescription
                            md.userurl =
                                allusermoments[i]!!.replys!![f]!!.user.avatarPhotos?.get(0)?.url
                            md.usernames = allusermoments[i]!!.replys!![f]!!.user.fullName
                            md.timeago = allusermoments[i]!!.createdDate.toString()
                            md.uid = allusermoments[i]!!.user.id.toString()


                            hm.add(f, md)


                        }

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
                    binding.noData.visibility = View.VISIBLE
                    binding.rvSharedMoments.visibility = View.GONE

                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val arguments = arguments
        if (arguments != null) {
            mID = arguments.get("momentID") as String?
            filesUrl = arguments.get("filesUrl") as String?
            likess = arguments.get("Likes") as String?
            Comments = arguments.get("Comments") as String?
            Desc_ = arguments.get("Desc") as String
            fullnames = arguments.get("fullnames") as String?
            muserID = arguments.get("momentuserID") as String?
            gender = arguments.get("gender") as String?
            giftUserid = arguments.get("userId") as String?


            Desc1_ = Arrays.asList(Desc_)






            if (Desc_!!.split(",").size > 1) {
                val s1 =
                    SpannableString(
                        Desc_!!.split(",")[0].replace("[\"", "").replace("\"]", "")
                            .replace("\"", "")
                    )
                val s2 = SpannableString("READ MORE")

                s1.setSpan(
                    ForegroundColorSpan(Color.WHITE),
                    0,
                    s1.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                s2.setSpan(
                    ForegroundColorSpan(resources.getColor(R.color.colorPrimary)),
                    0,
                    s2.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                s2.setSpan(StyleSpan(Typeface.BOLD), 0, s2.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)


                s2.setSpan(object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        builder = SpannableStringBuilder()
                        Desc1_!!.forEach { builder!!.append(it).toString() }

                        binding.txtMomentRecentComment.setText(
                            builder.toString().replace("[\"", "").replace("\"]", "")
                                .replace("\",\"", "")
                        )

                    }
                }, 0, s2.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)


                // build the string
                builder = SpannableStringBuilder()
                builder!!.append(s1)
                builder!!.append(s2)
            } else {
                builder = SpannableStringBuilder()
                builder!!.append(Desc_!!.replace("[\"", "").replace("\"]", ""))
            }


        }
        return super.onCreateView(inflater, container, savedInstanceState)

    }


    override fun setupClickListeners() {

    }

    fun fireLikeNotificationforreceiver(muserID: String?, mID: String?) {


        lifecycleScope.launchWhenResumed {


            val queryName = "sendNotification"
            val query = StringBuilder()
                .append("mutation {")
                .append("$queryName (")
                .append("userId: \"${muserID}\", ")
                .append("notificationSetting: \"LIKE\", ")
                .append("data: {momentId:${mID}}")
                .append(") {")
                .append("sent")
                .append("}")
                .append("}")
                .toString()

            val result = AppModule.provideGraphqlApi().getResponse<Boolean>(
                query,
                queryName, userToken
            )
            Timber.d("RSLT", "" + result.message)

        }


    }

    fun fireCommentNotificationforreceiver(muserID: String?, mID: String?) {


        lifecycleScope.launchWhenResumed {


            val queryName = "sendNotification"
            val query = StringBuilder()
                .append("mutation {")
                .append("$queryName (")
                .append("userId: \"${muserID}\", ")
                .append("notificationSetting: \"CMNT\", ")
                .append("data: {momentId:${mID}}")
                .append(") {")
                .append("sent")
                .append("}")
                .append("}")
                .toString()

            val result = AppModule.provideGraphqlApi().getResponse<Boolean>(
                query,
                queryName, userToken
            )
            Timber.d("RSLT", "" + result.message)

        }


    }


    override fun onreply(position: Int, models: CommentsModel) {

        Replymodels = models

//        binding.msgWrite.performClick()
        binding.msgWrite.setText("")
        binding.msgWrite.requestFocus()
        binding.msgWrite.postDelayed(Runnable {

            val inputMethodManager =
                requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?

            inputMethodManager!!.showSoftInput(binding.msgWrite, InputMethodManager.SHOW_IMPLICIT)
            binding.msgWrite.append("@" + models.username + " ")

        }, 150)

    }


    override fun oncommentLike(position: Int, models: CommentsModel) {
        showProgressView()
        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(
                    requireContext(),
                    userToken!!
                ).mutation(
                    LikeOnCommentMutation(
                        models.cmtID!!
                    )
                )
                    .execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                binding.root.snackbar("Exception ${e.message}")
                hideProgressView()
                return@launchWhenResumed
            }

            hideProgressView()

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

                getAllCommentsofMomentsRefresh()
            }
        }
    }

    override fun onUsernameClick(position: Int, models: CommentsModel) {
        var bundle = Bundle()
        bundle.putBoolean(SearchUserProfileFragment.ARGS_FROM_CHAT, false)
        bundle.putString("userId", models!!.uid)
        findNavController().navigate(
            destinationId = R.id.action_global_otherUserProfileFragment,
            popUpFragId = null,
            animType = AnimationTypes.SLIDE_ANIM,
            inclusive = true,
            args = bundle
        )
    }

    override fun onUsernameClick(position: Int, models: ReplysModel) {
        var bundle = Bundle()
        bundle.putBoolean(SearchUserProfileFragment.ARGS_FROM_CHAT, false)
        bundle.putString("userId", models.uid)
        findNavController().navigate(
            destinationId = R.id.action_global_otherUserProfileFragment,
            popUpFragId = null,
            animType = AnimationTypes.SLIDE_ANIM,
            inclusive = true,
            args = bundle
        )
    }
}