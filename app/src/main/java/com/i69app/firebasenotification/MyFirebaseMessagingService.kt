package com.i69app.firebasenotification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.i69app.R
import com.i69app.data.remote.repository.UserUpdateRepository
import com.i69app.singleton.App
import com.i69app.ui.screens.main.MainActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random


@DelicateCoroutinesApi
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var userUpdateRepository: UserUpdateRepository


    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.+
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")


        sendNotification(remoteMessage)

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob()
            } else {
                // Handle message within 10 seconds
                handleNow()
            }
        }


        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")


        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    // [START on_new_token]
    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }
    // [END on_new_token]

    /**
     * Schedule async work using WorkManager.
     */
    private fun scheduleJob() {
        // [START dispatch_job]
//        val work = OneTimeWorkRequest.Builder(MyWorker::class.java).build()
//        WorkManager.getInstance(this).beginWith(work).enqueue()
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        Log.d(TAG, "sendRegistrationTokenToServer($token)")


        token?.let {
            GlobalScope.launch {
                val userId = App.userPreferences.userId.first()
                val userToken = App.userPreferences.userToken.first()

                if (userId != null && userToken != null) {
                    userUpdateRepository.updateFirebasrToken(userId, token, userToken)
                }
            }
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private fun sendNotification(messageBody: RemoteMessage) {

//
        if (messageBody.notification!!.title != null) {

            MainActivity.notificationOpened = false
            val intent = Intent(App.getAppContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
            if (messageBody.notification!!.title.equals("Moment Liked") || messageBody.notification!!.title.equals("Comment in moment") ||
                messageBody.notification!!.title.equals("Story liked") || messageBody.notification!!.title.equals("Story Commented") ||
                messageBody.notification!!.title.equals("Gift received") || messageBody.notification!!.title.equals("Sent message"))
                {
                intent.putExtra("isNotification", "yes")

            }


            val textTitle: String = messageBody.notification!!.title!!

//            PendingIntent.FLAG_IMMUTABLE


            var pendingIntent: PendingIntent? = null
            pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
            } else {
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            }


            val soundUri: Uri

            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)


            val mBuilder: NotificationCompat.Builder =
                NotificationCompat.Builder(this, getString(R.string.app_name))
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(textTitle)
                    .setContentText(messageBody.notification!!.body)
                    .setSound(soundUri)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)


            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name: CharSequence = getString(R.string.app_name)
                val description = getString(R.string.app_name)
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(getString(R.string.app_name), name, importance)
                channel.description = description
                val attributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                channel.enableLights(true)
                channel.enableVibration(true)
                channel.setSound(soundUri, attributes)

                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                val notificationManager = getSystemService(
                    NotificationManager::class.java
                )
                notificationManager.createNotificationChannel(channel)
            }

            val notificationManager = NotificationManagerCompat.from(this)


            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(Random.nextInt(), mBuilder.build())


        }
//            else
//            {
//                val senderId = messageBody.notification!!..additionalData[MainActivity.ARGS_SENDER_ID].toString()
//
//                if (notification.launchURL != null) {
//                    MainActivity.notificationOpened = false
//                    val intent = Intent(App.getAppContext(), MainActivity::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
//                    intent.putExtra(MainActivity.ARGS_SCREEN, notification.launchURL)
//                    intent.putExtra(MainActivity.ARGS_SENDER_ID, senderId)
//                    startActivity(intent)
//                }
//            }
//        }
//        else
//        {
//            val senderId = notification.additionalData[MainActivity.ARGS_SENDER_ID].toString()
//
//            if (notification.launchURL != null) {
//                MainActivity.notificationOpened = false
//                val intent = Intent(App.getAppContext(), MainActivity::class.java)
//                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK
//                intent.putExtra(MainActivity.ARGS_SCREEN, notification.launchURL)
//                intent.putExtra(MainActivity.ARGS_SENDER_ID, senderId)
//                startActivity(intent)
//            }
//        }


    }

    companion object {

        private const val TAG = "MyFirebaseMsgService"
    }
}