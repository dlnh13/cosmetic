package com.example.cosmetic.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cosmetic.data.Product
import com.example.cosmetic.databinding.SpecialItemBinding

class SpecialProductAdapter :
    RecyclerView.Adapter<SpecialProductAdapter.SpecialProductViewHolder>() {
    inner class SpecialProductViewHolder(private val binding: SpecialItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.apply{
            Glide.with(itemView).load(product.images[0]).into(imgSpecialProduct)
                tvSpecialProductName.text = product.name
                tvPrice.text = product.price.toString()

        }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpecialProductViewHolder {
        return SpecialProductViewHolder(
            SpecialItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

        override fun onBindViewHolder(
            holder: SpecialProductViewHolder,
            position: Int
        ) {
            val product = differ.currentList[position]
            holder.bind(product)
        }

        override fun getItemCount(): Int {
            return differ.currentList.size
        }
    }