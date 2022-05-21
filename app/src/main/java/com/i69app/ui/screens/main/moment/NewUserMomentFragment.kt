package com.i69app.ui.screens.main.moment

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.apollographql.apollo3.api.DefaultUpload
import com.apollographql.apollo3.api.content
import com.apollographql.apollo3.exception.ApolloException
import com.google.gson.Gson
import com.i69app.BuildConfig
import com.i69app.MomentMutation

import com.i69app.R
import com.i69app.data.models.Photo
import com.i69app.data.models.User
import com.i69app.databinding.FragmentNewUserMomentBinding
import com.i69app.singleton.App
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.screens.ImagePickerActivity
import com.i69app.ui.screens.SplashActivity
import com.i69app.ui.screens.main.MainActivity
import com.i69app.ui.screens.main.search.userProfile.getimageSliderIntent
import com.i69app.ui.viewModels.UserViewModel
import com.i69app.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class NewUserMomentFragment : BaseFragment<FragmentNewUserMomentBinding>() {

    private val viewModel: UserViewModel by activityViewModels()
    private var mFilePath : String? = null
    protected var mUser: User? = null

    private val photosLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        val data = activityResult.data
        if (activityResult.resultCode == Activity.RESULT_OK) {
            mFilePath = data?.getStringExtra("result")
            Timber.d("fileBase64 $mFilePath")
            binding.imgUploadFile.loadCircleImage(mFilePath!!)
        }
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?) = FragmentNewUserMomentBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        binding.btnSelectFileToUpload.setOnClickListener {
            val intent = Intent(requireActivity(), ImagePickerActivity::class.java)
            photosLauncher.launch(intent)
        }

        binding.btnShareMoment.setOnClickListener {
            if (mFilePath == null) {
                binding.root.snackbar(getString(R.string.you_cant_share_moment))
                return@setOnClickListener
            }
            showProgressView()

            val description = binding.editWhatsGoing.text.toString()
            /*val fileBase64 = BitmapFactory.decodeFile(mFilePath).convertBitmapToString()
            lifecycleScope.launch(Dispatchers.Main) {
                val userToken = getCurrentUserToken()!!
                Timber.d("fileBase64 [${fileBase64.length}] $mFilePath")
                when (val response = mViewModel.shareUserMoment("Moment Image", fileBase64, description, token = userToken)) {
                    is Resource.Success -> {
                        hideProgressView()

                    }
                    is Resource.Error -> onFailureListener(response.message ?: "")
                }
            }*/
            Timber.d("filee $mFilePath")

            lifecycleScope.launchWhenCreated {

                val f = File(mFilePath)
                val buildder = DefaultUpload.Builder()
                buildder.contentType("Content-Disposition: form-data;")
                buildder.fileName(f.name)
                val upload = buildder.content(f).build()
                Timber.d("filee ${f.exists()}")
                val userToken = getCurrentUserToken()!!

                Timber.d("useriddd ${mUser?.id}")
                if (mUser?.id != null) {
                    val response = try {
                        apolloClient(context = requireContext(), token = userToken).mutation(
                            MomentMutation(
                                file = upload,
                                detail = description,
                                userField = mUser?.id!!
                            )
                        ).execute()
                    } catch (e: ApolloException) {
                        hideProgressView()
                        Timber.d("filee Apollo Exception ${e.message}")
                        binding.root.snackbar("ApolloException ${e.message}")
                        return@launchWhenCreated
                    } catch (e: Exception) {
                        hideProgressView()

                        Timber.d("filee General Exception ${e.message} $userToken")
                        binding.root.snackbar("Exception ${e.message}")
                        return@launchWhenCreated
                    }


                    hideProgressView()
                    if (response.hasErrors()) {

                        if(response.errors!![0].message.equals("User doesn't exist"))
                        {
                            binding.root.snackbar("" + response.errors!![0].message)

                            Handler().postDelayed({ nouserexist() }, 1500)


                        }
                        else {
                            binding.root.snackbar("" + response.errors!![0].message)
                        }

                    }
                    if (response.hasErrors() == false) {

                        getMainActivity().openUserMoments()
                    }
                } else {

                    binding.root.snackbar("username is null")
                    binding.root.snackbar("Exception ${mUser?.id}")
                }
                hideProgressView()
                //binding.root.snackbar("Exception (${response.hasErrors()}) ${response.data?.insertMoment?.moment?.momentDescription}")
                //Timber.d("filee response = (${response.hasErrors()}) ${response.data?.insertMoment?.moment?.momentDescription}")
                //Timber.d("filee response = (${response.hasErrors()}) [${response.errors?.get(0)?.message}] ${response.data?.insertMoment?.moment?.momentDescription}")
                //filee response = com.apollographql.apollo3.api.ApolloResponse@3f798dc
            }
        }



        showProgressView()
        lifecycleScope.launch {
            val userId = getCurrentUserId()!!
            val token = getCurrentUserToken()!!
            viewModel.getCurrentUser(userId, token = token, false).observe(viewLifecycleOwner) { user ->
                user?.let {
                    mUser = it.copy()
                    Timber.d("Userrname ${mUser?.username}")

                    if(mUser != null)
                    {

                            if(mUser!!.avatarPhotos != null && mUser!!.avatarPhotos!!.size != 0)
                            {

                                if (mUser!!.avatarPhotos!!.size!=0)
                                {
                                    binding.imgCurrentUser.loadCircleImage(mUser!!.avatarPhotos!!.get(mUser!!.avatarIndex!!).url.replace("http://95.216.208.1:8000/media/","${BuildConfig.BASE_URL}media/"))

                                }

                            }
                    }

                }
                hideProgressView()
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
            getMainActivity().drawerSwitchState()
        }

    }

    private fun onFailureListener(error: String) {
        hideProgressView()
        Timber.e("${getString(R.string.something_went_wrong)} $error")
        binding.root.snackbar("${getString(R.string.something_went_wrong)} $error")
    }

    fun getMainActivity() = activity as MainActivity
}