package com.i69app.ui.screens.main.moment

import android.view.LayoutInflater
import android.view.ViewGroup
import com.i69app.databinding.FragmentUserMomentDetailsBinding
import com.i69app.ui.base.BaseFragment

class UserMomentDetailsFragment: BaseFragment<FragmentUserMomentDetailsBinding>() {

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentUserMomentDetailsBinding.inflate(inflater, container, false)

    override fun setupTheme() {
    }

    override fun setupClickListeners() {
    }
}