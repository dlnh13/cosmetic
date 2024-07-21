package com.example.cosmetic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cosmetic.data.CartProduct
import com.example.cosmetic.data.Product
import com.example.cosmetic.firebase.FirebaseCommon
import com.example.cosmetic.helper.getProductPrice
import com.example.cosmetic.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommon
) : ViewModel() {

    private val _cartProducts =
        MutableStateFlow<Resource<List<CartProduct>>>(Resource.Unspecified())
    val cartProducts = _cartProducts.asStateFlow()

    val productsPrice = cartProducts.map {
        when (it) {
            is Resource.Success -> {
                calculatePrice(it.data!!)
            }
            else -> null
        }
    }
    private fun calculatePrice(data: List<CartProduct>): Float {
        return data.sumByDouble { cartProduct ->
            (cartProduct.product.offerPercentage.getProductPrice(cartProduct.product.price) * cartProduct.quantity).toDouble()
        }.toFloat()
    }

    private var cartProductDocuments = emptyList<DocumentSnapshot>()
    private val _deleteDialog = MutableSharedFlow<CartProduct>()
    val deleteDialog = _deleteDialog.asSharedFlow()
    fun deleteCartProduct(cartProduct: CartProduct) {
        val index = cartProducts.value.data?.indexOf(cartProduct)
        if (index != null && index != -1) {
            val documentId = cartProductDocuments[index].id
            firestore.collection("user").document(auth.uid!!).collection("cart")
                .document(documentId).delete()
        }
    }
    init {
        getCartProducts()
    }

    private fun getCartProducts() {
        viewModelScope.launch { _cartProducts.emit(Resource.Loading()) }
        firestore.collection("user").document(auth.uid!!).collection("cart")
            .addSnapshotListener { value, error ->
                if (error != null || value == null)
                    viewModelScope.launch { _cartProducts.emit(Resource.Error(error?.message.toString())) }
                else {
                    cartProductDocuments = value.documents
                    val cartProducts = value.toObjects(CartProduct::class.java)
                    viewModelScope.launch {  _cartProducts.emit(Resource.Success(cartProducts))  }
                }
            }
    }

    fun changeQuantity(
        cartProduct: CartProduct,
        quantityChange: FirebaseCommon.QuantityChange
    ) {
        val index = cartProducts.value.data?.indexOf(cartProduct)
        if (index != null && index != -1) {
            val documentId = cartProductDocuments[index].id
            when (quantityChange) {
                FirebaseCommon.QuantityChange.INCREASE -> {
                    viewModelScope.launch { _cartProducts.emit(Resource.Loading()) }
                    increaseQuantity(documentId)
                }

                FirebaseCommon.QuantityChange.DECREASE -> {
                    if (cartProduct.quantity == 1) {
                        viewModelScope.launch { _deleteDialog.emit(cartProduct) }
                        return
                    }
                    viewModelScope.launch { _cartProducts.emit(Resource.Loading()) }
                    decreaseQuantity(documentId)
                }
            }
        }
    }
    private fun decreaseQuantity(documentId: String) {
        firebaseCommon.decreaseQuantity(documentId) { result, exception ->
            if (exception != null)
                viewModelScope.launch { _cartProducts.emit(Resource.Error(exception.message.toString())) }
        }
    }

    private fun increaseQuantity(documentId: String) {
        firebaseCommon.increaseQuantity(documentId) { result, exception ->
            if (exception != null)
                viewModelScope.launch { _cartProducts.emit(Resource.Error(exception.message.toString())) }
        }
    }
}