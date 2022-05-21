package com.i69app.ui.screens.main.profile.subitems

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo3.exception.ApolloException
import com.google.gson.Gson
import com.i69app.*
import com.i69app.data.models.User
import com.i69app.databinding.FragmentFeedBinding
import com.i69app.di.modules.AppModule
import com.i69app.singleton.App
import com.i69app.ui.adapters.CurrentUserMomentAdapter
import com.i69app.ui.adapters.UserStoriesAdapter
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.screens.SplashActivity
import com.i69app.ui.viewModels.UserViewModel
import com.i69app.utils.EXTRA_USER_MODEL
import com.i69app.utils.apolloClient
import com.i69app.utils.getResponse
import com.i69app.utils.snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.roundToInt

class MomentsFragment : BaseFragment<FragmentFeedBinding>() , UserStoriesAdapter.UserStoryListener,
    CurrentUserMomentAdapter.CurrentUserMomentListener {

    private var userToken: String? = null
    private lateinit var currentUserMomentAdapter: CurrentUserMomentAdapter
    private val viewModel: UserViewModel by activityViewModels()

    var user: User? = null

    var width = 0
    var size = 0
//    private var id_s: String? = null

    private var userId: String? = null
    private var userName: String? = null

    var endCursor: String=""
    var hasNextPage: Boolean= false
    var allUserMoments: ArrayList<GetUserMomentsQuery.Edge> = ArrayList()
    var layoutManager: LinearLayoutManager? = null

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentFeedBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        navController = findNavController()
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        width = displayMetrics.widthPixels

        val densityMultiplier =getResources().getDisplayMetrics().density;
        val scaledPx = 14 * densityMultiplier;
        val paint = Paint()
        paint.setTextSize(scaledPx);
        size = paint.measureText("s").roundToInt();

        setUpData()
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
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        val arguments = arguments
//        if (arguments != null) {
//            id_s = arguments.get("ID") as String?
//        }

         user = (Gson().fromJson(arguments?.getString(EXTRA_USER_MODEL), User::class.java) ?: "") as User?

        return super.onCreateView(inflater, container, savedInstanceState)

    }

    private fun setUpData() {
        lifecycleScope.launch {
            userId = getCurrentUserId()!!
            userToken = getCurrentUserToken()!!
            userName = getCurrentUserName()
        }

        allUserMoments = ArrayList()

        currentUserMomentAdapter = CurrentUserMomentAdapter(
            requireActivity(),
            this,
            allUserMoments,
            userId
        )
        layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.rvMoments.setLayoutManager(layoutManager)

        getAllUserMoments(width,size)

        binding.scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (hasNextPage) {

                binding.rvMoments?.let {


                    if (it.bottom - (binding.scrollView.height + binding.scrollView.scrollY) == 0)
                        allusermoments1(width,size,10,endCursor)


                }

            }
        })

    }
    private fun getAllUserMoments(width: Int, size: Int) {


        if(user!!.id == "")
        {
            user!!.id = userId!!
        }
        lifecycleScope.launch() {
            val res = try {
                apolloClient(requireContext(), userToken!!).query(
                    GetUserMomentsQuery(width,size,10,"", user!!.id,"")
                ).execute()
            } catch (e: ApolloException) {
                Timber.d("apolloException currentUserMoments ${e.message}")

                return@launch
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
                if (allmoments.size != 0) {
                    endCursor = res.data?.allUserMoments!!.pageInfo.endCursor!!
                    hasNextPage = res.data?.allUserMoments!!.pageInfo.hasNextPage!!


                    val allUserMomentsFirst: ArrayList<GetUserMomentsQuery.Edge> = ArrayList()

                    allmoments.indices.forEach { i ->


                        allUserMomentsFirst.add(allmoments[i]!!)
                    }

                    currentUserMomentAdapter.addAll(allUserMomentsFirst)
                    binding.rvMoments.adapter = currentUserMomentAdapter
                }

                if (binding.rvMoments.itemDecorationCount == 0) {
                    binding.rvMoments.addItemDecoration(object : RecyclerView.ItemDecoration() {
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
            }
        }
    }


    override fun onLikeofMomentClick(position: Int, item: GetUserMomentsQuery.Edge) {



        showProgressView()
        lifecycleScope.launchWhenResumed {
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
                fireLikeNotificationforreceiver(item)

                getParticularMoments(position, item.node.pk.toString())

            }

        }

    }


    private fun getParticularMoments(pos: Int, ids: String) {


        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(requireContext(), userToken!!).query(GetUserMomentsQuery(width,size,1,"",user!!.id,ids))
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
                        allUserMoments[pos] = allmoments[i]!!
                        currentUserMomentAdapter.notifyItemChanged(pos)
                        return@forEach
                    }


                }

            }
        }
    }



    fun fireLikeNotificationforreceiver(item: GetUserMomentsQuery.Edge?) {


        lifecycleScope.launchWhenResumed {


            val queryName = "sendNotification"
            val query = StringBuilder()
                .append("mutation {")
                .append("$queryName (")
                .append("userId: \"${item!!.node!!.user!!.id}\", ")
                .append("notificationSetting: \"LIKE\", ")
                .append("data: {momentId:${item.node!!.pk}}")
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


    fun allusermoments1(width: Int, size: Int, i: Int, endCursors: String) {
        lifecycleScope.launchWhenResumed {
            val res = try {
                apolloClient(requireContext(), userToken!!).query(GetUserMomentsQuery(width,size,i,endCursors,
                    userId!!,""))
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

                val allusermoments = res.data?.allUserMoments!!.edges


                endCursor = res.data?.allUserMoments!!.pageInfo.endCursor!!
                hasNextPage = res.data?.allUserMoments!!.pageInfo.hasNextPage

                if (allusermoments.size != 0) {
                    val allUserMomentsNext: ArrayList<GetUserMomentsQuery.Edge> = ArrayList()


                    allusermoments.indices.forEach { i ->


                        allUserMomentsNext.add(allusermoments[i]!!)
                    }

                    currentUserMomentAdapter.addAll(allUserMomentsNext)

                }



                if (binding.rvMoments.itemDecorationCount == 0) {
                    binding.rvMoments.addItemDecoration(object : RecyclerView.ItemDecoration() {
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
                    Timber.d("apolloResponse: ${allusermoments?.get(0)?.node!!.file}")
                    Timber.d("apolloResponse: ${allusermoments?.get(0)?.node!!.id}")
                    Timber.d("apolloResponse: ${allusermoments?.get(0)?.node!!.createdDate}")
                    Timber.d("apolloResponse: ${allusermoments?.get(0)?.node!!.momentDescriptionPaginated}")
                    Timber.d("apolloResponse: ${allusermoments?.get(0)?.node!!.user?.fullName}")
                }
            }
            //binding.root.snackbar("apolloResponse ${allusermoments?.get(0)?.file}")
        }
    }

    override fun onCommentofMomentClick(
        position: Int,
        item: GetUserMomentsQuery.Edge
    ) {
        val bundle = Bundle()
        bundle.putString("momentID", item?.node!!.pk!!.toString())

        bundle.putString("filesUrl", item?.node!!.file!!)
        bundle.putString("Likes", item?.node!!.like!!.toString())
        bundle.putString("Comments", item?.node!!.comment!!.toString())
        val gson = Gson()
        bundle.putString("Desc",gson.toJson(item.node!!.momentDescriptionPaginated))
        if(item.node.user!!.gender != null)
        {
            bundle.putString("gender", item.node.user.gender!!.name)

        }
        else
        {
            bundle.putString("gender", null)

        }
        bundle.putString("fullnames",item.node!!.user!!.fullName)
        bundle.putString("momentuserID", item.node!!.user!!.id.toString())



        navController.navigate(R.id.momentsAddCommentFragment,bundle)
    }


    override fun onDotMenuofMomentClick(
        position: Int,
        item: GetUserMomentsQuery.Edge
        ,types: String) {

        if(types.equals("delete"))
        {

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
                    val positionss = allUserMoments.indexOf(item)
                    allUserMoments.remove(item)
                    currentUserMomentAdapter.notifyItemRemoved(position)
                }
            }
        }
        else if(types.equals("report"))
        {
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
            }
        }




    }

    override fun onMomentGiftClick(position: Int, item: GetUserMomentsQuery.Edge?) {
//        var bundle = Bundle()
//        bundle.putString("userId", userId)
//        navController.navigate(R.id.action_userProfileFragment_to_userGiftsFragment,bundle)
    }


    override fun onMoreShareMomentClick() {

    }
    override fun onSharedMomentClick(position: Int, item: GetUserMomentsQuery.Edge) {
    }



    override fun onUserStoryClick(position: Int, userStory: GetAllUserStoriesQuery.Edge?) {
    }

    override fun onAddNewUserStoryClick() {
    }
}