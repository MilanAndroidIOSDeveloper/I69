package com.i69app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.i69app.databinding.ListItemInterestBinding

class InterestListAdapter(private val listener: InterestListener) : RecyclerView.Adapter<InterestListAdapter.ViewHolder>() {

    private val list: ArrayList<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ListItemInterestBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewBinding.apply {
            text.text = list[position]
            root.setOnClickListener { listener.onViewClick(position) }
            removeButton.setOnClickListener { listener.onRemoveBtnClick(position) }
            executePendingBindings()
        }
    }

    override fun getItemCount() = list.size

    fun updateList(updatedList: List<String>) {
        list.clear()
        list.addAll(updatedList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val viewBinding: ListItemInterestBinding) : RecyclerView.ViewHolder(viewBinding.root)

    interface InterestListener {
        fun onViewClick(pos: Int)
        fun onRemoveBtnClick(pos: Int)
    }

}