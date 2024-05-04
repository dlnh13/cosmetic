package com.example.cosmetic.data.order

sealed class OrderStatus(val status: String) {
    object Ordered : OrderStatus("Đặt hàng")
    object Shipping : OrderStatus("Vận chuyển")
    object Delivered : OrderStatus("Thành công")
    object Canceled : OrderStatus("Huỷ")
    object Returned : OrderStatus("Hoàn đơn")

}

fun getOrderStatus(status: String): OrderStatus {
    return when (status) {
        "Ordered" -> {
            OrderStatus.Ordered
        }

        "Canceled" -> {
            OrderStatus.Canceled
        }

        "Shipping" -> {
            OrderStatus.Shipping
        }

        "Delivered" -> {
            OrderStatus.Delivered
        }

        else -> OrderStatus.Returned
    }
}