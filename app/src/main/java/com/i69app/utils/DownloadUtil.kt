package com.i69app.utils

import android.app.Activity
import android.app.Dialog
import android.os.Environment
import android.widget.Toast

import androidx.fragment.app.Fragment
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.BufferedSink
import okio.buffer
import okio.sink
import timber.log.Timber
import java.io.File
import java.util.*

class DownloadUtil (var fragment: Fragment) {

    val activity: Activity
        get() = fragment.requireActivity()
    private lateinit var loadingDialog: Dialog
    val client = OkHttpClient()

    fun downloadFile(url: String, filename: String, callback: ((File?) -> Unit)) {
        Thread {

            toggleProgressDialog(true)
            try {

                val token = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) * (33333333/222)
                Timber.d("checkauth $url")
                val request: Request = Request.Builder().url(url).build()

                val response: Response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val downloadedFile = File(activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), filename)
                    val sink: BufferedSink = downloadedFile.sink().buffer()
                    sink.writeAll(response.body!!.source())
                    sink.close()
                    callback.invoke(downloadedFile)
                    //Log.d("File upload","success, path: $serverUploadDirectoryPath$fileName")
                    //showToast("File uploaded successfully at ${response.body.toString()} $serverUploadDirectoryPath$fileName")
                } else {
                    //Log.e("File upload", "failed")
                    showToast("Failed: ${response.code}")
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                //Log.e("File upload", "failed")
                showToast("File uploading failed ${ex.message}")
            }
            toggleProgressDialog(false)
        }.start()
    }

    fun showToast(message: String) {
        activity.runOnUiThread {
            //fragment.view?.snackbar(message)
            Toast.makeText( activity, message, Toast.LENGTH_LONG ).show()
        }
    }

    private fun toggleProgressDialog(show: Boolean) {
        activity.runOnUiThread {
            if (show) {
                //dialog = ProgressDialog.show(activity, "", "Uploading file...", true);
                loadingDialog = activity.createLoadingDialog()
                loadingDialog.show()
            } else {
                loadingDialog.dismiss();
            }
        }
    }

}