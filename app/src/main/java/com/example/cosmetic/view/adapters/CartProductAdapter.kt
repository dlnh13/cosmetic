package com.example.cosmetic.view.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cosmetic.data.CartProduct
import com.example.cosmetic.databinding.CartProductItemBinding
import com.example.cosmetic.helper.getProductPrice

class CartProductAdapter : RecyclerView.Adapter<CartProductAdapter.CartProductsViewHolder>() {

    inner class CartProductsViewHolder(val binding: CartProductItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cartProduct: CartProduct) {
            binding.apply {
                Glide.with(itemView).load(cartProduct.product.images[0]).into(imageCartProduct)
                tvProductCartName.text = cartProduct.product.name
                tvCartProductQuantity.text = cartProduct.quantity.toString()

                val priceAfterPercentage =
                    cartProduct.product.offerPercentage.getProductPrice(cartProduct.product.price)
                tvProductCartPrice.text = "đ ${String.format("%.2f", priceAfterPercentage)}"

                tvCartProductColor.text = cartProduct.selectedColor ?: ""
                    .also {
                    imageCartProductColor.setImageDrawable(
                        ColorDrawable(Color.WHITE)
                    )}
                    tvCartProductSize.text = cartProduct.selectedSize ?: ""
                        .also {
                        imageCartProductSize.setImageDrawable(
                            ColorDrawable(Color.WHITE)
                        )
                    }
                }
            }
        }

        private val diffCallback = object : DiffUtil.ItemCallback<CartProduct>() {
            override fun areItemsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
                return oldItem.product.id == newItem.product.id
            }

            override fun areContentsTheSame(oldItem: CartProduct, newItem: CartProduct): Boolean {
                return oldItem == newItem
            }
        }

        val differ = AsyncListDiffer(this, diffCallback)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartProductsViewHolder {
            return CartProductsViewHolder(
                CartProductItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: CartProductsViewHolder, position: Int) {
            val cartProduct = differ.currentList[position]
            holder.bind(cartProduct)

            holder.itemView.setOnClickListener {
                onProductClick?.invoke(cartProduct)
            }

            holder.binding.imagePlus.setOnClickListener {
                onPlusClick?.invoke(cartProduct)
            }

            holder.binding.imageMinus.setOnClickListener {
                onMinusClick?.invoke(cartProduct)
            }
            holder.itemView.apply {
                cartProduct.selected = holder.binding.cbChosen.isChecked
                holder.binding.cbChosen.setOnCheckedChangeListener { _, isChecked ->
                    cartProduct.selected = holder.binding.cbChosen.isChecked
                    onCheckboxClick?.invoke(cartProduct, cartProduct.selected)

                }
            }
        }

        override fun getItemCount(): Int {
            return differ.currentList.size
        }

        var onProductClick: ((CartProduct) -> Unit)? = null
        var onPlusClick: ((CartProduct) -> Unit)? = null
        var onMinusClick: ((CartProduct) -> Unit)? = null
        var onCheckboxClick:((CartProduct,Boolean)->Unit)? = null
        }