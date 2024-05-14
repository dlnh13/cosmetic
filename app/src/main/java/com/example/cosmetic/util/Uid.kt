package com.example.cosmetic.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object Uid {
    private val auth = FirebaseAuth.getInstance()

    fun getUid(): String {
        return auth.currentUser?.uid!!
    }
}