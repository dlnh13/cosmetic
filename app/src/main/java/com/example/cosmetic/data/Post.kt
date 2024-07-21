package com.example.cosmetic.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post
    (
    val postId: String  ,
    val imagePost: List<String>,
    val time: Long? = 0,
    val caption: String? = "",
    val hashtag: List<String>? = null,
    val likes: Int? = 0,
    val uid: String,
    val profileImage: String? ="",
    val likers: List<String>? = null
): Parcelable
{
    constructor() : this("", listOf(), 0, "",null, 0, "", "", null)
}
