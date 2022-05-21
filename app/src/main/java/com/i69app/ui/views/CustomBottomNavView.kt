package com.i69app.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.cardview.widget.CardView
import com.i69app.R
import com.i69app.databinding.LayoutBottomNavigationBinding
import com.i69app.utils.loadCircleImage
import com.i69app.utils.loadImage

class CustomBottomNavView : FrameLayout, View.OnClickListener {
    private var selected = 0
    private var userImg = ""
    private var binding: LayoutBottomNavigationBinding
    private var items: List<Pair<Any?, Any?>>? = null
    var itemClickListener: ((Int) -> Unit)? = null

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    init {
        val inflater = LayoutInflater.from(context)
        binding = LayoutBottomNavigationBinding.inflate(inflater, this, false)
        addView(binding.root)
    }

    fun setItems(items: List<Pair<Any?, Any?>>) {
        this.items = items
        if (items.size != 5) {
            throw IllegalArgumentException("must be 3 items")
        }
        updateItems()
    }

    private fun updateItems() {
        if (items == null)
            return
        setItem(binding.tab1, null, items!!, 0)
        setItem(binding.tab4, null, items!!, 1)
        setItem(binding.tab5, null, items!!, 2)
        setItem(binding.tab2, null, items!!, 3)
        setItem(binding.userProfileImg, binding.tab3, items!!, 4)
    }

    override fun onClick(v: View?) {
        selected = when (v?.id ?: 0) {
            binding.tab4.id -> 1
            binding.tab5.id -> 2
            binding.tab2.id -> 3
            binding.tab3.id -> 4
            else -> 0
        }
        updateItems()
        itemClickListener?.invoke(selected)
    }

    fun selectItem(index: Int) {
        if (index in 0..5) {
            selected = index
            updateItems()
        }
    }

    private fun setItem(v: ImageView, cardView: CardView? = null, items: List<Pair<Any?, Any?>>, position: Int) {
        val item = items[position]
        val selected = position == selected
        val src = if (selected) item.second else item.first
        if (cardView == null) {
            updateIconLayoutParams(v, selected, R.dimen.nav_bottom_item_padding_chat)
            v.setBackgroundResource(src as Int)
            v.setOnClickListener(this)

        } else {
            src?.let {
                if (userImg != it.toString()) {
                    userImg = it.toString()
                    v.loadCircleImage(it.toString())
                    cardView.setOnClickListener(this)
                }
            }
        }
    }

    private fun updateIconLayoutParams(v: ImageView, selected: Boolean, dimenId: Int) {
        val p = if (selected) 0 else resources.getDimension(dimenId).toInt()
        v.setPadding(p, p, p, p)
    }

    fun updateMessagesCount(newMsgCount: Int) {
        if (newMsgCount > 0) {
            binding.tabActive2.visibility = View.VISIBLE
            binding.tab2IndicatorText.visibility = View.VISIBLE
            binding.tab2IndicatorText.text = newMsgCount.toString()
        } else {
            binding.tabActive2.visibility = View.GONE
            binding.tab2IndicatorText.visibility = View.GONE
        }
    }

}