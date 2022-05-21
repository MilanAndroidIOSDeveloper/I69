package com.i69app.ui.base.profile

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.i69app.R
import com.i69app.data.models.User
import com.i69app.data.remote.responses.DefaultPicker
import com.i69app.databinding.AlertFullImageBinding
import com.i69app.databinding.FragmentUserProfileBinding
import com.i69app.ui.adapters.UserItemsAdapter
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.screens.main.MainActivity
import com.i69app.ui.screens.main.profile.subitems.UserProfileAboutFragment
import com.i69app.ui.screens.main.profile.subitems.UserProfileInterestsFragment
import com.i69app.utils.*
import com.i69app.utils.EXTRA_USER_MODEL
import com.i69app.utils.loadImage
import com.i69app.utils.setViewGone

abstract class BaseUserProfileFragment : BaseFragment<FragmentUserProfileBinding>() {

    protected var defaultPicker: DefaultPicker? = null
    protected var mUser: User? = null

    abstract fun setupScreen()

    open fun onReportClick() {}

    open fun onSendMsgClick() {}

    open fun onInitChatClick() {}

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentUserProfileBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        navController = findNavController()
        setStatusBarColor(R.color.prmotGrey)
        binding.profileTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
                binding.userDataViewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
            }
        })

        val scrimColor = ContextCompat.getColor(requireContext(), R.color.user_toolbar_parallax)
        binding.userCollapseToolbar.setContentScrimColor(scrimColor)
        binding.userCollapseToolbar.setStatusBarScrimColor(Color.TRANSPARENT)
        binding.profileTabs.setupWithViewPager(binding.userDataViewPager)
        setupScreen()
    }

    override fun setupClickListeners() {
        binding.sendMsgBtn.setOnClickListener { onSendMsgClick() }
        //binding.initChatMsgBtn.setOnClickListener { onInitChatClick() }
        binding.actionBack.setOnClickListener { requireActivity().onBackPressed() }
        binding.actionMain.setOnClickListener { (activity as MainActivity).drawerSwitchState() }
        binding.actionEdit.setOnClickListener { navController.navigate(R.id.action_userProfileFragment_to_userEditProfileFragment) }
//        binding.actionGifts.setOnClickListener{ navController.navigate(R.id.action_userProfileFragment_to_userGiftsFragment) }
//        binding.actionGifts1.setOnClickListener{ navController.navigate(R.id.action_userProfileFragment_to_userGiftsFragment) }

        binding.actionReport.setOnClickListener { onReportClick() }
    }

    @SuppressLint("SetTextI18n")
    protected fun setupUserData(user: User, defaultPicker: DefaultPicker) {
        //binding.coinsAmt.text = user.purchaseCoins.toString()
        binding.userImgHeader.setImageListener { position, imageView ->
            user.avatarPhotos?.get(position)?.let { imageView.loadImage(it.url) }
            imageView.setOnClickListener {
                user.avatarPhotos?.get(position)?.let { it1 -> showImageDialog(it1.url) }
            }
        }
        binding.userImgHeader.pageCount = user.avatarPhotos?.size ?: 1
        setupViewPager(binding.userDataViewPager, user, defaultPicker)
    }

    private fun setupViewPager(viewPager: ViewPager, user: User?, defaultPicker: DefaultPicker) {
        if (activity == null) return

        val adapter = UserItemsAdapter(childFragmentManager)
        val about = UserProfileAboutFragment()
        val interests = UserProfileInterestsFragment()
        val userDataArgs = Bundle()
        if (user != null) userDataArgs.putString(EXTRA_USER_MODEL, Gson().toJson(user))
        userDataArgs.putString("default_picker", Gson().toJson(defaultPicker))
        about.arguments = userDataArgs
        interests.arguments = userDataArgs
        adapter.addFragItem(about, getString(R.string.about))
        adapter.addFragItem(interests, getString(R.string.interests))
        viewPager.adapter = adapter
    }

    private fun showImageDialog(imageUrl: String) {
        val dialog = Dialog(requireContext(), android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
        val dialogBinding = AlertFullImageBinding.inflate(layoutInflater, null, false)
        dialogBinding.fullImg.loadImage(imageUrl) {
            dialogBinding.alertTitle.setViewGone()
        }
        dialog.setContentView(dialogBinding.root)
        dialog.show()
        dialogBinding.root.setOnClickListener {
            dialog.cancel()
        }
    }

}