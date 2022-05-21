package com.i69app.ui.adapters

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.i69app.BuildConfig
import com.i69app.GetReceivedGiftsQuery
import com.i69app.R
import com.i69app.databinding.ItemReceivedSentGiftsBinding
import com.i69app.utils.loadCircleImage
import com.i69app.utils.loadImage
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class AdapterReceiveGifts(
    var context: Context,private val listener: ReceivedGiftPicUserPicInterface,
    var items: MutableList<GetReceivedGiftsQuery.Edge?>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val oldFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val newFormat = SimpleDateFormat("HH:mm a, dd MMM yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

//    var oldFormat = "yyyy-MM-dd HH:mm:ss"
//    var newFormat = "HH:mm a, dd MMM yyyy"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val cView = ItemReceivedSentGiftsBinding.inflate(
            LayoutInflater.from(
                parent.context
            ), parent, false
        )
        return MyViewHolder(cView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MyViewHolder) {
            holder.bind(items[position]!!)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }


    inner class MyViewHolder(var viewBinding: ItemReceivedSentGiftsBinding) :
        RecyclerView.ViewHolder(viewBinding.root) {
        //        init {
//            viewBinding.lytGift.setOnClickListener {
//                items[bindingAdapterPosition].isSelected = items[bindingAdapterPosition].isSelected == false
//                notifyItemChanged(bindingAdapterPosition)
//            }
//        }
        fun bind(item: GetReceivedGiftsQuery.Edge) {
            viewBinding.uname.text = item.node!!.user.fullName.toString()

            val avatarUrl = item?.node!!.user?.avatar
            if (avatarUrl != null) {
                viewBinding.upic.loadCircleImage(avatarUrl.url!!)
            } else {
                viewBinding.upic.loadImage(R.drawable.ic_default_user)
            }

            viewBinding.upic.setOnClickListener(View.OnClickListener {
                listener.onuserpiclicked(item.node!!.user?.id!!)

            })

            viewBinding.gname.text = item.node!!.gift.giftName

            val giftUrl = item?.node!!.gift?.picture
            if (giftUrl != null) {

                viewBinding.gpic.loadCircleImage(BuildConfig.BASE_URL+"/media/"+giftUrl)
            } else {
                viewBinding.gpic.loadImage(R.drawable.ic_default_user)
            }


            viewBinding.gpic.setOnClickListener(View.OnClickListener {
                var url=""
                val giftUrl = item?.node!!.gift?.picture
                if (giftUrl != null) {
                    url = giftUrl!!
                }
                listener.onpiclicked(BuildConfig.BASE_URL+"/media/"+url)
            })


            viewBinding.amount.text = item?.node!!.gift?.cost.roundToInt().toString()


            var text = item?.node!!.purchasedOn.toString()
            text = text.replace("T", " ").substring(0, text.indexOf("."))
//            val momentTime = formatter.parse(text)
//
//            viewBinding.times.text = DateUtils.getRelativeTimeSpanString(momentTime.time, Date().time, DateUtils.MINUTE_IN_MILLIS)


            var formatedDate = ""
            var myDate: Date? = null
            try {
                myDate = oldFormat.parse(text)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            formatedDate = newFormat.format(myDate)

            viewBinding.times.text = formatedDate

        }
    }


    interface ReceivedGiftPicUserPicInterface {
        fun onpiclicked(
            urls: String
        )
        fun onuserpiclicked(
            userid: String
        )
    }
}
