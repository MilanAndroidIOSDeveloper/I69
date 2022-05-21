package com.i69app.ui.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.contentValuesOf
import com.i69app.data.models.ModelGifts
import com.i69app.databinding.ItemGiftsBinding
import kotlin.coroutines.coroutineContext

class AdapterGifts(var context: Context, var items: MutableList<ModelGifts.Data.AllRealGift>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val cView = ItemGiftsBinding.inflate(
            LayoutInflater.from(
                parent.context
            ), parent, false
        )
        val dm = parent.context.resources.displayMetrics
        var size = dm.widthPixels/3
        size -= (5 * dm.density).toInt()
        cView.lytGift.layoutParams = FrameLayout.LayoutParams (size, size)
        cView.lytGift.requestLayout()
        size -= (2 * dm.density).toInt()
        cView.B.layoutParams = FrameLayout.LayoutParams (size, size)
        cView.BB.layoutParams = FrameLayout.LayoutParams (size, size)
        return MyViewHolder(cView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {
            holder.bind(items[position])
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun getSelected(): List<ModelGifts.Data.AllRealGift> {
        return items.filter { it.isSelected }
    }

    inner class MyViewHolder(var viewBinding: ItemGiftsBinding) : RecyclerView.ViewHolder(viewBinding.root){
        init {
            viewBinding.lytGift.setOnClickListener {
                items[bindingAdapterPosition].isSelected = items[bindingAdapterPosition].isSelected == false
                notifyItemChanged(bindingAdapterPosition)
            }
        }
        fun bind(item: ModelGifts.Data.AllRealGift){
            viewBinding.vm = item
        }
    }
}
