package com.example.cosmetic.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cosmetic.data.order.Order
import com.example.cosmetic.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    val firestore: FirebaseFirestore,
    val auth: FirebaseAuth
) : ViewModel() {
    private val _order = MutableStateFlow<Resource<Order>>(Resource.Unspecified())
    val order = _order.asStateFlow()
    fun placeOrder(order: Order,usedCoin: Int) {
        viewModelScope.launch { _order.emit(Resource.Loading()) }
        firestore.runBatch { batch ->
            firestore.collection("user")
                .document(auth.uid!!)
                .collection("orders")
                .document(order.orderId.toString())
                .set(order)
            firestore.collection("orders").document(order.orderId.toString()).set(order)

            val selectedProducts = order.products.filter { it.selected }
            selectedProducts.forEach { cartProduct ->
                val productId =
                    cartProduct.product.id
                firestore.collection("user").document(auth.uid!!).collection("cart")
                    .whereEqualTo("product.id", productId)
                    .whereEqualTo("selectedColor", cartProduct.selectedColor)
                    .whereEqualTo("selectedSize", cartProduct.selectedSize)
                    .get()
                    .addOnSuccessListener {
                        for (document in it.documents) {
                            document.reference.delete()
                        }
                    }
            }
        }
            .addOnSuccessListener {
                addCoin(order, usedCoin )
                viewModelScope.launch {
                    _order.emit(Resource.Success(order))
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _order.emit(Resource.Error(it.message.toString()))
                }
            }
    }
    fun addCoin(order: Order,usedCoin: Int) {
        val coin = (order.totalPrice / 10000).toInt()
        val userDocRef = firestore.collection("user").document(auth.uid!!)
        userDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val currentCoins = documentSnapshot.getLong("coins") ?: 0 // Lấy điểm số hiện tại, nếu không tồn tại thì lấy giá trị mặc định là 0
                val newCoins = maxOf(currentCoins.toInt() + coin - usedCoin,0)

                val data = hashMapOf("coins" to newCoins)

                userDocRef.set(data, SetOptions.merge())

            } else {
                println("Không tìm thấy thông tin người dùng trong Firestore")
            }
        }.addOnFailureListener { e ->
            println("Lỗi khi truy vấn thông tin người dùng: $e")
        }
    }
    suspend fun getCoins(): Int {
        return try {
            val documentSnapshot = firestore.collection("user").document(auth.uid!!).get().await()
            val coins = documentSnapshot.getLong("coins")?.toInt() ?: 0
            coins
        } catch (e: Exception) {
            0
        }
    }
}



