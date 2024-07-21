package com.example.cosmetic.data.order

sealed class OrderStatus(val status: String) {
    object Ordered : OrderStatus("Đặt hàng")
    object Shipping : OrderStatus("Vận chuyển")
    object Delivered : OrderStatus("Thành công")
    object Canceled : OrderStatus("Huỷ")
}