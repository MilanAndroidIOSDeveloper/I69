package com.i69app.utils

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.google.android.material.snackbar.Snackbar
import com.i69app.R
import com.i69app.data.models.IdWithValue
import com.i69app.ui.views.CheckableImageButton
import kotlin.math.roundToInt

enum class AnimationTypes {
    DEFAULT,
    SLIDE_ANIM,
    FADE_SLIDE_ANIM
}

fun NavController.navigate(
    @IdRes destinationId: Int,
    @IdRes popUpFragId: Int? = null,
    inclusive: Boolean = true,
    args: Bundle? = null,
    animType: AnimationTypes? = AnimationTypes.SLIDE_ANIM
) {
    val navOptionsBuilder = NavOptions.Builder()

    when (animType) {
        AnimationTypes.DEFAULT -> navOptionsBuilder
            .setEnterAnim(R.anim.nav_default_enter_anim)
            .setExitAnim(R.anim.nav_default_exit_anim)
            .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
            .setPopExitAnim(R.anim.nav_default_pop_exit_anim)

        AnimationTypes.SLIDE_ANIM -> navOptionsBuilder
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)

        AnimationTypes.FADE_SLIDE_ANIM -> navOptionsBuilder
            .setEnterAnim(R.anim.slide_in)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.slide_out)
    }

    popUpFragId?.let {
        navOptionsBuilder.setPopUpTo(it, inclusive)
    }
    val navOptions = navOptionsBuilder.build()

    navigate(destinationId, args, navOptions)
}

fun View.snackbar(message: String, duration: Int = Snackbar.LENGTH_LONG, callback: (() -> Unit)? = null) {
    Snackbar
        .make(this, message, duration)
        .also { snackbar ->
            snackbar.setAction("Ok") {
                snackbar.dismiss()
                callback?.invoke()
            }
            snackbar.setActionTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
            val view = snackbar.view

            val params = view.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.CENTER

            view.layoutParams = params
            view.background = ContextCompat.getDrawable(this.context, R.drawable.rounded_yellow_btn_snackbar)
            val px16 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24f, this.resources.displayMetrics)
            view.updatePadding(top = 12, bottom = 12, left = px16.roundToInt(), right = px16.roundToInt())

            val actionBtn = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)
            actionBtn.background = ContextCompat.getDrawable(this.context, R.drawable.snackbar_background)

            val text = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            text.isSingleLine = false
            text.setTextColor(ContextCompat.getColor(context, R.color.white))
            text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        }.show()
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}

fun CheckableImageButton.setButtonState(isChecked: Boolean) {
    this.isChecked = isChecked
}

fun List<IdWithValue>.getSelectedValueFromDefaultPicker(id: Int?): String {
    this.forEach { idWithValue ->
        if (idWithValue.id == id) return if (isCurrentLanguageFrench()) idWithValue.valueFr else idWithValue.value
    }
    return ""
}

fun spannedFromHtml(text: String): Spanned? {
    return if (Build.VERSION.SDK_INT >= 24) {
        Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
    } else {
        @Suppress("DEPRECATION")
        Html.fromHtml(text)
    }
}


fun DrawerLayout.closeNavigationDrawer() {
    enableNavigationDrawer()
    closeDrawer(GravityCompat.START)
}

fun DrawerLayout.drawerSwitchState() {
    if (isDrawerOpen(GravityCompat.START)) {
        closeNavigationDrawer()
    } else {
        openDrawer(GravityCompat.START)
        enableNavigationDrawer()
    }
}

fun DrawerLayout.disableNavigationDrawer() {
    setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
}

fun DrawerLayout.enableNavigationDrawer() {
    setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
}

fun ProgressBar.smoothProgress(percent: Int){
    val animation = ObjectAnimator.ofInt(this, "progress", percent)
    animation.duration = 400
    animation.interpolator = DecelerateInterpolator()
    animation.start()
}