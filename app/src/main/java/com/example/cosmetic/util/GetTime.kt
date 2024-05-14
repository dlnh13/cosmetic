package com.example.cosmetic.util

import java.text.SimpleDateFormat
import java.util.Date

fun getTime(): String {


    val formatter = SimpleDateFormat("HH:mm:ss")
    val date: Date = Date(System.currentTimeMillis())
    val stringdate = formatter.format(date)


    return stringdate

}