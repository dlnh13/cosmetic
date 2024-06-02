package com.example.cosmetic.adapters

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cosmetic.data.Notification
import com.example.cosmetic.data.Product
import com.example.cosmetic.databinding.NotiItemBinding
import com.example.cosmetic.helper.getProductPrice
import com.google.firebase.firestore.FirebaseFirestore

class NotificationAdapter : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(val binding: NotiItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(notification: Notification, product: Product?) {
            binding.apply {
                if (product != null) {
                    Glide.with(itemView).load(product.images[0]).into(imgProduct)
                    tvNotiContent.text = "${product.name} đang giảm giá ${
                        String.format(
                            "%.0f",
                            product.offerPercentage!! * 100
                        )
                    }% ! Mua ngay !"
                    val priceAfterOffer = product.offerPercentage.getProductPrice(product.price)
                    tvDiscountPrice.text = " ${String.format("%.2f", priceAfterOffer)}"
                    tvPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                    tvPrice.text = " ${product.price}"
                } else {
                    // Show loading or placeholder state
                    tvNotiContent.text = "Loading..."
                    tvDiscountPrice.text = ""
                    tvPrice.text = ""
                }
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem.id == newItem.id // Use unique identifier
        }

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder(
            NotiItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = differ.currentList[position]
        // Bind with a loading state initially
        holder.bind(notification, null)
        // Fetch product details based on notification.productId
        fetchProductDetails(notification.productId) { product ->
            holder.bind(notification, product)
        }
        holder.itemView.setOnClickListener {
            fetchProductDetails(notification.productId){ product ->
                onClick?.invoke(product!!)

            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private fun fetchProductDetails(productId: String, callback: (Product?) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("Products").document(productId)
            .get()
            .addOnSuccessListener { document ->
                val product = document.toObject(Product::class.java)
                callback(product)
            }
            .addOnFailureListener { exception ->
                Log.w("FetchProductDetails", "Error getting product details: ", exception)
                callback(null)
            }
    }

    var onClick: ((Product) -> Unit)? = null
}
