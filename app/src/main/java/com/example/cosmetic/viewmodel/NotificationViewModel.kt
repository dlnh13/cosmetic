package com.example.cosmetic.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cosmetic.data.CartProduct
import com.example.cosmetic.data.Comment
import com.example.cosmetic.data.Notification
import com.example.cosmetic.data.Product
import com.example.cosmetic.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
    ) : ViewModel() {
    private val _notifications  = MutableStateFlow<Resource<List<Notification>>>(Resource.Unspecified())
    val notifications = _notifications.asStateFlow()
    val favoriteProducts = mutableListOf<String>()
    private var notificationDocuments = emptyList<DocumentSnapshot>()
init {
    getUserFavoriteProductsId()
}
    private fun getUserFavoriteProductsId(){
        firestore.collection("user").document(auth.uid!!).collection("favoriteProducts").get()
            .addOnSuccessListener { documents ->
                val favoriteProducts = documents.map { it.id }
                Log.d("favoriteProducts", "${favoriteProducts.toString()}")
                fetchNotifications(favoriteProducts)
            }
            .addOnFailureListener { exception ->
                Log.w("FetchFavorites", "Error getting favorite products: ", exception)
            }
    }

    private fun fetchNotifications(favoriteProducts: List<String>) {
        viewModelScope.launch {
            _notifications.emit((Resource.Loading()))
        }
        if (favoriteProducts.isEmpty()) {
            viewModelScope.launch {
                _notifications.emit(Resource.Error("No favorite products found"))
            }
            return
        }
        firestore.collection("Notification").whereIn("productId", favoriteProducts)
            .addSnapshotListener{ value, error ->
                if (error != null)
                    viewModelScope.launch { _notifications.emit(Resource.Error(error.message.toString())) }
                else {
                    notificationDocuments = value!!.documents
                    val notifications = value.toObjects(Notification::class.java)
                    viewModelScope.launch { _notifications.emit(Resource.Success(notifications)) }
                }

            }


    }

}