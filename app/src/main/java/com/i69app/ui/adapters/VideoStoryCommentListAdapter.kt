package com.i69app.ui.adapters

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.i69app.GetAllUserMomentsQuery
import com.i69app.ui.viewModels.CommentsModel
import com.i69app.databinding.ItemCommentMomentBinding
import com.i69app.ui.screens.main.moment.PlayUserStoryDialogFragment
import com.i69app.ui.screens.main.moment.UserStoryDetailFragment
import com.i69app.utils.loadCircleImage
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class VideoStoryCommentListAdapter(
    private val ctx: Context,
    private val listener: PlayUserStoryDialogFragment,
    private var allusermoments: ArrayList<CommentsModel>?,
    private var momentAddCommentFragment: PlayUserStoryDialogFragment
) : RecyclerView.Adapter<VideoStoryCommentListAdapter.ViewHolder>() {
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun onBindViewHolder(holder: VideoStoryCommentListAdapter.ViewHolder, position: Int) {

        val item = allusermoments?.get(position)
        holder.bind(position, item)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VideoStoryCommentListAdapter.ViewHolder =
        ViewHolder(
            ItemCommentMomentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )


    override fun getItemCount(): Int {
        return if (allusermoments == null) 0 else allusermoments?.size!!
    }

    fun updateList(updatedList: List<CommentsModel>) {
        allusermoments!!.clear()
        allusermoments!!.addAll(updatedList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val viewBinding: ItemCommentMomentBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(position: Int, item: CommentsModel?) {

            viewBinding.txtMomentDescription.setText(item!!.commenttext)
            viewBinding.imgNearbyUser.loadCircleImage(item!!.userurl.toString())
            viewBinding.lblItemNearbyName.setText(item.username?.uppercase())
            viewBinding.txtNearbyUserLikeCount.setText(item.cmtlikes)


            var text = item.timeago
            text = text!!.replace("T", " ").substring(0, text.indexOf("."))
            val momentTime = formatter.parse(text)

            viewBinding.txtTimeAgo.text = DateUtils.getRelativeTimeSpanString(
                momentTime.time,
                Date().time,
                DateUtils.MINUTE_IN_MILLIS
            )


//            val headlineAdapter = CommentReplyListAdapter(ctx,momentAddCommentFragment, item.replylist)
//            viewBinding.rvReplies.adapter = headlineAdapter
//            viewBinding.rvReplies.layoutManager = LinearLayoutManager(ctx)
            viewBinding.txtMomentDescription.setOnClickListener { onItemClicked(item) }
//            if (item.isExpanded!!) {
//                viewBinding.rvReplies.visibility = View.VISIBLE
////                holder.ivArrow.setImageResource(R.drawable.ic_arrow_up)
//            } else {
//                viewBinding.rvReplies.visibility = View.GONE
////                holder.ivArrow.setImageResource(R.drawable.ic_arrow_down)
//            }





            viewBinding.imgNearbyUserComment.setOnClickListener(View.OnClickListener {
                listener.onreply(bindingAdapterPosition, item)

            })
            viewBinding.imgNearbyUserLikes.setOnClickListener(View.OnClickListener {
                listener.oncommentLike(bindingAdapterPosition, item)

            })
            viewBinding.lblItemNearbyName.setOnClickListener(View.OnClickListener {
                listener.onUsernameClick(bindingAdapterPosition, item)


            })

            viewBinding.imgNearbyUser.setOnClickListener(View.OnClickListener {
                listener.onUsernameClick(bindingAdapterPosition, item)


            })


        }

        private fun onItemClicked(newspaperModel: CommentsModel?) {
            newspaperModel?.isExpanded = !newspaperModel?.isExpanded!!
            notifyDataSetChanged()
        }
    }

    interface ClickPerformListener {
        fun onreply(position: Int, models: CommentsModel)
        fun oncommentLike(position: Int, models: CommentsModel)
        fun onUsernameClick(position: Int,
                            models: CommentsModel)
    }


}