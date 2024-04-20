package com.example.cosmetic.util

import androidx.fragment.app.Fragment
import com.example.cosmetic.R
import com.example.cosmetic.activities.ShoppingActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

fun Fragment.hideBottomNavigationView(){
    val bottomNavigationView =
        (activity as ShoppingActivity).findViewById<BottomNavigationView>(
            com.example.cosmetic.R.id.bottomNavigation
        )
    bottomNavigationView.visibility = android.view.View.GONE
}

fun Fragment.showBottomNavigationView(){
    val bottomNavigationView =
        (activity as ShoppingActivity).findViewById<BottomNavigationView>(
            com.example.cosmetic.R.id.bottomNavigation
        )
    bottomNavigationView.visibility = android.view.View.VISIBLE
}