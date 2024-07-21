package com.example.cosmetic.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(
    val addressTitle: String,
    val fullName: String, // so nha, phuong cu the
    val street: String,
    val phone: String,
    val district: String,
    val city: String
): Parcelable {

    constructor(): this("","","","","","")
}
