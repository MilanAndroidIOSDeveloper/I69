package com.i69app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.i69app.data.models.BlockedUser
import com.i69app.databinding.ItemBlockedUserBinding

class BlockUsersListAdapter(private val listener: BlockListener) : RecyclerView.Adapter<BlockUsersListAdapter.ViewHolder>() {

    private val list: ArrayList<BlockedUser> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(ItemBlockedUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.viewBinding.apply {
            user = list[position]
            unblockButton.setOnClickListener { listener.unblock(user!!) }
            executePendingBindings()
        }
    }

    override fun getItemCount() = list.size

    fun updateList(updatedList: List<BlockedUser>) {
        list.clear()
        list.addAll(updatedList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val viewBinding: ItemBlockedUserBinding) : RecyclerView.ViewHolder(viewBinding.root)

    fun interface BlockListener {
        fun unblock(user: BlockedUser)
    }

}