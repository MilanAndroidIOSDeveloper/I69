package com.i69app.ui.screens.main.notification

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo3.exception.ApolloException
import com.google.android.material.textview.MaterialTextView
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.i69app.*
import com.i69app.databinding.DialogNotificationsBinding
import com.i69app.singleton.App
import com.i69app.singleton.App.Companion.userPreferences
import com.i69app.ui.adapters.NotificationsAdapter
import com.i69app.ui.adapters.UserStoriesAdapter
import com.i69app.ui.screens.SplashActivity
import com.i69app.ui.screens.main.moment.PlayUserStoryDialogFragment
import com.i69app.ui.screens.main.moment.UserStoryDetailFragment
import com.i69app.ui.viewModels.UserViewModel
import com.i69app.utils.apolloClient
import com.i69app.utils.snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class NotificationDialogFragment(
    var userToken: String?,
    var Counters: MaterialTextView,
    var userId: String?,
    var bell: ImageView
) :
    DialogFragment(), NotificationsAdapter.NotificationListener {

    private lateinit var binding: DialogNotificationsBinding
    private lateinit var adapter: NotificationsAdapter
    var endCursor: String=""
    var hasNextPage: Boolean= false
    var navController: NavController? = null
    var allnotification: ArrayList<GetAllNotificationQuery.Edge> = ArrayList()
    var layoutManager: LinearLayoutManager? = null

    var width = 0
    var size = 0
    private val viewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View
    {
        binding = DialogNotificationsBinding.inflate(inflater, container, false)
        binding.btnCloseNotifications.setOnClickListener { dismiss() }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        navController = findNavController()
        allnotification = ArrayList()
        adapter = NotificationsAdapter(
            requireActivity(),
            this@NotificationDialogFragment,
            allnotification
        )
        layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.rvNotifications.setLayoutManager(layoutManager)

        getAllNotifications()

        binding.scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (hasNextPage) {

                binding.rvNotifications.let {


                    if (it.bottom - (binding.scrollView.height + binding.scrollView.scrollY) == 0)
                        getAllNotificationsNext(10,endCursor)


                }

            }
        })



    }
    private fun  getAllNotifications() {

        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(
                    requireContext(),
                    userToken!!
                ).query(GetAllNotificationQuery(10,""))
                    .execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                binding.root.snackbar("Exception ${e.message}")
                return@launchWhenResumed
            }
            if (res.hasErrors()) {

                if(res.errors!![0].message.equals("User doesn't exist"))
                {
                    binding.root.snackbar("" + res.errors!![0].message)

                   Handler().postDelayed({ nouserexist() }, 1500)
                }
                else {
                    binding.root.snackbar("" + res.errors!![0].message)
                }

            }
            if (res.hasErrors() == false) {
                val allnotification = res.data?.notifications!!.edges
                if (allnotification != null && allnotification.size != 0) {
                    endCursor = res.data?.notifications!!.pageInfo.endCursor!!
                    hasNextPage = res.data?.notifications!!.pageInfo.hasNextPage

                    if (allnotification.size > 0) {
                        binding.noData.visibility = View.GONE
                        binding.rvNotifications.visibility = View.VISIBLE

                        val allUserMomentsFirst: ArrayList<GetAllNotificationQuery.Edge> =
                            ArrayList()


                        allnotification.indices.forEach { i ->

                            allUserMomentsFirst.add(allnotification[i]!!)
                        }

                        adapter.addAll(allUserMomentsFirst)


                        binding.rvNotifications.adapter = adapter
                        if (binding.rvNotifications.itemDecorationCount == 0) {
                            binding.rvNotifications.addItemDecoration(object :
                                RecyclerView.ItemDecoration() {
                                override fun getItemOffsets(
                                    outRect: Rect,
                                    view: View,
                                    parent: RecyclerView,
                                    state: RecyclerView.State
                                ) {
                                    outRect.top = 20
                                    outRect.bottom = 10
                                    outRect.left = 20
                                    outRect.right = 20
                                }
                            })
                        }
                        getNotificationIndex()
                    } else {
                        binding.noData.visibility = View.VISIBLE
                        binding.rvNotifications.visibility = View.GONE

                    }

                } else {
                    binding.noData.visibility = View.VISIBLE
                    binding.rvNotifications.visibility = View.GONE
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

    fun getAllNotificationsNext( i: Int, endCursors: String) {
        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(requireContext(), userToken!!).query(GetAllNotificationQuery(i,endCursors))
                    .execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                binding.root.snackbar("Exception ${e.message}")
                return@launchWhenResumed
            }
            if (res.hasErrors()) {

                if(res.errors!![0].message.equals("User doesn't exist"))
                {
                    binding.root.snackbar("" + res.errors!![0].message)

                    Handler().postDelayed({ nouserexist() }, 1500)
                }
                else {
                    binding.root.snackbar("" + res.errors!![0].message)
                }

            }
            if (res.hasErrors() == false) {
                val allnotification = res.data?.notifications!!.edges
                endCursor = res.data?.notifications!!.pageInfo.endCursor!!
                hasNextPage = res.data?.notifications!!.pageInfo.hasNextPage

                if (allnotification.size != 0) {

                    val allUserMomentsNext: ArrayList<GetAllNotificationQuery.Edge> = ArrayList()


                    allnotification.indices.forEach { i ->


                        allUserMomentsNext.add(allnotification[i]!!)
                    }

                    adapter.addAll(allUserMomentsNext)

                }



                if (binding.rvNotifications.itemDecorationCount == 0) {
                    binding.rvNotifications.addItemDecoration(object :
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
                if (allnotification?.size!! > 0) {
                    Timber.d("apolloResponse: ${allnotification?.get(0)?.node!!.id}")
                    Timber.d("apolloResponse: ${allnotification?.get(0)?.node!!.createdDate}")
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


            if (res.hasErrors()) {

                if(res.errors!![0].message.equals("User doesn't exist"))
                {
                    binding.root.snackbar("" + res.errors!![0].message)

                    Handler().postDelayed({ nouserexist() }, 1500)
                }
                else {
                    binding.root.snackbar("" + res.errors!![0].message)
                }

            }
            if (res.hasErrors() == false) {
                Timber.d("apolloResponse NotificationIndex ${res.hasErrors()}")

                val NotificationCount = res.data?.unseenCount
                if (NotificationCount == null || NotificationCount == 0) {
                    Counters.visibility = View.GONE
                    bell.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.notification1
                        )
                    )

                } else {
                    Counters.visibility = View.VISIBLE
                    Counters.setText("" + NotificationCount)
                    bell.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.notification2
                        )
                    )

                }
            }

        }
    }

    private fun getMoments(ids: String) {


        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(requireContext(), userToken!!).query(GetAllUserParticularMomentsQuery(width,size,ids))
                    .execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                binding.root.snackbar("Exception all moments ${e.message}")
                return@launchWhenResumed
            }




            if (res.hasErrors()) {

                if(res.errors!![0].message.equals("User doesn't exist"))
                {
                    binding.root.snackbar("" + res.errors!![0].message)

                    Handler().postDelayed({ nouserexist() }, 1500)
                }
                else {
                    binding.root.snackbar("" + res.errors!![0].message)
                }

            }
            if (res.hasErrors() == false) {
                val allmoments = res.data?.allUserMoments!!.edges


                allmoments.indices.forEach { i ->
                    if (ids.equals(allmoments[i]!!.node!!.pk.toString())) {
                        val bundle = Bundle().apply {
                            putString("momentID", allmoments[i]?.node!!.pk!!.toString())
                            putString("filesUrl", allmoments[i]?.node!!.file!!)
                            putString("Likes", allmoments[i]?.node!!.like!!.toString())
                            putString("Comments", allmoments[i]?.node!!.comment!!.toString())
                            val gson = Gson()
                            putString(
                                "Desc",
                                gson.toJson(allmoments[i]?.node!!.momentDescriptionPaginated)
                            )
                            putString("fullnames", allmoments[i]?.node!!.user!!.fullName)
                            if (allmoments[i]!!.node!!.user!!.gender != null) {
                                putString("gender", allmoments[i]!!.node!!.user!!.gender!!.name)

                            } else {
                                putString("gender", null)

                            }
                            putString("momentuserID", allmoments[i]?.node!!.user!!.id.toString())
                        }
                        navController!!.navigate(R.id.momentsAddCommentFragment, bundle)

                        return@forEach
                    }


                }

            }
        }
    }

    private fun getStories(pkid: String) {

        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(requireContext(), userToken!!).query(GetAllUserStoriesQuery(100,"",pkid))
                    .execute()
            } catch (e: ApolloException) {
                Timber.d("apolloResponse ${e.message}")
                binding.root.snackbar("Exception all stories ${e.message}")
                return@launchWhenResumed
            }

            if (res.hasErrors()) {

                if(res.errors!![0].message.equals("User doesn't exist"))
                {
                    binding.root.snackbar("" + res.errors!![0].message)

                    Handler().postDelayed({ nouserexist() }, 1500)
                }
                else {
                    binding.root.snackbar("" + res.errors!![0].message)
                }

            }

            if (res.hasErrors() == false) {

                Timber.d("apolloResponse allUserStories stories ${res.hasErrors()}")

                val allUserStories = res.data?.allUserStories!!.edges

                allUserStories.indices.forEach { i ->
                    if (pkid.equals(allUserStories[i]!!.node!!.pk.toString())) {

                        val userStory = allUserStories[i]

                        val formatter =
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }

                        Timber.d("filee ${userStory?.node!!.fileType} ${userStory?.node.file}")
                        val url = "${BuildConfig.BASE_URL}media/${userStory?.node.file}"
                        var userurl = ""
                        if (userStory!!.node!!.user!!.avatar != null && userStory.node.user!!.avatar!!.url != null) {
                            userurl = userStory.node.user.avatar!!.url!!

                        } else {
                            userurl = ""

                        }
                        val username = userStory.node.user!!.fullName
                        val UserID = userId
                        val objectId = userStory.node.pk

                        var text = userStory.node.createdDate.toString()
                        text = text.replace("T", " ").substring(0, text.indexOf("."))
                        val momentTime = formatter.parse(text)
                        val times = DateUtils.getRelativeTimeSpanString(
                            momentTime.time,
                            Date().time,
                            DateUtils.MINUTE_IN_MILLIS
                        )
                        if (userStory?.node.fileType.equals("video")) {
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



                        return@forEach
                    }


                }
            }
        }
    }


    override fun onNotificationClick(
        position: Int,
        notificationdata: GetAllNotificationQuery.Edge?
    ) {

        val titles= notificationdata!!.node!!.notificationSetting!!.title


        if(titles.equals("Moment Liked")|| titles.equals("Comment in moment"))
        {
            if (notificationdata.node!!.data != null) {
                val resp: JsonObject = JsonParser().parse(notificationdata!!.node!!.data.toString()).asJsonObject


                val momentid = resp.get("momentId").toString()


                getMoments(momentid)
            }
        }

        else if(titles.equals("Story liked"))
        {
            if (notificationdata.node!!.data != null) {
                val resp: JsonObject = JsonParser().parse(notificationdata.node.data.toString()).asJsonObject


                val pkid = resp.get("pk").toString()
                getStories(pkid)

            }
        }
        else if(titles.equals("Story Commented"))
        {
            if (notificationdata.node!!.data != null) {
                val resp: JsonObject = JsonParser().parse(notificationdata.node.data.toString()).asJsonObject


                val pkid = resp.get("pk").toString()
                getStories(pkid)


            }
        }
        else if(titles.equals("Sent message"))
        {
            if (notificationdata.node!!.data != null) {
                val resp: JsonObject = JsonParser().parse(notificationdata.node.data.toString()).asJsonObject


                val roomid = resp.get("id").toString()
                val bundle = Bundle()
                bundle.putString("roomIDNotify", roomid)
                navController!!.navigate(R.id.messengerListFragment, bundle)


            }
        }
        else if(titles.equals("Gift received"))
        {
            navController!!.navigate(R.id.action_global_user_profile)

        }





    }
}