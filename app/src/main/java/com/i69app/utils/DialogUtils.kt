package com.i69app.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.i69app.BuildConfig
import com.i69app.GetAllUserStoriesQuery
import com.i69app.R
import com.i69app.ui.interfaces.AlertDialogCallback
import timber.log.Timber

fun Context.createLoadingDialog(): Dialog {
    val dialog = Dialog(this)
    with(dialog) {
        setContentView(R.layout.dialog_loading_progress)
        setCancelable(false)
        window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawable(ContextCompat.getDrawable(this@createLoadingDialog, R.drawable.progress_circle))
    }
    return dialog
}

fun Context.showAlertDialog(positionBtnText: String, title: String, subTitle: String, listener: AlertDialogCallback) {
    val resources = resources

    val dialog = MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(subTitle)
        .setBackground(ContextCompat.getDrawable(this, R.drawable.background_yellow_gradient))
        .setCancelable(true)
        .setNegativeButton(resources.getString(R.string.no)) { dialog, _ ->
            listener.onNegativeButtonClick(dialog = dialog)
        }
        .setPositiveButton(positionBtnText) { dialog, _ ->
            listener.onPositiveButtonClick(dialog)
        }
        .show()

    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColorStateList(this, R.color.black))
    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTypeface(null, Typeface.BOLD)

    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColorStateList(this, R.color.black))
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(null, Typeface.BOLD)
}

fun Context.showOkAlertDialog(positionBtnText: String, title: String, subTitle: String, listener: DialogInterface.OnClickListener) {
    val dialog = MaterialAlertDialogBuilder(this)
        .setTitle(title)
        .setMessage(subTitle)
        .setBackground(ContextCompat.getDrawable(this, R.drawable.background_yellow_gradient))
        .setCancelable(true)
        .setPositiveButton(positionBtnText, listener).show()

    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColorStateList(this, R.color.black))
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(null, Typeface.BOLD)
}

fun Activity.openUserStoryDialog (userStory: GetAllUserStoriesQuery.Edge?) {

    val builder = MaterialAlertDialogBuilder(this)
    val viewDialog = this.layoutInflater.inflate(R.layout.fragment_user_story_detail, null)
    val url = "${BuildConfig.BASE_URL}media/${userStory?.node!!.file}"
    Timber.d("openstory $url")
    val imgUserStory = viewDialog.findViewById<ImageView>(R.id.imgUserStory)
    imgUserStory.loadImage(url)

    builder.setView(viewDialog)
    val dialog = builder.create()
    dialog.setCancelable(false)
    dialog.window?.setGravity(Gravity.TOP)
    dialog.show()

}