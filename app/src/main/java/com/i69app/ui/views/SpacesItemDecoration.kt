package com.i69app.ui.views

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView


class SpacesItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val isFirstOrSecond = arrayOf(0, 1).contains(position)
        if (isFirstOrSecond) {
            outRect.top = space
        }
        if (position == 0) {
            outRect.top = space
            // don't recycle bottom if first item is also last
            // should keep bottom padding set above
        }
    }
}