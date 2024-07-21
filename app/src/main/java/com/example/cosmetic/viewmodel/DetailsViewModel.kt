package com.example.cosmetic.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.cosmetic.data.CartProduct
import com.example.cosmetic.firebase.FirebaseCommon
import com.example.cosmetic.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    val auth: FirebaseAuth,
    private val firebaseCommon: FirebaseCommon

) : ViewModel() {

    private val _addToCart = MutableStateFlow<Resource<CartProduct>>(Resource.Unspecified())
    val addToCart = _addToCart.asStateFlow()

    fun addUpdateProductInCart(cartProduct: CartProduct) {
        Log.d("test","test111")
        Log.d("test","${auth.uid!!}")

        viewModelScope.launch { _addToCart.emit(Resource.Loading()) }
        val del = firestore.collection("user").document(auth.uid!!).collection("cart")

        firestore.collection("user").document(auth.uid!!).collection("cart")
            .whereEqualTo("product.id", cartProduct.product.id).get()
            .addOnSuccessListener {
                Log.d("test","test333")

                it.documents.let {
                    Log.d("test","test222")
                    if (it.isEmpty()) { // add new product
                        addNewProduct(cartProduct)
                    } else {
                        val existingProduct = it.first().toObject(CartProduct::class.java)
                        val documentId = it.first().id
                        if (
                            existingProduct?.product?.id == cartProduct.product.id &&
                            existingProduct?.selectedColor == cartProduct.selectedColor &&
                            existingProduct?.selectedSize == cartProduct.selectedSize
                        ) { // increase the quantity
                            increaseQuantity(documentId, cartProduct)
                        } else { // add new product
                            addNewProduct(cartProduct)
                        }
                    }
                }
            }.addOnFailureListener {
                Log.d("test","test444")

                viewModelScope.launch { _addToCart.emit(Resource.Error(it.message.toString())) }

            }
    }

    private fun addNewProduct(cartProduct: CartProduct) {
        firebaseCommon.addProductToCart(cartProduct) { addedProduct, e ->
            viewModelScope.launch {
                if (e == null)
                    _addToCart.emit(Resource.Success(addedProduct!!))
                else
                    _addToCart.emit(Resource.Error(e.message.toString()))

            }
        }
    }

    private fun increaseQuantity(documentId: String, cartProduct: CartProduct) {
        firebaseCommon.increaseQuantity(documentId) { _, e ->
            viewModelScope.launch {
                if (e == null)
                    _addToCart.emit(Resource.Success(cartProduct))
                else
                    _addToCart.emit(Resource.Error(e.message.toString()))

            }
        }
    }
}