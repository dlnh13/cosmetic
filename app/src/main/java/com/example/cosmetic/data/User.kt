package com.example.cosmetic.data

data class User(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber:String = "",
    var imagePath: String = ""
){
    constructor(): this("","","","","")
}
