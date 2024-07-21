package com.example.cosmetic.data

data class Comment(
    val commentId: String,
    val postId: String,
    val uid: String,
    val time: Long? = 0,
    val content: String,
    val profileImage: String? ="",
    ) {
    constructor() : this("0","","",0,"","")
}