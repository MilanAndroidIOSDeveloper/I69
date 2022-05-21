package com.i69app.ui.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.format.DateUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.ViewDataBinding
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.i69app.BuildConfig
import com.i69app.GetAllUserMomentsQuery
import com.i69app.R
import com.i69app.databinding.ItemSharedUserMomentBinding
import com.i69app.ui.screens.main.search.userProfile.SearchUserProfileFragment
import com.i69app.utils.AnimationTypes
import com.i69app.utils.loadCircleImage
import com.i69app.utils.loadImage
import com.i69app.utils.navigate
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*


class NearbySharedMomentAdapter(
    private val ctx: Context,
    private val listener: NearbySharedMomentListener,
    private var allusermoments: ArrayList<GetAllUserMomentsQuery.Edge>,
    var userId: String?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private fun getViewHolderByType(type: Int, viewBinding: ViewDataBinding) = NearbySharedMomentHolder(viewBinding as ItemSharedUserMomentBinding)

    private fun getViewDataBinding(viewType: Int, parent: ViewGroup) = ItemSharedUserMomentBinding.inflate(LayoutInflater.from(parent.context), parent, false)

    override fun onCreateViewHolder(parent: ViewGroup, type: Int): RecyclerView.ViewHolder = getViewHolderByType(type, getViewDataBinding(type, parent))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        holder as NearbySharedMomentHolder
        val item = allusermoments.get(position)
        holder.bind(position, item)
    }

    fun updateList(updatedList: ArrayList<GetAllUserMomentsQuery.Edge>, uid: String?) {

        userId = uid
        allusermoments.clear()
        allusermoments.addAll(updatedList)
        notifyDataSetChanged()
    }


    fun add(r: GetAllUserMomentsQuery.Edge?) {
        allusermoments.add(r!!)
        notifyItemInserted(allusermoments.size - 1)
    }

    fun addAll(newdata: ArrayList<GetAllUserMomentsQuery.Edge>) {


        newdata.indices.forEach { i ->


            add(newdata[i])
        }
    }

    override fun getItemCount(): Int {
        return if (allusermoments == null) 0 else allusermoments?.size!!
    }

    inner class NearbySharedMomentHolder(val viewBinding: ItemSharedUserMomentBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(position: Int, item_data: GetAllUserMomentsQuery.Edge?) {

            val title = item_data?.node!!.user?.fullName

            val s1 = SpannableString("Near by user ")
            val s2 = SpannableString(title!!.toUpperCase())
            val s3 = SpannableString(" has shared a moment")


            s1.setSpan(
                ForegroundColorSpan(Color.WHITE),
                0,
                s1.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            s2.setSpan(
                ForegroundColorSpan(ctx.resources.getColor(R.color.colorPrimary)),
                0,
                s2.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            s2.setSpan(StyleSpan(Typeface.BOLD), 0, s2.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            s3.setSpan(
                ForegroundColorSpan(Color.WHITE),
                0,
                s3.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )


            // build the string
            val builder = SpannableStringBuilder()
            builder.append(s1)
            builder.append(s2)
            builder.append(s3)



            viewBinding.lblItemNearbyName.text = builder


            val url = "${BuildConfig.BASE_URL}media/${item_data.node.file}"
            Timber.d("binnd user avatar= ${item_data.node.user?.avatar}")
            /*if (item?.user?.avatarPhotos?.size!! > 0) {
                Timber.d("binnd user avatar= ${item?.user?.avatarPhotos?.get(0)?.url}")
                val avatarUrl = item?.user?.avatarPhotos?.get(0)?.url!!
                viewBinding.imgNearbyUser.loadCircleImage(avatarUrl)
            }*/

            viewBinding.imgSharedMoment.loadImage(url)

            val avatarUrl = item_data.node.user?.avatar
            if (avatarUrl != null) {
                viewBinding.imgNearbyUser.loadCircleImage(avatarUrl.url!!.replace("http://95.216.208.1:8000/media/","${BuildConfig.BASE_URL}media/"))
            }
            else {
                viewBinding.imgNearbyUser.loadImage(R.drawable.ic_default_user)
            }

            val sb = StringBuilder()
            item_data.node.momentDescriptionPaginated!!.forEach { sb.append(it) }
            val descstring = sb.toString().replace("","")

            viewBinding.txtMomentDescription.text = descstring
            var text = item_data.node.createdDate.toString()
            text = text.replace("T", " ").substring(0, text.indexOf("."))
            val momentTime = formatter.parse(text)

            viewBinding.txtTimeAgo.text = DateUtils.getRelativeTimeSpanString(momentTime.time, Date().time, DateUtils.MINUTE_IN_MILLIS)

            viewBinding.txtNearbyUserLikeCount.setText(""+item_data?.node!!.like)

            viewBinding.lblItemNearbyUserCommentCount.setText(""+item_data?.node!!.comment)
            if(item_data.node.user!!.gender != null){
                if(item_data.node.user!!.gender!!.name.equals("A_0"))
                {

                    viewBinding.imgNearbyUserGift.setImageDrawable(ResourcesCompat.getDrawable(ctx.resources,R.drawable.yellow_gift_male,null))

                }
                else if(item_data.node!!.user!!.gender!!.name.equals("A_1"))
                {
                    viewBinding.imgNearbyUserGift.setImageDrawable(ResourcesCompat.getDrawable(ctx.resources,R.drawable.red_gift_female,null))

                }
                else if(item_data.node!!.user!!.gender!!.name.equals("A_2"))
                {
                    viewBinding.imgNearbyUserGift.setImageDrawable(ResourcesCompat.getDrawable(ctx.resources,R.drawable.purple_gift_nosay,null))

                }
//            else
//            {
//                viewBinding.imgNearbyUserGift.setImageDrawable(ResourcesCompat.getDrawable(ctx.resources,R.drawable.pink_gift_noavb,null))
//
//            }
            }





            /*viewBinding.imgNearbyShare.setImageBitmap(bmp.borderedCircularBitmap())*/
            viewBinding.root.setOnClickListener {
                //holder.viewBinding.photoCover.transitionName = "profilePhoto"
                //listener.onSharedMomentClick(bindingAdapterPosition, item)
            }

            viewBinding.imgNearbyUserLikes.setOnClickListener(View.OnClickListener {



                listener.onLikeofMomentClick(bindingAdapterPosition, item_data)
            })

            viewBinding.imgNearbyUserComment.setOnClickListener(View.OnClickListener {
                listener.onCommentofMomentClick(bindingAdapterPosition,item_data)

            })

            viewBinding.lblViewAllComments.setOnClickListener(View.OnClickListener {
                listener.onCommentofMomentClick(bindingAdapterPosition,item_data)

            })
            viewBinding.itemCell.setOnClickListener(View.OnClickListener {
                listener.onCommentofMomentClick(bindingAdapterPosition,item_data)

            })

            viewBinding.imgNearbyUserGift.setOnClickListener(View.OnClickListener {
                listener.onMomentGiftClick(bindingAdapterPosition,item_data)

            })


            viewBinding.imgNearbySharedMomentOption.setOnClickListener(View.OnClickListener {


                if (userId!!.equals(item_data!!.node!!.user!!.id)) {
                    //creating a popup menu

                    val popup = PopupMenu(ctx, viewBinding.imgNearbySharedMomentOption)
                    popup.getMenuInflater().inflate(R.menu.more_options, popup.getMenu());

                    //adding click listener
                    popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

                        when (item!!.itemId) {
                            R.id.nav_item_delete -> {
                            listener.onDotMenuofMomentClick(bindingAdapterPosition,item_data,"delete")

                            }
                            R.id.nav_item_report -> {
                            listener.onDotMenuofMomentClick(bindingAdapterPosition,item_data,"report")

                            }

                        }

                        true
                    })
                    popup.show()
                }
                else
                {
                    val popup = PopupMenu(ctx, viewBinding.imgNearbySharedMomentOption)
                    popup.getMenuInflater().inflate(R.menu.more_options1, popup.getMenu());

                    //adding click listener
                    popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item: MenuItem? ->

                        when (item!!.itemId) {

                            R.id.nav_item_report -> {
                                listener.onDotMenuofMomentClick(bindingAdapterPosition,item_data,"report")

                            }

                        }

                        true
                    })
                    popup.show()
                }

            })

        }
    }

    interface NearbySharedMomentListener {
        fun onSharedMomentClick(
            position: Int,
            item: GetAllUserMomentsQuery.Edge?
        )
        fun onMoreShareMomentClick()
        fun onLikeofMomentClick(position: Int,
                                item: GetAllUserMomentsQuery.Edge?)
        fun onCommentofMomentClick(position: Int,
                                   item: GetAllUserMomentsQuery.Edge?)
        fun onMomentGiftClick(position: Int,
                                   item: GetAllUserMomentsQuery.Edge?)

        fun onDotMenuofMomentClick(position: Int,
                                   item: GetAllUserMomentsQuery.Edge?,types: String)


    }

}