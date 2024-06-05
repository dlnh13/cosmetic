package com.example.cosmetic.adapters

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cosmetic.R
import com.example.cosmetic.data.Product
import com.example.cosmetic.databinding.FavoriteProductItemRvBinding
import com.example.cosmetic.helper.getProductPrice
import com.example.cosmetic.util.Uid.getUid
import kotlinx.coroutines.cancel

class FavoriteProductsAdapter :
    RecyclerView.Adapter<FavoriteProductsAdapter.FavoriteProductsViewHolder>() {
    private var isLiked = true

    inner class FavoriteProductsViewHolder(val binding: FavoriteProductItemRvBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.apply {
                Glide.with(itemView).load(product.images[0]).into(imageItem)
                tvName.text = product.name
                val priceAfterOffer = product.offerPercentage.getProductPrice(product.price)
                tvDiscountPrice.text = "$ ${String.format("%.2f", priceAfterOffer)}"
                tvPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                if (product.offerPercentage == null)
                    tvDiscountPrice.visibility = View.INVISIBLE
                tvPrice.text = "$ ${product.price}"
                imgFavorite.tag = "liked"
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FavoriteProductsViewHolder {
        return FavoriteProductsViewHolder(
            FavoriteProductItemRvBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(
        holder: FavoriteProductsViewHolder,
        position: Int
    ) {
        val product = differ.currentList[position]
        holder.bind(product)

        holder.itemView.setOnClickListener {
            onClick?.invoke(product)
        }
        holder.binding.imgFavorite.setOnClickListener {
            if (holder.binding.imgFavorite.tag == "liked") {
                holder.binding.imgFavorite.tag = "notlike"
                holder.binding.imgFavorite.setImageResource(R.drawable.ic_favorite)
                isLiked = false
            } else {
                holder.binding.imgFavorite.tag = "liked"
                holder.binding.imgFavorite.setImageResource(R.drawable.ic_favorite_red)
                isLiked = true
            }
            onFavoriteClick?.invoke(product,isLiked)

        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onClick: ((Product) -> Unit)? = null
    var onFavoriteClick: ((Product,Boolean) -> Unit)? = null

    fun clear() {
        differ.submitList(emptyList())
    }
}
