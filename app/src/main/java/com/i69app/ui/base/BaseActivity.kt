package com.i69app.ui.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import kotlinx.coroutines.flow.first
import com.i69app.data.preferences.UserPreferences
import com.i69app.singleton.App
import com.i69app.utils.createLoadingDialog
import com.i69app.utils.transact

abstract class BaseActivity<dataBinding : ViewDataBinding> : AppCompatActivity() {

    protected lateinit var userPreferences: UserPreferences
    lateinit var binding: dataBinding
    protected lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userPreferences = App.userPreferences
        binding = getActivityBinding(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            lifecycleOwner = this@BaseActivity
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        loadingDialog = createLoadingDialog()
        setupTheme()
        setupClickListeners()
    }

    abstract fun getActivityBinding(inflater: LayoutInflater): dataBinding

    abstract fun setupTheme()

    abstract fun setupClickListeners()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        if(loadingDialog != null)
        {
            loadingDialog.dismiss()
        }
        super.onPause()

    }

    override fun onDestroy() {
        if(loadingDialog != null)
        {
            loadingDialog.dismiss()
        }
        super.onDestroy()

    }

    protected fun showProgressView() {
        loadingDialog.show()
    }

    protected fun hideProgressView() {
        loadingDialog.dismiss()
    }
    suspend fun getCurrentUserName() = userPreferences.userName.first()

    suspend fun getCurrentUserId() = userPreferences.userId.first()

    suspend fun getCurrentUserToken() = userPreferences.userToken.first()

    suspend fun getChatUserId() = userPreferences.chatUserId.first()

    fun transact(fr: Fragment, addToBackStack: Boolean = false) = supportFragmentManager.transact(fr, addToBackStack)

}