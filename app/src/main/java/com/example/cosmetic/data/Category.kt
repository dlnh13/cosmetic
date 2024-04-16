package com.example.cosmetic.data

sealed class Category(val category:String){
    object Makeup: Category("Makeup")
    object MakeupTool: Category("Makeup Tool")
    object SkinCare: Category("SkinCare")
    object HairCare: Category("HairCare")
    object Perfume: Category("Perfume")

}