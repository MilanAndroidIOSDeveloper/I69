package com.i69app.ui.screens.main.search.result

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
import com.apollographql.apollo3.exception.ApolloException
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.i69app.GetNotificationCountQuery
import dagger.hilt.android.AndroidEntryPoint
import com.i69app.R
import com.i69app.databinding.FragmentSearchResultBinding
import com.i69app.singleton.App
import com.i69app.ui.adapters.PageResultAdapter
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.screens.SplashActivity
import com.i69app.ui.screens.main.notification.NotificationDialogFragment
import com.i69app.ui.viewModels.UserViewModel
import com.i69app.utils.apolloClient
import com.i69app.utils.snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class SearchResultFragment : BaseFragment<FragmentSearchResultBinding>() {
    private var userToken: String? = null
    private var userId: String? = null
    private val viewModel: UserViewModel by activityViewModels()

    override fun getStatusBarColor() = R.color.toolbar_search_color

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentSearchResultBinding.inflate(inflater, container, false)

    override fun setupTheme() {

        lifecycleScope.launch {
             userToken = getCurrentUserToken()!!
            userId = getCurrentUserId()!!

            Timber.i("usertokenn $userToken")
        }
        Timber.i("usertokenn 2 $userToken")
        val tabs = arrayOf(getString(R.string.random), getString(R.string.popular), getString(R.string.most_active))
        val pagerAdapter = PageResultAdapter(this, tabs)
        binding.searchPageViewPager.apply {
            adapter = pagerAdapter
            offscreenPageLimit = 3
        }

        val tabConfigurationStrategy = TabLayoutMediator.TabConfigurationStrategy { tab: TabLayout.Tab, pos: Int ->
            tab.text = tabs[pos]
        }
        TabLayoutMediator(binding.slidingTabs, binding.searchPageViewPager, true, tabConfigurationStrategy).attach()
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

    override fun onPause() {
        super.onPause()
        binding.slidingTabs.clearOnTabSelectedListeners()
    }

}