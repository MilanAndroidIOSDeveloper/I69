package com.i69app.singleton

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ProcessLifecycleOwner
import com.facebook.FacebookSdk
import com.onesignal.OneSignal
import com.quickblox.auth.session.QBSettings
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.i69app.BuildConfig
import com.i69app.R
import com.i69app.data.preferences.UserPreferences
import com.i69app.data.remote.repository.UserUpdateRepository
import com.i69app.ui.screens.main.MainActivity
import timber.log.Timber
import javax.inject.Inject
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

@HiltAndroidApp
class App : Application() {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_data_store")

    companion object {
        lateinit var userPreferences: UserPreferences
        private lateinit var mInstance: App

        fun getAppContext(): Context = mInstance.applicationContext

        fun getOneSignalPlayerId() = OneSignal.getDeviceState()?.userId

        fun updateOneSignal(userUpdateRepository: UserUpdateRepository) {
            val playerId = getOneSignalPlayerId()
            Timber.e("Player Id $playerId")

            playerId?.let {
                GlobalScope.launch {
                    val userId = userPreferences.userId.first()
                    val userToken = userPreferences.userToken.first()

                    if (userId != null && userToken != null) {
                        userUpdateRepository.updateOneSignalPlayerId(userId, playerId, userToken)
                    }
                }
            }
        }



        fun updateFirebaseToken(userUpdateRepository: UserUpdateRepository) {

            Firebase.messaging.getToken().addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.d("App", "Fetching FCM registration token failed")
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result

                Timber.d("FirebaseToken", ""+token)
                token?.let {
                    GlobalScope.launch {
                        val userId = userPreferences.userId.first()
                        val userToken = userPreferences.userToken.first()

                        if (userId != null && userToken != null) {
                            var res=userUpdateRepository.updateFirebasrToken(userId, token, userToken)
                            Timber.d("TOKEN")
                        }
                    }
                }

//            Toast.makeText(mInstance, token, Toast.LENGTH_SHORT).show()
            })
        }







//         [START log_reg_token]

//         [END log_reg_token]
    }

    @Inject
    lateinit var userUpdateRepository: UserUpdateRepository

    override fun onCreate() {
        super.onCreate()
        mInstance = this
        userPreferences = UserPreferences(this.applicationContext.dataStore)
        FacebookSdk.fullyInitialize()

        if (BuildConfig.DEBUG) {
            Timber.plant(object : Timber.DebugTree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) = super.log(priority, "ayan_$tag", message, t)

                override fun createStackElementTag(element: StackTraceElement): String =
                    String.format("(%s, Line: %s, Method: %s)", super.createStackElementTag(element), element.lineNumber, element.methodName)
            })
        }
        initQuickblox()
        initOneSignal()
        initFirebase()
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleListener(userPreferences, userUpdateRepository))
//        initPayPal()
    }

    private fun initFirebase() {
        updateFirebaseToken(userUpdateRepository)

    }

    private fun initQuickblox() {
        QBSettings.getInstance().init(this, com.i69app.data.config.Constants.QUICK_BLOX_APPLICATION_ID, com.i69app.data.config.Constants.QUICK_BLOX_AUTH_KEY, com.i69app.data.config.Constants.QUICK_BLOX_AUTH_SECRET)
        QBSettings.getInstance().accountKey = com.i69app.data.config.Constants.QUICK_BLOX_ACCOUNT_KEY
    }

    private fun initOneSignal() {


        OneSignal.initWithContext(this)
        OneSignal.setAppId(com.i69app.data.config.Constants.ONESIGNAL_APP_ID)
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE)
        updateOneSignal(userUpdateRepository)

        OneSignal.setNotificationOpenedHandler { result ->
            Timber.tag(MainActivity.CHAT_TAG).w("Result: $result")
            val notification = result.notification


            if(notification.title != null)
            {



                if(notification.title.equals("Moment Liked")||notification.title.equals("Great News!!"))
                {

                    MainActivity.notificationOpened = false
                    val intent = Intent(getAppContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.putExtra("LikeComment", "yes")

                    startActivity(intent)
                }
                else
                {
                    val senderId = notification.additionalData[MainActivity.ARGS_SENDER_ID].toString()

                    if (notification.launchURL != null) {
                        MainActivity.notificationOpened = false
                        val intent = Intent(getAppContext(), MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.putExtra(MainActivity.ARGS_SCREEN, notification.launchURL)
                        intent.putExtra(MainActivity.ARGS_SENDER_ID, senderId)
                        startActivity(intent)
                    }
                }
            }
            else
            {
                val senderId = notification.additionalData[MainActivity.ARGS_SENDER_ID].toString()

                if (notification.launchURL != null) {
                    MainActivity.notificationOpened = false
                    val intent = Intent(getAppContext(), MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
                    intent.putExtra(MainActivity.ARGS_SCREEN, notification.launchURL)
                    intent.putExtra(MainActivity.ARGS_SENDER_ID, senderId)
                    startActivity(intent)
                }
            }
        }
    }

//    private fun initPayPal() {
//        val config = CheckoutConfig(
//            application = this,
//            clientId = Constants.PAYPAL_CLIENT_ID,
//            environment = Constants.PAYPAL_ENVIRONMENT,
//            returnUrl = "${BuildConfig.APPLICATION_ID}://paypalpay",
//            currencyCode = Constants.PAYPAL_CURRENCY,
//            userAction = Constants.PAYPAL_USER_ACTION,
//            settingsConfig = SettingsConfig(
//                loggingEnabled = true
//            )
//        )
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            PayPalCheckout.setConfig(config)
//        }
//    }

}