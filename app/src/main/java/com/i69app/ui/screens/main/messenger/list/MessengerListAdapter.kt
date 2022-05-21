package com.i69app.ui.screens.main.messenger.list

import android.os.Build
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.i69app.GetAllRoomsQuery
import com.i69app.R
import com.i69app.databinding.ItemMessageBinding
import com.i69app.utils.findFileExtension
import com.i69app.utils.isImageFile
import com.i69app.utils.isVideoFile
import com.i69app.utils.loadCircleImage
import java.text.SimpleDateFormat
import java.util.*

class MessengerListAdapter(
    private val listener: MessagesListListener,
    private val userId: String?
) : RecyclerView.Adapter<MessengerListAdapter.ViewHolder>() {

    private val itemColors = listOf(
        R.color.message_list_container_1, R.color.message_list_container_2,
        R.color.message_list_container_3, R.color.message_list_container_4,
        R.color.message_list_container_5
    )
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val textPairColors = listOf(
        Pair(R.color.message_list_text_title_color_1, R.color.message_list_description_color_1),
        Pair(R.color.message_list_text_title_color_2, R.color.message_list_description_color_2),
        Pair(R.color.message_list_text_title_color_3, R.color.message_list_description_color_3),
        Pair(R.color.message_list_text_title_color_4, R.color.message_list_description_color_4),
        Pair(R.color.message_list_text_title_color_5, R.color.message_list_description_color_5)
    )

    private val diffUtilCallBack = object : DiffUtil.ItemCallback<GetAllRoomsQuery.Edge>() {
        override fun areItemsTheSame(
            oldItem: GetAllRoomsQuery.Edge,
            newItem: GetAllRoomsQuery.Edge
        ): Boolean {
            return oldItem.toString() == newItem.toString()
        }

        override fun areContentsTheSame(
            oldItem: GetAllRoomsQuery.Edge,
            newItem: GetAllRoomsQuery.Edge
        ): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncListDiffer(this, diffUtilCallBack)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MessengerListAdapter.ViewHolder =
        ViewHolder(ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: MessengerListAdapter.ViewHolder, position: Int) {

        val context = holder.viewBinding.root.context
        val roomdata = differ.currentList[position]

        var msg: GetAllRoomsQuery.Node1? = null
        if (roomdata.node!!.messageSet.edges.size == 0) {
            msg = null
        } else {
            msg = roomdata.node.messageSet.edges[0]!!.node!!
        }

        var option: Int

        holder.viewBinding.apply {


            this.obj = roomdata
            if (roomdata.node.userId.id.equals(userId)) {
                roomdata.node.target.avatar.let { imgSrc ->
                    if (imgSrc != null) {
                        img.loadCircleImage(imgSrc.url!!)

                    } else {
                        img.loadCircleImage("")

                    }
                }

                title.text = roomdata.node.target.fullName


                if (roomdata.node.target.isOnline!!) {
                    onlineStatus.setImageResource(R.drawable.round_green)
                } else {
                    onlineStatus.setImageResource(R.drawable.round_yellow)

                }

                if (!roomdata.node.unread.equals("0")) {
                    option = 1
                    unseenMessages.text = roomdata.node.unread
                    unseenMessages.visibility = View.VISIBLE
                } else {
                    option = 0
                    unseenMessages.visibility = View.GONE


                }


                root.setBackgroundColor(ContextCompat.getColor(context, itemColors[option]))
                title.setTextColor(ContextCompat.getColor(context, textPairColors[option].first))
                subtitle.setTextColor(
                    ContextCompat.getColor(
                        context,
                        textPairColors[option].second
                    )
                )
                continueDialog.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        textPairColors[option].second
                    )
                )

                if (msg?.content?.contains("media/chat_files") == true) {
                    val ext = msg.content.findFileExtension()
                    val stringResId =
                        if (ext.isImageFile()) {
                            R.string.photo
                        } else if (ext.isVideoFile()) {
                            R.string.video
                        } else {
                            R.string.file
                        }
                    val icon =
                        if (ext.isImageFile()) {
                            R.drawable.ic_photo
                        } else if (ext.isVideoFile()) {
                            R.drawable.ic_video
                        } else {
                            R.drawable.ic_baseline_attach_file_24
                        }
                    subtitle.text = context.getString(stringResId)
                    subtitle.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                            context,
                            icon
                        ), null, null, null
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        subtitle.compoundDrawableTintList = ContextCompat.getColorStateList(
                            context,
                            textPairColors[option].second
                        )
                    }
                } else {
                    if (roomdata.node.id == "firstName" || roomdata.node?.id == "broadcast") {

                        subtitle.text = roomdata.node.name

                    }
                    else
                    {
                        subtitle.text = msg?.content

                    }
                }

                if (roomdata.node.lastModified != null) {
                    var text = roomdata.node.lastModified.toString()
                    if(!text.equals("")) {
                        text = text.replace("T", " ").substring(0, text.indexOf("."))
                        val momentTime = formatter.parse(text)

                        time.text = DateUtils.getRelativeTimeSpanString(
                            momentTime.time,
                            Date().time,
                            DateUtils.MINUTE_IN_MILLIS
                        )
                    }
                }


            } else {
                roomdata.node.userId.avatar.let { imgSrc ->
                    when {
                        imgSrc != null -> {
                            img.loadCircleImage(imgSrc.url!!)
                        }
                        roomdata.node.id == "firstName" || roomdata.node.id == "broadcast" -> {
                            img.loadCircleImage(R.drawable.logo)
                        }
                        else -> {
                            img.loadCircleImage("")
                        }
                    }
                }

                title.text = roomdata.node.userId.fullName


                if (roomdata.node.userId.isOnline == true) {
                    onlineStatus.setImageResource(R.drawable.round_green)
                } else {
                    onlineStatus.isVisible = !(roomdata.node.id == "firstName" || roomdata.node.id == "broadcast")
                    onlineStatus.setImageResource(R.drawable.round_yellow)
                }
                if (!roomdata.node.unread.equals("0")) {
                    option = 1
                    unseenMessages.text = roomdata.node.unread
                    unseenMessages.visibility = View.VISIBLE
                } else {
                    option = 0
                    unseenMessages.visibility = View.GONE


                }

                root.setBackgroundColor(ContextCompat.getColor(context, itemColors[option]))
                title.setTextColor(ContextCompat.getColor(context, textPairColors[option].first))
                subtitle.setTextColor(
                    ContextCompat.getColor(
                        context,
                        textPairColors[option].second
                    )
                )
                continueDialog.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        textPairColors[option].second
                    )
                )

                if (msg?.content?.contains("media/chat_files") == true) {
                    val ext = msg.content.findFileExtension()
                    val stringResId =
                        if (ext.isImageFile()) {
                            R.string.photo
                        } else if (ext.isVideoFile()) {
                            R.string.video
                        } else {
                            R.string.file
                        }
                    val icon =
                        when {
                            ext.isImageFile() -> {
                                R.drawable.ic_photo
                            }
                            ext.isVideoFile() -> {
                                R.drawable.ic_video
                            }
                            else -> {
                                R.drawable.ic_baseline_attach_file_24
                            }
                        }
                    subtitle.text = context.getString(stringResId)
                    subtitle.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                            context,
                            icon
                        ), null, null, null
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        subtitle.compoundDrawableTintList = ContextCompat.getColorStateList(
                            context,
                            textPairColors[option].second
                        )
                    }
                } else {
                    if (roomdata.node.id == "firstName" || roomdata.node?.id == "broadcast") {

                        subtitle.text = roomdata.node.name

                    }
                    else
                    {
                        subtitle.text = msg?.content

                    }
                }

                if (roomdata.node.lastModified != null) {
                    var text = roomdata.node.lastModified.toString()
                    if(!text.equals(""))
                    {
                        text = text.replace("T", " ").substring(0, text.indexOf("."))
                        val momentTime = formatter.parse(text)

                        time.text = DateUtils.getRelativeTimeSpanString(
                            momentTime.time,
                            Date().time,
                            DateUtils.MINUTE_IN_MILLIS
                        )
                    }

                }


            }



            root.setOnClickListener { listener.onItemClick(roomdata) }
            executePendingBindings()
        }
    }

    override fun getItemCount() = differ.currentList.size

    fun updateList(updatedList: List<GetAllRoomsQuery.Edge?>) {
        if (updatedList.isEmpty()) {
            differ.submitList(emptyList())
            return
        }
        differ.submitList(updatedList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val viewBinding: ItemMessageBinding) :
        RecyclerView.ViewHolder(viewBinding.root)

    fun interface MessagesListListener {
        fun onItemClick(userId: GetAllRoomsQuery.Edge)
    }

}