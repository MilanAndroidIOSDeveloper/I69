package com.i69app.ui.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.i69app.BuildConfig
import com.i69app.GetChatMessagesByRoomIdQuery
import com.i69app.R
import com.i69app.databinding.ItemNewIncomingTextMessageBinding
import com.i69app.databinding.ItemNewOutcomingTextMessageBinding
import com.i69app.utils.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


private const val TYPE_NEW_INCOMING = 0
private const val TYPE_NEW_OUTGOING = 1

class NewChatMessagesAdapter(
    private val ctx: Context,
    private val userId: String?,
    private val listener: ChatMessageListener,
    private val chatMessages: ArrayList<GetChatMessagesByRoomIdQuery.Edge?>?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val displayTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }

    private fun getViewHolderByType(type: Int, viewBinding: ViewDataBinding) =
        if (type == TYPE_NEW_INCOMING) IncomingHolder(viewBinding as ItemNewIncomingTextMessageBinding)
        else OutgoingHolder(viewBinding as ItemNewOutcomingTextMessageBinding)

    private fun getViewDataBinding(viewType: Int, parent: ViewGroup) =
        if (viewType == TYPE_NEW_INCOMING)
            ItemNewIncomingTextMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        else
            ItemNewOutcomingTextMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

    override fun getItemViewType(position: Int): Int {
        val item = chatMessages?.get(position)
        return if (item?.node?.userId?.id == userId) TYPE_NEW_OUTGOING else TYPE_NEW_INCOMING
    }

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): RecyclerView.ViewHolder =
        getViewHolderByType(type, getViewDataBinding(type, parent))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = chatMessages?.get(position)
        if (getItemViewType(position) == TYPE_NEW_INCOMING) {
            holder as IncomingHolder
            holder.bind(item)
        } else {
            holder as OutgoingHolder
            holder.bind(item)
        }
    }

    override fun getItemCount() = chatMessages?.size ?: 0

    private val diffUtilCallBack =
        object : DiffUtil.ItemCallback<GetChatMessagesByRoomIdQuery.Edge>() {
            override fun areItemsTheSame(
                oldItem: GetChatMessagesByRoomIdQuery.Edge,
                newItem: GetChatMessagesByRoomIdQuery.Edge
            ): Boolean {
                return oldItem.node?.id == newItem.node?.id
            }

            override fun areContentsTheSame(
                oldItem: GetChatMessagesByRoomIdQuery.Edge,
                newItem: GetChatMessagesByRoomIdQuery.Edge
            ): Boolean {
                return oldItem == newItem
            }
        }

    private val differ = AsyncListDiffer(this, diffUtilCallBack)


    fun updateList(updatedList: MutableList<GetChatMessagesByRoomIdQuery.Edge?>?) {
        if (updatedList?.isEmpty() == true) {
            differ.submitList(emptyList())
            return
        }
        differ.submitList(updatedList)
        notifyDataSetChanged()
    }

    /*@SuppressLint("NotifyDataSetChanged")
    fun addMessage(message: ChatSubscription.Message?) {
        var url: String
        try {
            url = message?.sender?.avatarPhotos?.get(0)?.url!!
        } catch (e:Exception) {url = ""}
        val avatar = GetChatMessagesByChatIdQuery.AvatarPhoto(url)
        val avatarPhoto = arrayListOf<GetChatMessagesByChatIdQuery.AvatarPhoto>()
        avatarPhoto.add(avatar)
        val sender = GetChatMessagesByChatIdQuery.Sender(message?.sender?.id!!, avatarPhoto, message.sender.username, message.sender.fullName, message.sender.email)
        val node = GetChatMessagesByChatIdQuery.Node(message.id, true, message.created, message.text, sender)
        val edge = Any(node)
        chatMessages?.add(edge)
        notifyDataSetChanged()
    }*/

    inner class IncomingHolder(val viewBinding: ItemNewIncomingTextMessageBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(item: GetChatMessagesByRoomIdQuery.Edge?) {
            val content = item?.node?.content
            if (content?.contains("media/chat_files") == true) {
                var fullUrl = content;
                if (content.startsWith("/media/chat_files/")) {
                    fullUrl = "${BuildConfig.BASE_URL}$content"
                }
                val uri = Uri.parse(fullUrl)
                val lastSegment = uri.lastPathSegment
                val ext = lastSegment?.substring(lastSegment.lastIndexOf(".") + 1)
                if (ext?.isImageFile() == true || ext?.isVideoFile() == true) {
                    viewBinding.messageText.text = ""
                    viewBinding.messageImage.loadImage(fullUrl, RequestOptions().centerCrop(), null, null)
                    viewBinding.messageImage.visibility = View.VISIBLE
                    viewBinding.messageFileIcon.visibility = View.GONE
                    viewBinding.messagePlayIcon.visibility = if (ext.isVideoFile()) View.VISIBLE else View.GONE
                } else {
                    viewBinding.messageText.text = lastSegment
                    viewBinding.messageImage.setImageBitmap(null)
                    viewBinding.messageFileIcon.visibility = View.VISIBLE
                    viewBinding.messageImage.visibility = View.GONE
                    viewBinding.messagePlayIcon.visibility = View.GONE
                }
            } else {
                viewBinding.messageText.text = content
                viewBinding.messageImage.setImageBitmap(null)
                viewBinding.messageImage.visibility = View.GONE
                viewBinding.messageFileIcon.visibility = View.GONE
                viewBinding.messagePlayIcon.visibility = View.GONE
            }
            var avatarUrl: String? = ""
            try {
                avatarUrl = item?.node?.userId?.avatarPhotos?.get(item.node.userId.avatarIndex)?.url

            } catch (e: Exception) {
                avatarUrl = ""
            }
            if(avatarUrl != null)
            {
                viewBinding.messageUserAvatar.loadCircleImage(avatarUrl!!)

            }
            else
            {
                viewBinding.messageUserAvatar.loadCircleImage(R.drawable.logo)
            }

            var text = item?.node?.timestamp.toString()
            val messageTime: Date
            try {
                text = text.replace("T", " ").substring(0, text.indexOf("."))
                messageTime = formatter.parse(text)
                viewBinding.messageTime.text = displayTime.format(messageTime)
                val displayDate = formatDayDate(ctx, messageTime.time)
                // code for display date label
                if (bindingAdapterPosition == 0) {
                    if (itemCount == 1) {
                        viewBinding.lblDate.visibility = View.VISIBLE
                        viewBinding.lblDate.text = displayDate
                    }
                } else {
                    val preItem = chatMessages?.get(bindingAdapterPosition - 1)
                    var preTime = preItem?.node?.timestamp.toString()
                    preTime = preTime.replace("T", " ").substring(0, preTime.indexOf("."))
                    val preDate = formatter.parse(preTime)
                    val preDisplayDate = formatDayDate(ctx, preDate.time)
                    if (displayDate?.equals(preDisplayDate) == true) {
                        viewBinding.lblDate.text = ""
                        viewBinding.lblDate.visibility = View.GONE
                    } else {
                        viewBinding.lblDate.visibility = View.VISIBLE
                        viewBinding.lblDate.text = displayDate
                    }
                }
            } catch (e: Exception) {
                viewBinding.messageTime.text = e.message
                viewBinding.lblDate.visibility = View.GONE
            }

            viewBinding.root.setOnClickListener {
                //holder.viewBinding.photoCover.transitionName = "profilePhoto"
                if(!item!!.node!!.roomId.name.equals("") || !item.node!!.roomId.id.equals(""))
                {
                    listener.onChatMessageClick(bindingAdapterPosition, item)

                }
            }
            viewBinding.messageUserAvatar.setOnClickListener {
                //holder.viewBinding.photoCover.transitionName = "profilePhoto"
                if(!item!!.node!!.roomId.name.equals("") || !item.node!!.roomId.id.equals("")) {
                    listener.onChatUserAvtarClick()
                }

            }

        }
    }

    inner class OutgoingHolder(val viewBinding: ItemNewOutcomingTextMessageBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(item: GetChatMessagesByRoomIdQuery.Edge?) {
            val content = item?.node?.content
            if (content?.contains("media/chat_files") == true) {
                var fullUrl = content;
                if (content.startsWith("/media/chat_files/")) {
                    fullUrl = "${BuildConfig.BASE_URL}$content"
                }
                val uri = Uri.parse(fullUrl)
                val lastSegment = uri.lastPathSegment
                val ext = lastSegment?.substring(lastSegment.lastIndexOf(".") + 1)
                Timber.d("clickk $lastSegment $ext")
                if (ext?.isImageFile() == true || ext?.isVideoFile() == true) {
                    viewBinding.messageText.text = ""
                    viewBinding.messageImage.loadImage(fullUrl, RequestOptions().centerCrop(), null, null)
                    viewBinding.messageImage.visibility = View.VISIBLE
                    viewBinding.messageFileIcon.visibility = View.GONE
                    viewBinding.messagePlayIcon.visibility = if (ext.isVideoFile()) View.VISIBLE else View.GONE
                } else {
                    viewBinding.messageText.text = lastSegment
                    viewBinding.messageImage.setImageBitmap(null)
                    viewBinding.messageFileIcon.visibility = View.VISIBLE
                    viewBinding.messageImage.visibility = View.GONE
                    viewBinding.messagePlayIcon.visibility = View.GONE
                }
            } else {
                viewBinding.messageText.text = content
                viewBinding.messageImage.setImageBitmap(null)
                viewBinding.messageImage.visibility = View.GONE
                viewBinding.messageFileIcon.visibility = View.GONE
                viewBinding.messagePlayIcon.visibility = View.GONE
            }

            //Timber.d("messgg $content ${content?.contains("media/chat_files")}")
            var text = item?.node?.timestamp.toString()
            val messageTime: Date?
            try {
                text = text.replace("T", " ").substring(0, text.indexOf("."))
                messageTime = formatter.parse(text)
                viewBinding.messageTime.text = displayTime.format(messageTime)
                val displayDate = formatDayDate(ctx, messageTime.time)

                // code for display date label
                if (bindingAdapterPosition == 0) {
                    if (itemCount == 1) {
                        viewBinding.lblDate.visibility = View.VISIBLE
                        viewBinding.lblDate.text = displayDate
                    }
                    Timber.d("lblb $bindingAdapterPosition == $displayDate == ${viewBinding.messageTime.text} ${item?.node?.timestamp}")
                } else {
                    val preItem = chatMessages?.get(bindingAdapterPosition - 1)
                    var preTime = preItem?.node?.timestamp.toString()
                    preTime = preTime.replace("T", " ").substring(0, preTime.indexOf("."))
                    val preDate = formatter.parse(preTime)
                    val preDisplayDate = formatDayDate(ctx, preDate.time)
                    Timber.d("lblb $bindingAdapterPosition $preDisplayDate == $displayDate == ${viewBinding.messageTime.text}  $content")
                    if (displayDate?.equals(preDisplayDate) == true) {
                        viewBinding.lblDate.text = ""
                        viewBinding.lblDate.visibility = View.GONE
                    } else {
                        viewBinding.lblDate.visibility = View.VISIBLE
                        viewBinding.lblDate.text = displayDate
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                viewBinding.messageTime.text = e.message
            }

            viewBinding.root.setOnClickListener {
                //holder.viewBinding.photoCover.transitionName = "profilePhoto"
                if(!item!!.node!!.roomId.name.equals("") || !item.node!!.roomId.id.equals("")) {
                    listener.onChatMessageClick(bindingAdapterPosition, item)
                }
            }
        }
    }

    interface ChatMessageListener {
        fun onChatMessageClick(position: Int, message: GetChatMessagesByRoomIdQuery.Edge?)
        fun onChatUserAvtarClick()
    }
}

