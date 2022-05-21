package com.i69app.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.i69app.R
import com.i69app.ui.views.chipcloud.ChipCloud

class InterestsView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : com.i69app.ui.views.chipcloud.ChipCloud(context, attrs) {

    private var chipListener: OnItemClickListener? = null

    init {
        createAddButton()
    }

    private fun createAddButton() {
        val addButton = LayoutInflater.from(context).inflate(R.layout.button_add_tag, null)
        addView(addButton)
    }

    fun setOnAddButtonClickListener(onClickListener: OnClickListener) {
        findViewById<View>(R.id.button_add_tag).setOnClickListener(onClickListener)
    }

    fun setOnChipClickListener(listener: OnItemClickListener) {
        this.chipListener = listener
    }

    fun setInterests(labels: List<String>) {
        val addButton = findViewById<View>(R.id.button_add_tag)
        removeAllViews()

        for ((i, label) in labels.withIndex()) {
            addUnSelectableChip(label, i)
        }

        addView(addButton)
    }

    private fun addUnSelectableChip(label: String?, labelPos: Int) {
        val container = LayoutInflater.from(context).inflate(R.layout.tag, null) as FrameLayout
        container.findViewById<TextView>(R.id.label).text = label
        container.setOnClickListener {
            chipListener?.onClick(label, labelPos)
        }
        addView(container)
    }

    fun interface OnItemClickListener {
        fun onClick(tag: String?, position: Int)
    }

}