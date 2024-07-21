package com.example.cosmetic.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cosmetic.data.order.Order
import com.example.cosmetic.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    val firestore: FirebaseFirestore,
    val auth: FirebaseAuth
) : ViewModel() {
    private val _order = MutableStateFlow<Resource<Order>>(Resource.Unspecified())
    val order = _order.asStateFlow()
    fun placeOrder(order: Order) {
        viewModelScope.launch { _order.emit(Resource.Loading()) }
        firestore.runBatch { batch ->
//            TODO: Add orders into user-orders collection
//            TODO:Add orders into orders collection
//            TODO:Delete products from user-cart collection
            firestore.collection("user")
                .document(auth.uid!!)
                .collection("orders")
                .document()
                .set(order)
            firestore.collection("orders").document().set(order)

            val selectedProducts = order.products.filter { it.selected }
            selectedProducts.forEach { cartProduct ->
                val productId =
                    cartProduct.product.id // Giả sử ID của sản phẩm được lưu trữ trong thuộc tính id của cartProduct
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
}


