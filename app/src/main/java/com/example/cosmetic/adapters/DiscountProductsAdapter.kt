package com.example.cosmetic.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cosmetic.data.Product
import com.example.cosmetic.databinding.DiscountProductRvItemBinding
import com.example.cosmetic.helper.getProductPrice

class DiscountProductsAdapter : RecyclerView.Adapter<DiscountProductsAdapter.DiscountProductsViewHolder>() {
    inner class DiscountProductsViewHolder(private val binding: DiscountProductRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.apply{
                Glide.with(itemView).load(product.images[0]).into(imgProduct)
                tvName.text = product.name
                    val priceAfterOffer = product.offerPercentage.getProductPrice(product.price)
                    tvNewPrice.text = "$ ${String.format("%.2f", priceAfterOffer)}"
                    tvPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

                if(product.offerPercentage == null)
                    tvNewPrice.visibility = View.INVISIBLE
                tvPrice.text = "$ ${product.price}"
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscountProductsViewHolder {
        return DiscountProductsViewHolder(
            DiscountProductRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(
        holder: DiscountProductsViewHolder,
        position: Int
    ) {
        val product = differ.currentList[position]
        holder.bind(product)

        holder.itemView.setOnClickListener {
            onClick?.invoke(product)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
    var onClick: ((Product) -> Unit)? = null

}