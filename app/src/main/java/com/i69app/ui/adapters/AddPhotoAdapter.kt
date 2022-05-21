package com.i69app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.i69app.databinding.ButtonAddPhotoLayoutBinding

class AddPhotoAdapter(private val listener: AddPhotoAdapterListener) : RecyclerView.Adapter<AddPhotoAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder =
        MyViewHolder(
            ButtonAddPhotoLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.viewBinding.apply {
            root.setOnClickListener {
                listener.onAddButtonClick()
            }
        }
    }

    override fun getItemCount() = 1

    class MyViewHolder(val viewBinding: ButtonAddPhotoLayoutBinding) : RecyclerView.ViewHolder(viewBinding.root)

    fun interface AddPhotoAdapterListener {
        fun onAddButtonClick()
    }

}