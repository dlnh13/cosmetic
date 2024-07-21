package com.example.cosmetic.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide.init
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


//    val selectedProducts = mutableListOf<CartProduct>()

//    MutableStateFlow<Resource<List<CartProduct>>>(Resource.Unspecified())
    //val selectedProducts = _selectedProducts.asStateFlow()
//    viewModelScope.launch {
//        cartProducts.collect { resource ->
//            Log.d("YourViewModel", "Cart products: ${resource.data!!.filter {!it.selected  }}")
//        }}
    val productsPrice = cartProducts.map {
        when (it) {
            is Resource.Success -> {
                val selectedProducts = it.data?.filter { it.selected }
                calculatePrice(selectedProducts!!)
            }
            else -> null
        }
    }
    fun test(data: List<CartProduct>): Float{
        return data.filter { it.selected }.sumByDouble {cartProduct ->
            (cartProduct.product.offerPercentage.getProductPrice(cartProduct.product.price) * cartProduct.quantity).toDouble()
        }.toFloat()
        }



//        fun totalPrice(data:List<CartProduct>){
//                CartProduct
//    }
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