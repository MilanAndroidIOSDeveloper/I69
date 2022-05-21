package com.i69app.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.i69app.BuildConfig
import com.i69app.GetAllUserStoriesQuery
import com.i69app.databinding.ItemAddNewNearbyThumbBinding
import com.i69app.databinding.ItemNearbyThumbBinding
import com.i69app.utils.loadCircleImage


private const val TYPE_NEW_STORY = 0
private const val TYPE_DEFAULT = 1
class UserStoriesAdapter (
    private val ctx: Context,
    private val listener: UserStoryListener,
    private val allUserStories: List<GetAllUserStoriesQuery.Edge?>?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private fun getViewHolderByType(type: Int, viewBinding: ViewDataBinding) =
        if (type == TYPE_DEFAULT) UserStoryHolder(viewBinding as ItemNearbyThumbBinding) else NewUserStoryHolder(viewBinding as ItemAddNewNearbyThumbBinding)

    private fun getViewDataBinding(viewType: Int, parent: ViewGroup) = if (viewType == TYPE_DEFAULT)
        ItemNearbyThumbBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    else
        ItemAddNewNearbyThumbBinding.inflate(LayoutInflater.from(parent.context), parent, false)

    override fun getItemViewType(position: Int): Int = if (position == 0) TYPE_NEW_STORY else TYPE_DEFAULT

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): RecyclerView.ViewHolder = getViewHolderByType(type, getViewDataBinding(type, parent))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_NEW_STORY) {
            holder as NewUserStoryHolder
            holder.viewBinding.root.setOnClickListener {
                listener.onAddNewUserStoryClick()
            }
            holder.viewBinding.addstorytext.setText("ADD STORY")
        } else {
            holder as UserStoryHolder
            val item = allUserStories?.get(position-1)
            holder.bind(item)

            holder.viewBinding.root.setOnClickListener {
                //holder.viewBinding.photoCover.transitionName = "profilePhoto"
                listener.onUserStoryClick(holder.bindingAdapterPosition, item)
            }
        }
    }

    override fun getItemCount() = (allUserStories?.size?.plus(1)) ?: 1

    inner class NewUserStoryHolder(val viewBinding: ItemAddNewNearbyThumbBinding) : RecyclerView.ViewHolder(viewBinding.root)

    inner class UserStoryHolder(val viewBinding: ItemNearbyThumbBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(item: GetAllUserStoriesQuery.Edge?) {
            val title = item?.node!!.user?.fullName
            viewBinding.txtItemNearbyName.text = title
            val storyImage: String
            if (item?.node.fileType.equals("video")) {
                storyImage = "${BuildConfig.BASE_URL}media/${item?.node.thumbnail}"
            }
            else {
                storyImage = "${BuildConfig.BASE_URL}media/${item?.node.file}"
            }
            viewBinding.imgUserStory.loadCircleImage(storyImage)
            var url: String? = ""
            if ((item?.node.user?.avatarPhotos?.size!! > 0) && (item?.node.user?.avatarIndex < item?.node.user?.avatarPhotos.size)) {
                url = item.node.user.avatarPhotos[item.node.user.avatarIndex].url
            }
            viewBinding.imgNearbyProfile.loadCircleImage(url!!.replace("http://95.216.208.1:8000/media/","${BuildConfig.BASE_URL}media/"))
        }
    }

    interface UserStoryListener {
        fun onUserStoryClick(position: Int, userStory: GetAllUserStoriesQuery.Edge?)
        fun onAddNewUserStoryClick()
    }
}