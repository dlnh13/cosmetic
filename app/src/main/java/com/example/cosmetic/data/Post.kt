package com.example.cosmetic.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post
    (
    val postId: String  ,
    val imagePost: List<String>,
    val time: String? = "",
    val caption: String? = "",
    val hashtag: List<String>? = null,
    val likes: Int? = 0,
    val uid: String,
    val profileImage: String? ="",
    val likers: List<String>? = null
): Parcelable
{
    constructor() : this("", listOf(), "", "",null, 0, "", "", null)
}
