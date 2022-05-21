package com.i69app.ui.adapters

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.i69app.GetAllNotificationQuery
import com.i69app.GetAllUserStoriesQuery
import com.i69app.databinding.ItemLikesBinding
import com.i69app.databinding.ItemNotificationBinding
import com.i69app.ui.screens.main.moment.UserStoryDetailFragment
import com.i69app.ui.viewModels.CommentsModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class StoryLikesAdapter(
    private val ctx: Context,
    private val alldatas: java.util.ArrayList<CommentsModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun updateList(updatedList: ArrayList<CommentsModel>) {

        alldatas.clear()
        alldatas.addAll(updatedList)
        notifyDataSetChanged()
    }


    fun add(r: CommentsModel) {
        alldatas.add(r)
        notifyItemInserted(alldatas.size - 1)
    }

    fun addAll(newdata: ArrayList<CommentsModel>) {


        newdata.indices.forEach { i ->


            add(newdata[i])
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        holder as NotificationHolder

        val item = alldatas?.get(position)
        holder.bind(position, item)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder =
        NotificationHolder(
            ItemLikesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )



    override fun getItemCount() = alldatas?.size?: 0


    inner class NotificationHolder(val viewBinding: ItemLikesBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(position: Int,item: CommentsModel) {
            //val title = item?.title


            viewBinding.txtNotificationBody.text = item?.commenttext






        }
    }

    interface NotificationListener {
        fun onNotificationClick(position: Int,notificationdata: GetAllNotificationQuery.Edge?)
    }
}