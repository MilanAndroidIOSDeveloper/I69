package com.i69app.ui.screens.auth.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import com.i69app.R
import com.i69app.databinding.FragmentWelcomeBinding
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.screens.auth.AuthActivity
import com.i69app.ui.screens.main.MainActivity
import com.i69app.utils.startActivity

@AndroidEntryPoint
class WelcomeFragment : BaseFragment<FragmentWelcomeBinding>() {

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?)= FragmentWelcomeBinding.inflate(inflater,container,false)

    override fun setupTheme() {
        (activity as AuthActivity).updateStatusBarColor(ContextCompat.getColor(requireActivity(), R.color.colorPrimary))
    }

    override fun setupClickListeners() {
        binding.start.setOnClickListener {
            requireActivity().startActivity<MainActivity>()
            requireActivity().finish()
        }
    }

}