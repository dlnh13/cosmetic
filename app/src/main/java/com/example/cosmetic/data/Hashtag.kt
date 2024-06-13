package com.example.cosmetic.data

data class Hashtag(
    val hashtag: String,
    val postIds: List<String>
){
    constructor() : this("", emptyList())
}
