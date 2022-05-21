package com.i69app.ui.screens

import android.app.ActivityOptions
import android.app.Notification
import android.content.ContentResolver
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.i69app.R
import com.i69app.databinding.ActivitySplashBinding
import com.i69app.ui.base.BaseActivity
import com.i69app.ui.screens.auth.AuthActivity
import com.i69app.ui.screens.main.MainActivity
import com.i69app.ui.screens.main.search.SearchInterestedInFragment
import com.i69app.utils.defaultAnimate
import com.i69app.utils.startActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


private const val MY_REQUEST_CODE = 101

class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    private lateinit var appUpdateManager: AppUpdateManager
    private var userId: String? = null
    private var userToken: String? = null
    override fun getActivityBinding(inflater: LayoutInflater) = ActivitySplashBinding.inflate(inflater)

    override fun setupTheme() {
        SearchInterestedInFragment.setShowAnim(true)
        binding.splashLogo.defaultAnimate(400, 500)
        binding.splashTitle.defaultAnimate(300, 700)





//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
//        val channelId = getString(R.string.default_notification_channel_id)
//        val channelName = getString(R.string.default_notification_channel_name)
//        val channelDescription = getString(R.string.default_notification_channel_desc)
//        val importance = NotificationManagerCompat.IMPORTANCE_HIGH
//        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//
//        val channel = NotificationChannelCompat.Builder(channelId, importance).apply {
//            setName(channelName)
//            setDescription(channelDescription)
//            setSound(alarmSound, Notification.AUDIO_ATTRIBUTES_DEFAULT)
//        }
//        channel.setVibrationEnabled(true)
//        NotificationManagerCompat.from(this).createNotificationChannel(channel.build())



        appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE))
                    startUpdate(appUpdateInfo) else navigate()
            }
            .addOnFailureListener {
                navigate()
            }
    }

    override fun setupClickListeners() {

    }

    private fun startUpdate(appUpdateInfo: AppUpdateInfo) {
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            AppUpdateType.FLEXIBLE,
            this,
            MY_REQUEST_CODE
        )
    }

    private fun navigate() {
        lifecycleScope.launch(Dispatchers.IO) {
            delay(1200)


            withContext(Dispatchers.Main) {
                Timber.d(""+getCurrentUserId())
                if (getCurrentUserId() == null)
                    goToAuthActivity()
                else
                    startActivity<MainActivity>()
            }
        }
    }

    private fun goToAuthActivity() {
        val transactions = arrayOf<Pair<View, String>>(Pair(binding.splashLogo, "logoView"), Pair(binding.splashTitle, "logoTitle"))
        val options = ActivityOptions.makeSceneTransitionAnimation(this, *transactions)
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent, options.toBundle())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Timber.e("MY_APP", "Update flow failed! Result code: $resultCode")
                navigate()
            } else {
                navigate()
            }
        }
    }

}