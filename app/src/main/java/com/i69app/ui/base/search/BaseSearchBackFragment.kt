package com.i69app.ui.base.search

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.apollographql.apollo3.exception.ApolloException
import com.i69app.GetNotificationCountQuery
import com.i69app.R
import com.i69app.databinding.FragmentSearchInterestedInBinding
import com.i69app.singleton.App
import com.i69app.ui.adapters.SearchInterestedAdapter
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.screens.SplashActivity
import com.i69app.ui.screens.main.MainActivity
import com.i69app.ui.screens.main.notification.NotificationDialogFragment
import com.i69app.ui.viewModels.UserViewModel
import com.i69app.utils.apolloClient
import com.i69app.utils.snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class BaseSearchBackFragment : BaseFragment<FragmentSearchInterestedInBinding>(), SearchInterestedAdapter.SearchInterestedListener {

    private var userToken: String? = null
    private var userId: String? = null


    companion object {
        var showAnim = true
    }

    protected val viewModel: UserViewModel by activityViewModels()
    protected lateinit var adapter: SearchInterestedAdapter

    abstract fun setScreenTitle()

    abstract fun initDrawerStatus()

    abstract fun getItems(): List<SearchInterestedAdapter.MenuItem>

    abstract fun onAdapterItemClick(pos: Int)


    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentSearchInterestedInBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        setScreenTitle()
        navController = findNavController()

                lifecycleScope.launch {
            userToken = getCurrentUserToken()!!
                    userId = getCurrentUserId()!!

                    Timber.i("usertokenn $userToken")
        }

        adapter = SearchInterestedAdapter(0, getAnim(), this)
        showAnim = false
        adapter.setItems(getItems())
        binding.searchChoiceItems.adapter = adapter
        initDrawerStatus()
    }

        override fun onResume() {
        getNotificationIndex()
        super.onResume()
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

            if(res.hasErrors())
            {

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
        moveUp()
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
    }

    override fun onViewClick(pos: Int) {
        onAdapterItemClick(pos)
    }

    protected open fun getAnim(): Boolean = showAnim

    fun getMainActivity() = activity as MainActivity

}