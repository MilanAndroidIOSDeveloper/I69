package com.i69app.utils

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.i69app.R
import org.json.JSONObject
import java.util.*

private const val START_ANIM_VALUE = 0.9f
private const val END_ANIM_VALUE = 1f
const val EXTRA_USER_MODEL = "user"
const val EXTRA_FIELD_VALUE = "EXTRA_FIELD_VALUE"
var animSideDuration: Float = 0f
var prevDiff: Int = 0

private fun getCurrentLanguage(): String = Locale.getDefault().language

fun isCurrentLanguageFrench() = getCurrentLanguage() == "fr"

fun getChatMsgNotificationBody(msgText: String): String = if (msgText.length > 15) msgText.take(15) + "..." else msgText

class Utils {

    companion object {
        @JvmStatic
        fun convertInchToLb(value: Float): String {
            val inchInt = value.toInt()
            val inch = inchInt % 12
            val lb = inchInt / 12
            return "$lb'$inch\""
        }
        fun getScreenHeight(): Int {
            return Resources.getSystem().displayMetrics.heightPixels
        }
    }

}

fun Activity.startEmailIntent(toEmail: String, subject: String, message: String) {
    val intent = Intent(Intent.ACTION_SENDTO)
    intent.data = Uri.parse("mailto:")
    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(toEmail))
    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
    intent.putExtra(Intent.EXTRA_TEXT, message)

    try {
        startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun TextView.setTextOrHide(text: String?) {
    if (text.isNullOrEmpty()) {
        this.visibility = View.GONE
    } else {
        this.text = text
        this.visibility = View.VISIBLE
    }
}

fun View.setVisibility(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.setVisibleOrInvisible(visible: Boolean) {
    this.visibility = if (visible) View.VISIBLE else View.INVISIBLE
}

fun View.setViewGone() = this.setVisibility(false)

fun View.setViewVisible() = this.setVisibility(true)


fun JSONObject.getStringOrNull(key: String): String? {
    if (has(key)) {
        return this.getString(key)
    }
    return null
}

fun Context.inflate(@LayoutRes resId: Int, parent: ViewGroup? = null, attachToRoot: Boolean = false): View =
    LayoutInflater.from(this).inflate(resId, parent, attachToRoot)

fun View.inflate(@LayoutRes resId: Int, parent: ViewGroup? = null, attachToRoot: Boolean = false): View =
    LayoutInflater.from(this.context).inflate(resId, parent, attachToRoot)

fun FragmentManager.transact(fragment: Fragment, addToBackStack: Boolean = false) {
    val res = this.beginTransaction()
        .replace(R.id.container, fragment)
    if (addToBackStack)
        res.addToBackStack(null)

    res.commit()
}

fun View.defaultAnimate(duration: Long, delay: Long) {
    this.scaleX = START_ANIM_VALUE
    this.scaleY = START_ANIM_VALUE
    this.alpha = 0f
    ViewCompat.animate(this)
        .scaleX(END_ANIM_VALUE)
        .scaleY(END_ANIM_VALUE)
        .alpha(END_ANIM_VALUE)
        .setInterpolator(AccelerateDecelerateInterpolator())
        .setDuration(duration)
        .setStartDelay(delay)
        .start()
}

fun View.animateFromLeft(duration: Long, delay: Long, width: Int = 0) {
    this.alpha = 0f
    val side = getAnimSideTransactionX(context, width)
    this.x = this.x - side
    ViewCompat.animate(this)
        .translationXBy(side)
        .alpha(END_ANIM_VALUE)
        .setInterpolator(OvershootInterpolator())
        .setDuration(duration)
        .setStartDelay(delay)
        .start()
}

fun View.animateFromRight(duration: Long, delay: Long, width: Int = 0) {
    this.alpha = 0f
    val side = getAnimSideTransactionX(context, width)
    this.x = this.x + side
    ViewCompat.animate(this)
        .translationXBy(-side)
        .alpha(END_ANIM_VALUE)
        .setInterpolator(OvershootInterpolator())
        .setDuration(duration)
        .setStartDelay(delay)
        .start()
}

fun getAnimSideTransactionX(ctx: Context, widthDiff: Int): Float {
    if (prevDiff != widthDiff || animSideDuration == 0f)
        animSideDuration = if (widthDiff == 0) ctx.resources.getDimension(R.dimen.anim_side_duration) else widthDiff.toFloat()
    prevDiff = widthDiff
    return animSideDuration
}

fun String.findFileExtension (): String {
    var ext = ""
    try {
        val uri = Uri.parse(this)
        val lastSegment = uri.lastPathSegment
        ext = lastSegment?.substring(lastSegment.lastIndexOf(".") + 1).toString()

    } catch (e: Exception) {}
    return ext
}

fun String.isImageFile(): Boolean {
    return this.contentEquals("jpg")
        || this.contentEquals("png")
        || this.contentEquals("jpeg")
}

fun String.isVideoFile(): Boolean {
    return this.contentEquals("mp4")
            || this.contentEquals("mov")
            || this.contentEquals("wmv")
            || this.contentEquals("avi")
            || this.contentEquals("flv")
            || this.contentEquals("mkv")
}

fun String.isNeitherImageNorVideoFile (): Boolean {
    return !this.isImageFile() && !this.isVideoFile()
}