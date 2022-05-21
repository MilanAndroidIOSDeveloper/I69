package com.i69app.ui.base

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.findNavController
import kotlinx.coroutines.flow.first
import com.i69app.R
import com.i69app.data.preferences.UserPreferences
import com.i69app.singleton.App
import com.i69app.utils.createLoadingDialog

abstract class BaseFragment<dataBinding : ViewDataBinding> : Fragment() {

    protected lateinit var userPreferences: UserPreferences
    protected lateinit var binding: dataBinding
    private lateinit var loadingDialog: Dialog
    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        userPreferences = App.userPreferences
        setStatusBarColor(getStatusBarColor())
        binding = getFragmentBinding(inflater, container)
        val contentView = binding.root
        contentView.findViewById<View>(R.id.actionBack)?.setOnClickListener {
            activity?.onBackPressed()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadingDialog = requireContext().createLoadingDialog()
        binding.apply {
            lifecycleOwner = this@BaseFragment
        }
        setupTheme()
        setupClickListeners()
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

    abstract fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?): dataBinding

    abstract fun setupTheme()

    abstract fun setupClickListeners()

    suspend fun getCurrentUserId() = userPreferences.userId.first()

    suspend fun getCurrentUserName() = userPreferences.userName.first()

    suspend fun getCurrentUserToken() = userPreferences.userToken.first()

    suspend fun getChatUserId() = userPreferences.chatUserId.first()

    open fun getStatusBarColor() = R.color.colorPrimaryDark

    fun setStatusBarColor(@ColorRes color: Int) {
        val window = requireActivity().window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(requireActivity(), color)
    }

    protected fun showProgressView() {
        loadingDialog.show()
    }

    protected fun hideProgressView() {
        loadingDialog.dismiss()
    }

    protected fun <T : Activity> getTypeActivity(): T? {
        return if (activity != null) activity as T else null
    }

    fun moveTo(direction: Int, args: Bundle? = null) =
        view?.findNavController()?.navigate(direction, args)

    fun moveTo(direction: NavDirections) = view?.findNavController()?.navigate(direction)

    fun moveUp() = view?.findNavController()?.navigateUp()

}