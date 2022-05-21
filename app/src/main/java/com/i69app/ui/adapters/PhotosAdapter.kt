package com.i69app.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.i69app.databinding.ProfilePhotoThumbnailLayoutBinding
import com.i69app.utils.loadImage

class PhotosAdapter(private val listener: PhotoAdapterListener) : RecyclerView.Adapter<PhotosAdapter.MyViewHolder>() {

    val photos: ArrayList<String> = ArrayList()
    var avtar_index: Int = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder =
        MyViewHolder(ProfilePhotoThumbnailLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.viewBinding.apply {
            photo.loadImage(photos[holder.bindingAdapterPosition])
            removePhotoButton.setOnClickListener {

                Log.d("INDEX",""+avtar_index)

                if(avtar_index == holder.bindingAdapterPosition)
                {
                    avtar_index = 0
                }
                else if (holder.bindingAdapterPosition < avtar_index)
                {
                    avtar_index = avtar_index - 1
                }

                listener.onRemoveBtnClick(holder.bindingAdapterPosition,photos[holder.bindingAdapterPosition])
                notifyDataSetChanged()



            }

            photo.setOnClickListener(View.OnClickListener {
                avtar_index = position
                notifyDataSetChanged()

            })
        }


        if(avtar_index==position)
        {
            holder.viewBinding.BB.visibility = View.VISIBLE
        }
        else
        {
            holder.viewBinding.BB.visibility = View.GONE

        }
    }

    override fun getItemCount() = photos.size

    fun updateList(updated: List<String>) {
        photos.clear()
        photos.addAll(updated)
        notifyDataSetChanged()
    }

    fun  addItem(newPhoto: String) {
        photos.add(newPhoto)
        if (photos.size == 1) notifyDataSetChanged() else notifyItemInserted(photos.size - 1)
    }

    fun removeItem(pos: Int) {

        photos.removeAt(pos)
        if (photos.isEmpty()) notifyDataSetChanged() else notifyItemRemoved(pos)
    }

    class MyViewHolder(val viewBinding: ProfilePhotoThumbnailLayoutBinding) : RecyclerView.ViewHolder(viewBinding.root)

    fun interface PhotoAdapterListener {
        fun onRemoveBtnClick(position: Int, photo_url: String)
    }

}