package com.i69app.ui.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.i69app.R

class CheckableImageButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), Checkable {
    private var colorDefault: Int = 0
    private var colorChecked: Int = 0
    private val iconCheckedDrawable: Drawable?
    private val iconUnCheckedDrawable: Drawable?

    private val checkedStateSet = intArrayOf(android.R.attr.state_checked)

    private var isChecked = false

    var onCheckedChangedCallback: ((view: View, isChecked: Boolean) -> Unit)? = null

    init {
        isClickable = this.isClickable
        isFocusable = this.isFocusable
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CheckableImageButton,
            0, 0
        ).apply {
            try {
                val text = getString(R.styleable.CheckableImageButton_text)
                iconCheckedDrawable = getDrawable(R.styleable.CheckableImageButton_checkedSrc)
                iconUnCheckedDrawable = getDrawable(R.styleable.CheckableImageButton_uncheckedSrc)
                colorDefault = getColor(R.styleable.CheckableImageButton_colorDefault, colorDefault)
                colorChecked = getColor(R.styleable.CheckableImageButton_colorChecked, colorChecked)

                initLayout(text, iconUnCheckedDrawable)
            } finally {
                recycle()
            }
        }
    }

    private fun initLayout(text: String?, src: Drawable?) {
        val content = LayoutInflater.from(context).inflate(R.layout.checkable_image_button_content_vertical, this)
        content.findViewById<TextView>(R.id.text).text = text
        content.findViewById<ImageView>(R.id.image_src).apply {
            setBackgroundResource(R.drawable.background_circle_button_unchecked)
            setImageDrawable(src)
        }
    }

    override fun isChecked(): Boolean = isChecked

    override fun toggle() {
        setChecked(!isChecked)
    }

    override fun setChecked(checked: Boolean) {
        isChecked = checked
        onCheckedChangedCallback?.invoke(this, isChecked)
        refreshDrawableState()
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 1)
        if (isChecked()) {
            View.mergeDrawableStates(drawableState, checkedStateSet)
        }
        return drawableState
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        val color = getColor()
        findViewById<TextView>(R.id.text).setTextColor(color)
        findViewById<ImageView>(R.id.image_src).apply {
            setImageDrawable(
                if (isChecked) iconCheckedDrawable
                else iconUnCheckedDrawable
            )
            setBackgroundResource(
                if (isChecked) R.drawable.background_circle_button_checked
                else R.drawable.background_circle_button_unchecked
            )
        }
    }

    private fun getColor(): Int = if (isChecked) colorChecked else colorDefault

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return onTouchEvent(ev)
    }

    override fun performClick(): Boolean {
        toggle()
        return super.performClick()
    }

}