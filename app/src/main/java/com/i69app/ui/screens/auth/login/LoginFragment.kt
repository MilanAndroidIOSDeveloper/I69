package com.i69app.ui.screens.auth.login

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.i69app.R
import com.i69app.data.models.Photo
import com.i69app.data.models.User
import com.i69app.data.remote.requests.LoginRequest
import com.i69app.data.remote.responses.LoginResponse
import com.i69app.data.remote.responses.ResponseBody
import com.i69app.databinding.FragmentLoginBinding
import com.i69app.singleton.App
import com.i69app.ui.base.BaseFragment
import com.i69app.ui.screens.main.MainActivity
import com.i69app.ui.viewModels.AuthViewModel
import com.i69app.utils.Resource
import com.i69app.utils.defaultAnimate
import com.i69app.utils.getCompressedImageFilePath
import com.i69app.utils.snackbar
import com.i69app.utils.startActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber


@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {

    private val viewModel: AuthViewModel by activityViewModels()
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private lateinit var loginManager: LoginManager

    private val googleLoginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            showProgressView()
            Timber.d("Google Token: ${account.idToken}")
            onLoginSuccess(
                provider = com.i69app.data.enums.LoginProvider.GOOGLE,
                name = account.displayName,
                photo = "",
                accessToken = account.idToken!!
            )

        } catch (e: ApiException) {
            Timber.e("${getString(R.string.sign_in_failed)} ${e.message}")
            Log.d("LoginFragment", "${e.message}")
            if (e.statusCode != GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                binding.root.snackbar("${getString(R.string.sign_in_failed)} ${e.message}")
            }
        }
    }

    private val webLoginLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        val data = result.data
        if (result.resultCode == RESULT_OK) {
            val accessToken = data?.getStringExtra(WebLoginActivity.ARGS_ACCESS_TOKEN)
            val accessVerifier = data?.getStringExtra(WebLoginActivity.ARGS_ACCESS_VERIFIER)

            showProgressView()
              onLoginSuccess(
                provider = com.i69app.data.enums.LoginProvider.TWITTER,
                photo = "",
                accessToken = accessToken.toString(),
                accessVerifier = accessVerifier.toString()
            )
        }
    }

    override fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) = FragmentLoginBinding.inflate(inflater, container, false)

    override fun setupTheme() {
        binding.signInLogo.defaultAnimate(300, 800)
        binding.signInAppTitle.defaultAnimate(300, 800)
        //binding.signInAppDescription.defaultAnimate(300, 800)
        binding.signInButtonWithFb.defaultAnimate(300, 800)
        binding.signInButtonWithTwitter.defaultAnimate(300, 800)
        binding.signInButtonWithGoogle.defaultAnimate(300, 800)

        initializeGoogleSignIn()
        initializeFacebookSignIn()
    }

    override fun setupClickListeners() {
        binding.signInButtonWithFb.setOnClickListener { loginToFacebook() }
        binding.signInButtonWithTwitter.setOnClickListener { loginToTwitter() }
        binding.signInButtonWithGoogle.setOnClickListener { loginToGoogle() }
    }

    private fun initializeGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun initializeFacebookSignIn() {
        callbackManager = CallbackManager.Factory.create()

        loginManager = LoginManager.getInstance()
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
            override fun onSuccess(result: LoginResult?) {
                Timber.d("Facebook: onSuccess: $result")
                result?.let {
                    showProgressView()
                    viewModel.getUserDataFromFacebook(loginResult = result) { name, photos ->
                        onLoginSuccess(
                            com.i69app.data.enums.LoginProvider.FACEBOOK,
                            name = name,
                            photo = photos?.firstOrNull() ?: "",
                            accessToken = it.accessToken.token
                        )
                    }
                }
            }

            override fun onCancel() {
                Timber.d("Facebook: onCancel")
            }

            override fun onError(exception: FacebookException) {
                Timber.e("Facebook: onError: $exception")
                binding.root.snackbar("${getString(R.string.sign_in_failed)} Try again later!")
            }
        })
    }

    private fun loginToGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        googleLoginLauncher.launch(signInIntent)
    }

    private fun loginToFacebook() {
        loginManager.logInWithReadPermissions(this, mutableListOf("email", "public_profile"))
//        loginManager.logInWithReadPermissions(this, mutableListOf("email", "public_profile", "user_friends"))
    }

    private fun loginToTwitter() {
        val intent = Intent(requireActivity(), WebLoginActivity::class.java)
        webLoginLauncher.launch(intent)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun onLoginSuccess(
        provider: com.i69app.data.enums.LoginProvider,
        name: String? = "",
        photo: String,
        accessToken: String,
        accessVerifier: String = ""
    ) {
        lifecycleScope.launch(Dispatchers.Main) {
            val loginRequest = LoginRequest(
                accessToken = accessToken,
                accessVerifier = accessVerifier,
                provider = provider.provider
            )

            when (val response = viewModel.login(loginRequest)) {
                is Resource.Success -> {
                    val nameValue = if (provider == com.i69app.data.enums.LoginProvider.TWITTER) response.data?.data?.username else name
                    val compressedFilePath = if (photo.isNotEmpty()) requireContext().getCompressedImageFilePath(photo) else ""
                    val photos = if (compressedFilePath.isNullOrEmpty()) mutableListOf() else mutableListOf(Photo(id = "0", compressedFilePath))

                    hideProgressView()
                    navigateToNextScreen(response, nameValue, photos)
                }
                is Resource.Error -> {
                    hideProgressView()
                    Timber.e("${getString(R.string.sign_in_failed)} ${response.message}")
                    binding.root.snackbar("${getString(R.string.sign_in_failed)} ${response.message}")
                }
            }
        }
    }

    private suspend fun navigateToNextScreen(
        response: Resource.Success<ResponseBody<LoginResponse>>,
        name: String?,
        photos: MutableList<Photo>
    ) {
        val loginResult = response.data!!.data

        if (loginResult?.isNew == true) {
            val email = loginResult.email?.substring(0, loginResult.email.indexOf("@")) ?: ""

            val names= name?.replace("_twitter_twitter","")!!.replace("_twitter","")

            viewModel.setAuthUser(
                User(
                    id = loginResult.id,
                    email = loginResult.email ?: "",
                    fullName = names ?: email,
                    avatarPhotos = photos,
                )
            )
            viewModel.token = loginResult.token
            moveTo(R.id.action_login_to_interested_in)

        } else {
            val names= name?.replace("_twitter_twitter","")!!.replace("_twitter","")
            userPreferences.saveUserIdToken(userId = loginResult!!.id, token = loginResult.token,names)
            App.updateFirebaseToken(viewModel.userUpdateRepository)

            App.updateOneSignal(viewModel.userUpdateRepository)
            requireActivity().startActivity<MainActivity>()
            requireActivity().finish()
        }
    }

}