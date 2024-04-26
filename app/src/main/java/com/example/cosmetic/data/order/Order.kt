package com.example.cosmetic.data.order

import com.example.cosmetic.data.Address
import com.example.cosmetic.data.CartProduct

data class Order(
    val orderStatus: String,
    val totalPrice: Float,
    val products: List<CartProduct>,
    val address: Address
)