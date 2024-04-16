package com.example.cosmetic.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.cosmetic.data.Product
import com.example.cosmetic.databinding.SpecialItemBinding
import com.example.cosmetic.databinding.ViewpagerImageItemBinding
import kotlinx.coroutines.NonDisposableHandle.parent

class ViewPager2Images : RecyclerView.Adapter<ViewPager2Images.ViewPager2ImagesViewHolder>() {
    inner class ViewPager2ImagesViewHolder(val binding: ViewpagerImageItemBinding) : ViewHolder(binding.root){
        fun bind(imagePath: String){
            Glide.with(itemView).load(imagePath).into(binding.imageProductDetails)
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, diffCallback)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPager2ImagesViewHolder {
        return ViewPager2ImagesViewHolder(
            ViewpagerImageItemBinding.inflate(
                LayoutInflater.from(parent.context),parent,false
            )
        )
    }
    override fun onBindViewHolder(
        holder: ViewPager2ImagesViewHolder,
        position: Int
    ) {
        val image = differ.currentList[position]
        holder.bind(images)
    }
    override fun getItemCount(): Int {
        return differ.currentList.size
    }

}