package com.example.cosmetic.util

import android.util.Patterns

fun validateEmail(email: String): RegisterValidation{
    if (email.isEmpty())
        return RegisterValidation.Failed("Email không được để trống")

    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        return RegisterValidation.Failed("Sai định dạng email")

    return RegisterValidation.Success
}

fun validatePassword(password: String): RegisterValidation{
    if (password.isEmpty())
        return RegisterValidation.Failed("Password không được để trống")

    if (password.length < 6)
        return RegisterValidation.Failed("Password phải chứa 6 kí tự trở lên")

    return RegisterValidation.Success
}