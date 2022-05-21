package com.i69app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.i69app.BuildConfig
import com.i69app.data.models.User
import com.i69app.data.remote.responses.DefaultPicker
import com.i69app.databinding.ListItemSearchedUserBinding
import com.i69app.databinding.ListItemUnlockFeatureBinding
import com.i69app.utils.getSelectedValueFromDefaultPicker
import com.i69app.utils.loadCircleImage
import timber.log.Timber

private const val TYPE_UNLOCK_PAID = 0
private const val TYPE_DEFAULT = 1

class UsersSearchListAdapter(private val listener: UserSearchListener, private val defaultPicker: DefaultPicker) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items: ArrayList<User> = ArrayList()

    private fun getViewHolderByType(type: Int, viewBinding: ViewDataBinding) =
        if (type == TYPE_DEFAULT) SearchedUserHolder(viewBinding as ListItemSearchedUserBinding) else UnlockHolder(viewBinding as ListItemUnlockFeatureBinding)

    private fun getViewDataBinding(viewType: Int, parent: ViewGroup) = if (viewType == TYPE_DEFAULT)
        ListItemSearchedUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    else
        ListItemUnlockFeatureBinding.inflate(LayoutInflater.from(parent.context), parent, false)

    override fun getItemViewType(position: Int): Int = if (position == 0) TYPE_UNLOCK_PAID else TYPE_DEFAULT

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): RecyclerView.ViewHolder = getViewHolderByType(type, getViewDataBinding(type, parent))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_UNLOCK_PAID) {
            holder as UnlockHolder
            holder.viewBinding.root.setOnClickListener {
                listener.onUnlockFeatureClick()
            }
        } else {
            holder as SearchedUserHolder
            val item = items[position]
            holder.bind(item)
            holder.viewBinding.root.setOnClickListener {
                holder.viewBinding.photoCover.transitionName = "profilePhoto"
                listener.onItemClick(holder.bindingAdapterPosition, item)
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(updated: List<User>) {
        items.clear()
        items.add(0, User())
        items.addAll(updated)
        notifyDataSetChanged()
    }

    inner class UnlockHolder(val viewBinding: ListItemUnlockFeatureBinding) : RecyclerView.ViewHolder(viewBinding.root)

    inner class SearchedUserHolder(val viewBinding: ListItemSearchedUserBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(user: User) {
            var title = ""
            var subTitle = ""
            if (user.fullName.isNotEmpty()) title += user.fullName
            if (user.age != null) {
                val age = defaultPicker.agePicker.getSelectedValueFromDefaultPicker(user.age)
                if (age.isNotEmpty()) title += ", $age"
            }
            if (!user.work.isNullOrEmpty()) subTitle += (user.work + "\n")
            if (!user.education.isNullOrEmpty()) subTitle += user.education

            viewBinding.userTitle.text = title
            if (subTitle.isEmpty()) {
                viewBinding.userSubTitle.visibility = View.GONE
            } else {
                viewBinding.userSubTitle.text = subTitle
                viewBinding.userSubTitle.visibility = View.VISIBLE
            }
            if(user.avatarPhotos!=null)
            {
                if(user.avatarPhotos!!.size!=0)
                {

                    if(user.avatarIndex!! <= user.avatarPhotos!!.size-1) {
                        val imageUrl = user.avatarPhotos!!.get(user.avatarIndex!!).url.replace(
                            "http://95.216.208.1:8000/media/",
                            "${BuildConfig.BASE_URL}media/"
                        )
                        Timber.d("userprofilephotto $imageUrl")
                        viewBinding.photoCover.loadCircleImage(
                            imageUrl
                        )
                    }
                }

            }
        }
    }

    interface UserSearchListener {
        fun onItemClick(position: Int, user: User)
        fun onUnlockFeatureClick()
    }

}