package com.i69app.ui.adapters

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.i69app.GetAllNotificationQuery
import com.i69app.GetAllUserMomentsQuery
import com.i69app.databinding.ItemNotificationBinding
import org.xml.sax.Parser
import java.text.SimpleDateFormat
import java.util.*


class NotificationsAdapter(
    private val ctx: Context,
    private val listener: NotificationListener,
    private val allNotifications: ArrayList<GetAllNotificationQuery.Edge>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun updateList(updatedList: ArrayList<GetAllNotificationQuery.Edge>) {

        allNotifications.clear()
        allNotifications.addAll(updatedList)
        notifyDataSetChanged()
    }


    fun add(r: GetAllNotificationQuery.Edge) {
        allNotifications.add(r)
        notifyItemInserted(allNotifications.size - 1)
    }

    fun addAll(newdata: ArrayList<GetAllNotificationQuery.Edge>) {


        newdata.indices.forEach { i ->


            add(newdata[i])
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        holder as NotificationHolder

        val item = allNotifications?.get(position)
        holder.bind(position, item)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder =
        NotificationHolder(
            ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )



    override fun getItemCount() = allNotifications?.size?: 0


    inner class NotificationHolder(val viewBinding: ItemNotificationBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(position: Int,item: GetAllNotificationQuery.Edge?) {
            //val title = item?.title


            viewBinding.txtNotificationTitle.text = item?.node!!.notificationSetting!!.title
            viewBinding.txtNotificationBody.text = item?.node!!.notificationBody


            var text = item?.node!!.createdDate.toString()
            text = text.replace("T", " ").substring(0, text.indexOf("."))
            val momentTime = formatter.parse(text)

            viewBinding.txtNotificationTime.text = DateUtils.getRelativeTimeSpanString(momentTime.time, Date().time, DateUtils.MINUTE_IN_MILLIS)
            viewBinding.root.setOnClickListener {
                listener.onNotificationClick(bindingAdapterPosition, item)
            }

        }
    }

    interface NotificationListener {
        fun onNotificationClick(position: Int,notificationdata: GetAllNotificationQuery.Edge?)
    }
}