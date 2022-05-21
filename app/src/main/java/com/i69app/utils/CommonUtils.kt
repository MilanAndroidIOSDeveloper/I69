package com.i69app.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.i69app.R
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

inline fun <reified T : Activity> Activity.newIntent() = Intent(this, T::class.java)

inline fun <reified T : Activity> Activity.startActivity() = this.startActivity(newIntent<T>())

fun Uri.getFilePathFromUri(context: Context): String? {
    var parcelFileDescriptor: ParcelFileDescriptor? = null
    try {
        parcelFileDescriptor = context.contentResolver.openFileDescriptor(this, "r", null)
    } catch (e: Exception) {
        Timber.d("Error: ${e.message}")
    }

    parcelFileDescriptor?.let {
        val inputStream = FileInputStream(it.fileDescriptor)
        val file = File(
            context.cacheDir,
            context.contentResolver.getFileName(this)
        )
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)

        return file.absolutePath
    }
    return null
}

fun Uri.getVideoFilePath (context: Context): String? {
    val projection = arrayOf(MediaStore.Video.Media.DATA)
    val cursor = context.contentResolver.query(this, projection, null, null, null)
    if (cursor != null) {
        // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
        // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
        val column_index = cursor!!
            .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
        cursor!!.moveToFirst()
        return cursor!!.getString(column_index)
    }
    return null
}

fun ContentResolver.getFileName(uri: Uri): String {
    var name = ""
    val cursor = query(uri, null, null, null, null)
    cursor?.use {
        it.moveToFirst()
        name = cursor.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
    }
    return name
}

fun Bitmap.savePhotoToInternalStorage(context: Context): String? {
    return try {
        val fileName = "${context.getString(R.string.app_name)}_${System.currentTimeMillis()}.jpg"
        context.openFileOutput(fileName, MODE_PRIVATE).use { fos ->
            if (!this.compress(Bitmap.CompressFormat.JPEG, 95, fos)) {
                throw IOException("Couldn't save the bitmap!")
            }

            val filePath = "${context.filesDir.absolutePath}${File.separator}${fileName}"
            filePath
        }

    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

suspend fun Context.getCompressedImageFilePath(filePath: String): String? {
    val resizedBitmap = getBitmap(filePath)
    return resizedBitmap.savePhotoToInternalStorage(this)
}



// url = file path or whatever suitable URL you want.
fun File.getMimeType(): String? {
    var type: String? = null
    val extension = MimeTypeMap.getFileExtensionFromUrl(this.path)
    if (extension != null) {
        type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
    return type
}

fun File.openFile (activity: Activity) {
    // Get URI and MIME type of file
    val uri = FileProvider.getUriForFile(activity.applicationContext, "${activity.packageName}.fileprovider", this)
//    val uri = Uri.fromFile(selectedItem).normalizeScheme()
    val mime: String? = this.getMimeType()

// Open file with user selected app
    val intent = Intent()
    intent.action = Intent.ACTION_VIEW
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//    intent.data = uri
//    intent.type = mime
    intent.setDataAndType(uri, mime)
    return activity.startActivity(intent)
}