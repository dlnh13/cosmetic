package com.example.cosmetic.data

data class Notification(
    val id: String,
    val productId : String,
    val timestamp: Long = 0
){
    constructor() : this("","", 0)
}
