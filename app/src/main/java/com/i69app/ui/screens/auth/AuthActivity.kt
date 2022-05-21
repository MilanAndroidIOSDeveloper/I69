package com.i69app.ui.screens.auth

import android.view.LayoutInflater
import android.view.WindowManager
import dagger.hilt.android.AndroidEntryPoint
import com.i69app.databinding.ActivitySignInBinding
import com.i69app.ui.base.BaseActivity

@AndroidEntryPoint
class AuthActivity : BaseActivity<ActivitySignInBinding>() {

    override fun getActivityBinding(inflater: LayoutInflater) = ActivitySignInBinding.inflate(inflater)

    override fun setupTheme() {}

    override fun setupClickListeners() {}

    fun updateStatusBarColor(color: Int) {// Color must be in hexadecimal format
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = color
    }

}