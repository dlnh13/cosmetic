package com.example.cosmetic.util

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.example.cosmetic.view.activities.LoginRegisterActivity
import com.example.cosmetic.data.User
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
                "${user?.firstName} ${user?.lastName}" ?: ""
            } catch (e: Exception) {
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
                ""
            }
        }
    }

    fun checkAuth(activity: Activity) {
        if (auth.currentUser == null) {
            Toast.makeText(activity.applicationContext, "Chưa login", Toast.LENGTH_SHORT).show()
            // User is not authenticated, redirect to login screen
            val intent = Intent(activity, LoginRegisterActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            activity.startActivity(intent)
            activity.finish() // Finish current activity to prevent user from going back
        }
    }


}