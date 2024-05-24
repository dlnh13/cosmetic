package com.example.cosmetic.util

import com.example.cosmetic.data.User
import com.example.cosmetic.data.order.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object Uid {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun getUid(): String {
        return auth.currentUser?.uid!!
    }

    suspend fun getUserName(uid: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val documentSnapshot = firestore.collection("user").document(uid).get().await()
                val user = documentSnapshot.toObject(User::class.java)
                user?.firstName ?: ""
            } catch (e: Exception) {
                // Xử lý lỗi nếu cần
                ""
            }
        }
    }


    suspend fun getProfileImg(uid: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val documentSnapshot = firestore.collection("user").document(uid).get().await()
                val user = documentSnapshot.toObject(User::class.java)
                user?.imagePath ?: ""
            } catch (e: Exception) {
                // Xử lý lỗi nếu cần
                ""
            }
        }
    }
}