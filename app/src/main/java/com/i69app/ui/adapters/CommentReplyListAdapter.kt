package com.i69app.ui.adapters

import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.i69app.R
import com.i69app.ui.viewModels.ReplysModel
import com.i69app.databinding.ItemCommentReplyBinding
import com.i69app.ui.screens.main.search.userProfile.SearchUserProfileFragment
import com.i69app.ui.viewModels.CommentsModel
import com.i69app.utils.AnimationTypes
import com.i69app.utils.loadCircleImage
import com.i69app.utils.loadImage_bysize
import com.i69app.utils.navigate
import java.text.SimpleDateFormat
import java.util.*

class CommentReplyListAdapter(
    private val ctx: Context,
    private val listener: CommentReplyListAdapter.ClickonListener,
    private var allusermoments: List<ReplysModel>?
) : RecyclerView.Adapter<CommentReplyListAdapter.ViewHolder>() {
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    override fun onBindViewHolder(holder: CommentReplyListAdapter.ViewHolder, position: Int) {

        val item = allusermoments?.get(position)
        holder.bind(position, item)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CommentReplyListAdapter.ViewHolder =
        ViewHolder(
            ItemCommentReplyBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )


    override fun getItemCount(): Int {
        return if (allusermoments == null) 0 else allusermoments?.size!!
    }


    inner class ViewHolder(val viewBinding: ItemCommentReplyBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(position: Int, item: ReplysModel?) {

            viewBinding.txtMomentDescription.setText(item!!.replytext)
            viewBinding.imgNearbyUser.loadCircleImage(item!!.userurl.toString())
            viewBinding.lblItemNearbyName.setText(item.usernames!!.uppercase())


            var text = item.timeago
            text = text!!.replace("T", " ").substring(0, text.indexOf("."))
            val momentTime = formatter.parse(text)

            viewBinding.txtTimeAgo.text = DateUtils.getRelativeTimeSpanString(momentTime.time, Date().time, DateUtils.MINUTE_IN_MILLIS)


            viewBinding.lblItemNearbyName.setOnClickListener(View.OnClickListener {

                listener.onUsernameClick(bindingAdapterPosition, item)



            })
            viewBinding.imgNearbyUser.setOnClickListener(View.OnClickListener {
                listener.onUsernameClick(bindingAdapterPosition, item)



            })
        }


    }

    interface ClickonListener {

        fun onUsernameClick(position: Int,
                            models: ReplysModel)
    }
}