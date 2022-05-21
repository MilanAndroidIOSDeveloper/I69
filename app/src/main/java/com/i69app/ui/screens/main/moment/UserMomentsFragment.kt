package com.i69app.ui.screens.main.moment

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.format.DateUtils
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.DefaultUpload
import com.apollographql.apollo3.api.content
import com.apollographql.apollo3.exception.ApolloException
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.i69app.*
import com.i69app.data.models.ModelGifts
import com.i69app.databinding.FragmentUserMomentsBinding
import com.i69app.di.modules.AppModule
import com.i69app.di.modules.AppModule.provideGraphqlApi
import com.i69app.gifts.FragmentRealGifts
import com.i69app.gifts.FragmentVirtualGifts
import com.i69app.singleton.App
import com.i69app.ui.adapters.NearbySharedMomentAdapter
import com.i69app.ui.adapters.UserItemsAdapter
import com.i69app.ui.adapters.UserStoriesAdapter
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.screens.ImagePickerActivity
import com.i69app.ui.screens.SplashActivity
import com.i69app.ui.screens.main.MainActivity
import com.i69app.ui.screens.main.notification.NotificationDialogFragment
import com.i69app.ui.viewModels.UserViewModel
import com.i69app.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class UserMomentsFragment : BaseFragment<FragmentUserMomentsBinding>(),
    UserStoriesAdapter.UserStoryListener,
    NearbySharedMomentAdapter.NearbySharedMomentListener {

    private var ShowNotification: String? = ""
    var width = 0
    var size = 0
    private var userToken: String? = null
    private lateinit var usersAdapter: UserStoriesAdapter
    private lateinit var sharedMomentAdapter: NearbySharedMomentAdapter
    private var mFilePath: String? = null
    var layoutManager: LinearLayoutManager? = null
    var allUserMoments: ArrayList<GetAllUserMomentsQuery.Edge> = ArrayList()
    private val viewModel: UserViewModel by activityViewModels()

    private var userId: String? = null
    private var userName: String? = null

    var endCursor: String=""
    var hasNextPage: Boolean= false


    private lateinit var GiftbottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    var giftUserid: String? = null
    var fragVirtualGifts: FragmentVirtualGifts?= null
    var fragRealGifts: FragmentRealGifts?= null

    private val photosLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            val data = activityResult.data
            if (activityResult.resultCode == Activity.RESULT_OK) {
                mFilePath = data?.getStringExtra("result")
                Timber.d("fileBase64 $mFilePath")
                uploadStory()
            }
        }

    private val videoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            val contentURI = activityResult.data?.data
            mFilePath = contentURI?.getVideoFilePath(requireContext())
            val f = File(mFilePath!!)
            Timber.d("filee ${f.exists()} ${f.length()} ${f.absolutePath}")
            if (f.exists()) {
                val sizeInMb = (f.length() / 1000) / 1000
                if (sizeInMb < 2) {
                    uploadStory()
                } else {
                    mFilePath = null
                    val ok = resources.getString(R.string.pix_ok)
                    requireContext().showOkAlertDialog(
                        ok,
                        "File Size ${sizeInMb}MB",
                        "Your video file should be less than 11mb"
                    ) { dialog, which -> }
                }
            } else {
                binding.root.snackbar("Wrong path $mFilePath")
            }
        }

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentUserMomentsBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        navController = findNavController()

        lifecycleScope.launch {
            userId = getCurrentUserId()!!
            userToken = getCurrentUserToken()!!
            Timber.i("usertokenn $userToken")
        }
        Timber.i("userID $userId")



        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        width = displayMetrics.widthPixels

        val densityMultiplier =getResources().getDisplayMetrics().density;
        val scaledPx = 14 * densityMultiplier;
        val paint = Paint()
        paint.setTextSize(scaledPx);
        size = paint.measureText("s").roundToInt()
        allUserMoments = ArrayList()
        sharedMomentAdapter = NearbySharedMomentAdapter(
            requireActivity(),
            this@UserMomentsFragment,
            allUserMoments,
            userId
        )
        layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.rvSharedMoments.setLayoutManager(layoutManager)

        getAllUserStories()
        getAllUserMoments(width,size)

        binding.scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (hasNextPage) {

                binding.rvSharedMoments.let {

                    if (it.bottom - (binding.scrollView.height + binding.scrollView.scrollY) == 0)
                        allusermoments1(width,size,10,endCursor)
                }

            }
        })
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

    override fun setupClickListeners() {
        binding.toolbarHamburger.setOnClickListener {
            getMainActivity().drawerSwitchState()
        }

        binding.bell.setOnClickListener {
            val dialog = NotificationDialogFragment(
                userToken,
                binding.counter,
                userId,
                binding.bell
            )
            dialog.show(childFragmentManager, "notifications")
        }


        GiftbottomSheetBehavior = BottomSheetBehavior.from<ConstraintLayout>(binding.giftbottomSheet)
        GiftbottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
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
                }
            }
        })

        binding.sendgiftto.setOnClickListener(View.OnClickListener {

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
                        if (res!!.hasErrors() == false) {
//                                views.snackbar("You bought ${res.data?.giftPurchase?.giftPurchase?.gift?.giftName} successfully!")
                            Toast.makeText(requireContext(),"You bought ${res.data?.giftPurchase?.giftPurchase?.gift?.giftName} successfully!",
                                Toast.LENGTH_LONG).show()

                            fireGiftBuyNotificationforreceiver(gift.id,giftUserid)

                        }
                        if(res.hasErrors())
                        {
                            if(res.errors!![0].message.equals("User doesn't exist"))
                            {
                                Toast.makeText(requireContext(),""+ res.errors!![0].message, Toast.LENGTH_LONG).show()

                                Handler().postDelayed({ nouserexist() }, 1500)




                            }
                            else {
                                Toast.makeText(requireContext(),""+ res.errors!![0].message, Toast.LENGTH_LONG).show()

                            }

                        }
                    }
                    hideProgressView()
                }
            }

        })


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
        setupViewPager(binding.giftsPager)

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

    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = UserItemsAdapter(childFragmentManager)
        fragRealGifts = FragmentRealGifts()
        fragVirtualGifts = FragmentVirtualGifts()

        adapter.addFragItem(fragRealGifts!!, getString(R.string.real_gifts))
        adapter.addFragItem(fragVirtualGifts!!, getString(R.string.virtual_gifts))
        viewPager.adapter = adapter
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val arguments = arguments
        if (arguments != null) {
            ShowNotification = arguments.get("ShowNotification") as String?

            if (ShowNotification.equals("true")) {


                Handler().postDelayed({ binding.bell.performClick() }, 500)


            }


        }
        return super.onCreateView(inflater, container, savedInstanceState)

    }

    private fun  getAllUserMoments(width: Int, size: Int) {

        showProgressView()
        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(requireContext(), userToken!!).query(GetAllUserMomentsQuery(width,size,10,"","")).execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                binding.root.snackbar("Exception all moments ${e.message}")
                hideProgressView()
                return@launchWhenResumed
            }

            hideProgressView()

            if(res.hasErrors())
            {
                if(res.errors!![0].message.equals("User doesn't exist getAllUserMoments"))
                {
                    Toast.makeText(requireContext(),""+ res.errors!![0].message, Toast.LENGTH_LONG).show()

                    Handler().postDelayed({ nouserexist() }, 1500)


                }
                else {
                    Toast.makeText(requireContext(),""+ res.errors!![0].message, Toast.LENGTH_LONG).show()

                }

            }
            if (res.hasErrors() == false) {
                val allmoments = res.data?.allUserMoments!!.edges
                if (allmoments.size != 0) {
                    endCursor = res.data?.allUserMoments!!.pageInfo.endCursor!!
                    hasNextPage = res.data?.allUserMoments!!.pageInfo.hasNextPage!!

                    val allUserMomentsFirst: ArrayList<GetAllUserMomentsQuery.Edge> = ArrayList()

                    allmoments.indices.forEach { i ->
                        allUserMomentsFirst.add(allmoments[i]!!)
                    }

                    sharedMomentAdapter.addAll(allUserMomentsFirst)

                    binding.rvSharedMoments.adapter = sharedMomentAdapter
                }

                if (binding.rvSharedMoments.itemDecorationCount == 0) {
                    binding.rvSharedMoments.addItemDecoration(object :
                        RecyclerView.ItemDecoration() {
                        override fun getItemOffsets(
                            outRect: Rect,
                            view: View,
                            parent: RecyclerView,
                            state: RecyclerView.State
                        ) {
                            outRect.top = 15
                        }
                    })
                }
                if (allmoments.size > 0) {
                    Timber.d("apolloResponse: ${allmoments.get(0)?.node!!.file}")
                    Timber.d("apolloResponse: ${allmoments.get(0)?.node!!.id}")
                    Timber.d("apolloResponse: ${allmoments.get(0)?.node!!.createdDate}")
                    Timber.d("apolloResponse: ${allmoments.get(0)?.node!!.momentDescriptionPaginated}")
                    Timber.d("apolloResponse: ${allmoments.get(0)?.node!!.user?.fullName}")
                }
            }
        }
    }

    private fun getNotificationIndex() {

        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(requireContext(), userToken!!).query(GetNotificationCountQuery())
                    .execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                binding.root.snackbar("Exception NotificationIndex ${e.message}")
                return@launchWhenResumed
            }
            Timber.d("apolloResponse NotificationIndex ${res.hasErrors()}")

            if(res.hasErrors())
            {
                if(res.errors!![0].message.equals("User doesn't exist"))
                {
                    Toast.makeText(requireContext(),""+ res.errors!![0].message, Toast.LENGTH_LONG).show()

                    Handler().postDelayed({ nouserexist() }, 1500)


                }
                else {
                    Toast.makeText(requireContext(),""+ res.errors!![0].message, Toast.LENGTH_LONG).show()

                }

            }
            if (res.hasErrors() == false) {
                val NotificationCount = res.data?.unseenCount
                if (NotificationCount == null || NotificationCount == 0) {
                    binding.counter.visibility = View.GONE
                    binding.bell.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.notification1
                        )
                    )

                } else {
                    binding.counter.visibility = View.VISIBLE
                    binding.counter.setText("" + NotificationCount)
                    binding.bell.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.notification2
                        )
                    )


                }
            }

        }
    }

    override fun onResume() {
        getNotificationIndex()
        super.onResume()
    }

    private fun getAllUserStories() {

         showProgressView()
        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(requireContext(), userToken!!).query(GetAllUserStoriesQuery(100,"",""))
                    .execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                binding.root.snackbar("Exception all stories ${e.message}")
                hideProgressView()
                return@launchWhenResumed
            }
            Timber.d("apolloResponse allUserStories stories ${res.hasErrors()}")

            hideProgressView()


            if(res.hasErrors())
            {
                if(res.errors!![0].message.equals("User doesn't exist getAllUserStories"))
                {
                    Toast.makeText(requireContext(),""+ res.errors!![0].message, Toast.LENGTH_LONG).show()

                    Handler().postDelayed({ nouserexist() }, 1500)


                }
                else {
                    Toast.makeText(requireContext(),""+ res.errors!![0].message, Toast.LENGTH_LONG).show()

                }

            }
            if (res.hasErrors() == false) {
                val allUserStories = res.data?.allUserStories!!.edges.also {


                    usersAdapter =
                        UserStoriesAdapter(requireActivity(), this@UserMomentsFragment, it)
                }
                binding.rvUserStories.adapter = usersAdapter
                if (binding.rvUserStories.itemDecorationCount == 0) {
                    binding.rvUserStories.addItemDecoration(object : RecyclerView.ItemDecoration() {
                        override fun getItemOffsets(
                            outRect: Rect,
                            view: View,
                            parent: RecyclerView,
                            state: RecyclerView.State
                        ) {
                            outRect.top = 20
                            outRect.bottom = 10
                            outRect.left = 20
                        }
                    })
                }

                if (allUserStories?.size!! > 0) {
                    Timber.d("apolloResponse: stories ${allUserStories?.size}")
                    Timber.d("apolloResponse: stories ${allUserStories?.get(0)?.node!!.file}")
                    Timber.d("apolloResponse: stories ${allUserStories?.get(0)?.node!!.id}")
                    Timber.d("apolloResponse: stories ${allUserStories?.get(0)?.node!!.createdDate}")
                }
            }
        }
    }

    private fun uploadStory() {

        showProgressView()
        lifecycleScope.launchWhenCreated {

            val f = File(mFilePath!!)
            val buildder = DefaultUpload.Builder()
            buildder.contentType("Content-Disposition: form-data;")
            buildder.fileName(f.name)
            val upload = buildder.content(f).build()
            Timber.d("filee ${f.exists()} ${f.length()}")
            val userToken = getCurrentUserToken()!!

            val res = try {
                apolloClient(context = requireContext(), token = userToken).mutation(
                    StoryMutation(file = upload)
                ).execute()
            } catch (e: ApolloException) {
                Timber.d("filee Apollo Exception ${e.message}")
                binding.root.snackbar("ApolloException ${e.message}")
                return@launchWhenCreated
            } catch (e: Exception) {

                Timber.d("filee General Exception ${e.message} $userToken")
                binding.root.snackbar("Exception ${e.message}")
            } finally {
                hideProgressView()
            }
            Timber.d("filee hasError= ${res}")

//            if(res.hasErrors())
//            {
//                if(res.errors!![0].message.equals("User doesn't exist"))
//                {
//                    Toast.makeText(requireContext(),""+ res.errors!![0].message, Toast.LENGTH_LONG).show()
//
//                    lifecycleScope.launch(Dispatchers.IO) {
//                        App.userPreferences.clear()
//                    }
//                    viewModel.logOut(getCurrentUserId()!!, getCurrentUserToken()!!) {
//
//
//                        lifecycleScope.launch(Dispatchers.Main) {
//                            userPreferences.clear()
//                            val intent = Intent(requireActivity(), SplashActivity::class.java)
//                            startActivity(intent)
//                            requireActivity().finishAffinity()
//                        }
//
//                    }
//                }
//                else {
//                    Toast.makeText(requireContext(),""+ res.errors!![0].message, Toast.LENGTH_LONG).show()
//
//                }
//
//            }
//            if (res.hasErrors() == false) {
                getAllUserStories()
//            }
        }
    }

    override fun onUserStoryClick(position: Int, userStory: GetAllUserStoriesQuery.Edge?) {

        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        //findNavController().navigate(R.id.action_user_story_detail_fragment)
        //requireActivity().openUserStoryDialog(userStory)
        Timber.d("filee ${userStory?.node!!.fileType} ${userStory?.node.file}")
        val url = "${BuildConfig.BASE_URL}media/${userStory.node.file}"
        var userurl = ""
        if(userStory.node!!.user!!.avatar != null && userStory.node.user!!.avatar!!.url != null)
        {
            userurl = userStory.node.user.avatar!!.url!!

        }
        else
        {
            userurl = ""

        }
        val username = userStory.node.user!!.fullName
        val UserID = userId
        val objectId = userStory.node.pk


        var text = userStory.node.createdDate.toString()
        text = text.replace("T", " ").substring(0, text.indexOf("."))
        val momentTime = formatter.parse(text)
        val times = DateUtils.getRelativeTimeSpanString(momentTime.time, Date().time, DateUtils.MINUTE_IN_MILLIS)
        if (userStory.node.fileType.equals("video")) {
            val dialog = PlayUserStoryDialogFragment()
            val b = Bundle()

            b.putString("Uid", UserID)
            b.putString("url", url)
            b.putString("userurl", userurl)
            b.putString("username", username)
            b.putString("times", times.toString())
            b.putString("token", userToken)
            b.putInt("objectID", objectId!!)



            dialog.arguments = b
            dialog.show(childFragmentManager, "story")

        } else {
            val dialog = UserStoryDetailFragment()

            val b = Bundle()
            b.putString("Uid", UserID)
            b.putString("url", url)
            b.putString("userurl", userurl)
            b.putString("username", username)
            b.putString("times", times.toString())
            b.putString("token", userToken)
            b.putInt("objectID", objectId!!)


            dialog.arguments = b
            dialog.show(childFragmentManager, "story")
        }
    }

    override fun onAddNewUserStoryClick() {
        val intent = Intent(requireActivity(), ImagePickerActivity::class.java)
        photosLauncher.launch(intent)
        //val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        //videoLauncher.launch(galleryIntent)
    }

    override fun onSharedMomentClick(position: Int, item: GetAllUserMomentsQuery.Edge?) {

    }

    override fun onLikeofMomentClick(position: Int, item: GetAllUserMomentsQuery.Edge?) {

        showProgressView()
        lifecycleScope.launchWhenResumed {
            userName = getCurrentUserName()!!
            userId = getCurrentUserId()!!

            val res = try {
                apolloClient(
                    requireContext(),
                    userToken!!
                ).mutation(LikeOnMomentMutation(item?.node!!.pk!!.toString()))
                    .execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                binding.root.snackbar("Exception ${e.message}")
                hideProgressView()
                return@launchWhenResumed
            }


            hideProgressView()


            if(res.hasErrors())
            {
                if(res.errors!![0].message.equals("User doesn't exist"))
                {
                    Toast.makeText(requireContext(),""+ res.errors!![0].message, Toast.LENGTH_LONG).show()

                    Handler().postDelayed({ nouserexist() }, 1500)


                }
                else {
                    Toast.makeText(requireContext(),""+ res.errors!![0].message, Toast.LENGTH_LONG).show()

                }

            }
            if (res.hasErrors() == false) {
                Timber.d("Size", "" + allUserMoments.size)


                fireLikeNotificationforreceiver(item)

                getParticularMoments(position, item.node.pk.toString())


            }


        }

    }

    private fun getParticularMoments(pos: Int, ids: String) {


        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(requireContext(), userToken!!).query(GetAllUserMomentsQuery(width,size,1,"",ids))
                    .execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                binding.root.snackbar("Exception all moments ${e.message}")
                return@launchWhenResumed
            }


            if(res.hasErrors())
            {
                if(res.errors!![0].message.equals("User doesn't exist"))
                {
                    Toast.makeText(requireContext(),""+ res.errors!![0].message, Toast.LENGTH_LONG).show()

                    Handler().postDelayed({ nouserexist() }, 1500)


                }
                else {
                    Toast.makeText(requireContext(),""+ res.errors!![0].message, Toast.LENGTH_LONG).show()

                }

            }
            if (res.hasErrors() == false) {
                val allmoments = res.data?.allUserMoments!!.edges

                allmoments.indices.forEach { i ->
                    if (ids.equals(allmoments[i]!!.node!!.pk.toString())) {
                        allUserMoments[pos] = allmoments[i]!!
                        sharedMomentAdapter.notifyItemChanged(pos)
                        return@forEach
                    }


                }
            }

        }
    }


    fun fireLikeNotificationforreceiver(item: GetAllUserMomentsQuery.Edge) {

        lifecycleScope.launchWhenResumed {


            val queryName = "sendNotification"
            val query = StringBuilder()
                .append("mutation {")
                .append("$queryName (")
                .append("userId: \"${item.node!!.user!!.id}\", ")
                .append("notificationSetting: \"LIKE\", ")
                .append("data: {momentId:${item.node.pk}}")
                .append(") {")
                .append("sent")
                .append("}")
                .append("}")
                .toString()

            val result= provideGraphqlApi().getResponse<Boolean>(
                query,
                queryName, userToken)
            Timber.d("RSLT",""+result.message)

        }








    }




    fun allusermoments1(width: Int, size: Int, i: Int, endCursors: String) {
        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(requireContext(), userToken!!).query(GetAllUserMomentsQuery(width,size,i,endCursors,""))
                    .execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                binding.root.snackbar("Exception all moments ${e.message}")
                return@launchWhenResumed
            }


            if(res.hasErrors())
            {
                if(res.errors!![0].message.equals("User doesn't exist"))
                {
                    Toast.makeText(requireContext(),""+ res.errors!![0].message, Toast.LENGTH_LONG).show()


                    Handler().postDelayed({ nouserexist() }, 1500)


                }
                else {
                    Toast.makeText(requireContext(),""+ res.errors!![0].message, Toast.LENGTH_LONG).show()

                }

            }
            if (res.hasErrors() == false) {
                val allusermoments = res.data?.allUserMoments!!.edges


                endCursor = res.data?.allUserMoments!!.pageInfo.endCursor!!
                hasNextPage = res.data?.allUserMoments!!.pageInfo.hasNextPage


                if (allusermoments.size != 0) {
                    val allUserMomentsNext: ArrayList<GetAllUserMomentsQuery.Edge> = ArrayList()

                    allusermoments.indices.forEach { i ->


                        allUserMomentsNext.add(allusermoments[i]!!)
                    }

                    sharedMomentAdapter.addAll(allUserMomentsNext)


                }


                if (binding.rvSharedMoments.itemDecorationCount == 0) {
                    binding.rvSharedMoments.addItemDecoration(object :
                        RecyclerView.ItemDecoration() {
                        override fun getItemOffsets(
                            outRect: Rect,
                            view: View,
                            parent: RecyclerView,
                            state: RecyclerView.State
                        ) {
                            outRect.top = 15
                        }
                    })
                }
                if (allusermoments?.size!! > 0) {
                    Timber.d("apolloResponse: ${allusermoments?.get(0)?.node!!.file}")
                    Timber.d("apolloResponse: ${allusermoments?.get(0)?.node!!.id}")
                    Timber.d("apolloResponse: ${allusermoments?.get(0)?.node!!.createdDate}")
                    Timber.d("apolloResponse: ${allusermoments?.get(0)?.node!!.momentDescriptionPaginated}")
                    Timber.d("apolloResponse: ${allusermoments?.get(0)?.node!!.user?.fullName}")
                }
            }
        }
    }

    override fun onCommentofMomentClick(

        position: Int, item: GetAllUserMomentsQuery.Edge?
    ) {
        val bundle = Bundle().apply {
            putString("momentID", item?.node!!.pk.toString())
            putString("filesUrl", item.node.file!!)
            putString("Likes", item.node.like!!.toString())
            putString("Comments", item.node.comment!!.toString())
            val gson = Gson()
            putString("Desc",gson.toJson(item.node.momentDescriptionPaginated))
            putString("fullnames", item.node.user!!.fullName)
            if(item.node.user.gender != null)
            {
                putString("gender", item.node.user.gender!!.name)
            }
            else
            {
                putString("gender", null)
            }
            putString("momentuserID", item.node.user.id.toString())



        }
        navController.navigate(R.id.momentsAddCommentFragment, bundle)
    }

    override fun onMomentGiftClick(position: Int, item: GetAllUserMomentsQuery.Edge?) {
//        var bundle = Bundle()
//        bundle.putString("userId", userId)
//        navController.navigate(R.id.action_userProfileFragment_to_userGiftsFragment,bundle)

        if(!userId!!.equals(item!!.node!!.user!!.id))
        {
            giftUserid = item.node!!.user!!.id.toString()
            binding.sendgiftto.text = "SEND GIFT TO "+ item!!.node!!.user!!.fullName
            if (GiftbottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                GiftbottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
//            buttonSlideUp.text = "Slide Down";

            } else {
                GiftbottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED;
//            buttonSlideUp.text = "Slide Up"

            }
        }
        else
        {

            Toast.makeText(requireContext(),"User can't bought gift yourself", Toast.LENGTH_LONG).show()

        }


    }


    override fun onDotMenuofMomentClick(
        position: Int,
        item: GetAllUserMomentsQuery.Edge?, types: String
    ) {

        if (types.equals("delete")) {

            showProgressView()
            lifecycleScope.launchWhenResumed {
                val res = try {
                    apolloClient(
                        requireContext(),
                        userToken!!
                    ).mutation(DeletemomentMutation(item?.node!!.pk!!.toString()))
                        .execute()
                } catch (e: ApolloException) {
                    Timber.d("apolloResponse ${e.message}")
                    binding.root.snackbar("Exception ${e.message}")
                    hideProgressView()
                    return@launchWhenResumed
                }


                hideProgressView()

                if(res.hasErrors())
                {
                    if(res.errors!![0].message.equals("User doesn't exist"))
                    {
                        Toast.makeText(requireContext(),""+ res.errors!![0].message, Toast.LENGTH_LONG).show()

                        Handler().postDelayed({ nouserexist() }, 1500)


                    }
                    else {
                        Toast.makeText(requireContext(),""+ res.errors!![0].message, Toast.LENGTH_LONG).show()

                    }

                }

                if (res.hasErrors() == false) {

                    val positionss = allUserMoments.indexOf(item)
                    allUserMoments.remove(item)
                    sharedMomentAdapter.notifyItemRemoved(position)
                }
            }
        } else if (types.equals("report")) {
            showProgressView()
            lifecycleScope.launchWhenResumed {
                val res = try {
                    apolloClient(
                        requireContext(),
                        userToken!!
                    ).mutation(ReportonmomentMutation(item?.node!!.pk!!.toString(), "This is not valid post"))
                        .execute()
                } catch (e: ApolloException) {
                    Timber.d("apolloResponse ${e.message}")
                    binding.root.snackbar("Exception ${e.message}")
                    hideProgressView()
                    return@launchWhenResumed
                }

                hideProgressView()

                if(res.hasErrors())
                {
                    if(res.errors!![0].message.equals("User doesn't exist"))
                    {
                        Toast.makeText(requireContext(),""+ res.errors!![0].message, Toast.LENGTH_LONG).show()

                        Handler().postDelayed({ nouserexist() }, 1500)


                    }
                    else {
                        Toast.makeText(requireContext(),""+ res.errors!![0].message, Toast.LENGTH_LONG).show()

                    }

                }
            }
        }


    }


    override fun onMoreShareMomentClick() {

    }


    fun getMainActivity() = activity as MainActivity
}