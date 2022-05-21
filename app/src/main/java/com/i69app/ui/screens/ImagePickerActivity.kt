package com.i69app.ui.screens

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.i69app.R
import com.i69app.utils.getCompressedImageFilePath
import com.i69app.utils.getFilePathFromUri
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Flash
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import io.ak1.pix.models.Ratio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File


class ImagePickerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startPix()
    }

    private fun startPix() {
        val options = Options().apply {
            ratio = Ratio.RATIO_AUTO
            count = 1
            spanCount = 5
            flash = Flash.Auto
            mode = Mode.All
            isFrontFacing = true
            videoDurationLimitInSeconds = intent.getIntExtra("video_duration_limit", 15)
            path = "/"
        }

        addPixToActivity(R.id.container, options) {
            when (it.status) {
                PixEventCallback.Status.SUCCESS -> {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val imageUri = it.data.first()
                        val filePath = imageUri.getFilePathFromUri(this@ImagePickerActivity) ?: return@launch
                        val file = java.io.File(filePath)
                        val ext = file.name.substring(file.name.indexOf(".") + 1)
                        val result: String?
                        if (ext.contentEquals("jpg") || ext.contentEquals("png") || ext.contentEquals("jpeg")) {
                            result = getCompressedImageFilePath(filePath)
                            if (this@ImagePickerActivity.intent.getBooleanExtra("withCrop", false)) {
                                try {
                                    onCropImage(result)
                                } catch (e: Exception) {
                                    Timber.d("crroppresult cropping ${e.message}")
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    val intent = Intent()
                                    intent.putExtra("result", result)
                                    setResult(Activity.RESULT_OK, intent)
                                    finish()
                                }
                            }
                        } else {
                            result = filePath
                            withContext(Dispatchers.Main) {
                                val intent = Intent()
                                intent.putExtra("result", result)
                                setResult(Activity.RESULT_OK, intent)
                                finish()
                            }
                        }
                    }
                }
                PixEventCallback.Status.BACK_PRESSED -> {
                    val intent = Intent()
                    setResult(Activity.RESULT_CANCELED, intent)
                    finish()
                }
            }
        }
    }

    private fun onCropImage (filePath: String?) {

        val file = File (filePath!!)
        val intermediateProvider = FileProvider.getUriForFile(applicationContext, "${packageName}.fileprovider", file)
        grantUriPermission("com.android.camera", intermediateProvider, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(intermediateProvider, "image/*")

        val list = packageManager.queryIntentActivities(intent, 0)

        var size = 0

        if (list.size > 0) {
            grantUriPermission(
                list[0].activityInfo.packageName,
                intermediateProvider,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            size = list.size
        }

        if (size == 0) {
            runOnUiThread {
                Toast.makeText(this, "Error, wasn't taken image!", Toast.LENGTH_SHORT).show()
            }

        } else {

            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            intent.putExtra("crop", "true")
            intent.putExtra("scale", true)

            val fileName = "${getString(R.string.app_name)}_${System.currentTimeMillis()}.jpg"
            photoCroppedFile = File (filesDir, fileName)
            val resultProvider = FileProvider.getUriForFile(
                this@ImagePickerActivity,
                "${packageName}.fileprovider",
                photoCroppedFile!!
            )

            intent.putExtra("return-data", false)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, resultProvider)
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
            val cropIntent = Intent(intent)
            val res = list!![0]
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            grantUriPermission(res.activityInfo.packageName, resultProvider, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)

            cropIntent.component =
                ComponentName(res.activityInfo.packageName, res.activityInfo.name)
            cropActivityResultLauncher.launch(cropIntent)
        }
    }

    private var photoCroppedFile: File? = null
    private val cropActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->

        if (activityResult.resultCode == Activity.RESULT_OK) {

            lifecycleScope.launch(Dispatchers.IO) {

                withContext(Dispatchers.Main) {
                    val intent = Intent()
                    intent.putExtra("cropped_result", photoCroppedFile?.absolutePath)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            }
        }
    }
}