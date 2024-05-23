package com.example.cosmetic.data

data class Comment(
    val commentId: String,
    val postId: String,
    val uid: String,
    val time: String? = "",
    val content: String,
    val profileImage: String? ="",
    ) {
    constructor() : this("0","","","","","")
}