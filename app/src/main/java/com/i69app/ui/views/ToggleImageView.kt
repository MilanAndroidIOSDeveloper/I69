package com.i69app.ui.views

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.widget.AppCompatImageView
import android.util.AttributeSet
import android.widget.Checkable
import android.widget.CompoundButton
import com.i69app.R

class ToggleImageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr), Checkable {

    private var isChecked: Boolean = false
    private var checkedDrawable: Drawable? = null
    private var uncheckedDrawable: Drawable? = null

    var onCheckedChangeListener: CompoundButton.OnCheckedChangeListener? = null

    init {
        context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.ToggleImageView,
                0, 0).apply {

            try {
                checkedDrawable = getDrawable(R.styleable.ToggleImageView_checkedDrawable)
                uncheckedDrawable = getDrawable(R.styleable.ToggleImageView_uncheckedDrawable)
                setChecked(isChecked)
            } finally {
                recycle()
            }
        }

        setOnClickListener {
            toggle()
        }
    }

    override fun isChecked(): Boolean = isChecked

    override fun toggle() {
        setChecked(!isChecked)
    }

    override fun setChecked(checked: Boolean) {
        isChecked = checked
        setImageDrawable(if (isChecked) checkedDrawable else uncheckedDrawable)
        onCheckedChangeListener?.onCheckedChanged(null, isChecked)
    }
}