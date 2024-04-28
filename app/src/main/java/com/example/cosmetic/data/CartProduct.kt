package com.example.cosmetic.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartProduct(
    val product: Product,
    val quantity : Int,
    val selectedColor: String? = null,
    val selectedSize: String? = null,
    var selected:Boolean = false
):Parcelable{
    constructor() : this(Product(),1,null,null, false)
}
