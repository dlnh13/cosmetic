package com.example.cosmetic.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cosmetic.data.Product
import com.example.cosmetic.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class ListFavViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _favProducts =
        MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val favProducts = _favProducts.asStateFlow()

    init {
        getUserFavoriteProductsId()
    }

    fun getUserFavoriteProductsId() {
        firestore.collection("user").document(auth.uid!!).collection("favoriteProducts").get()
            .addOnSuccessListener { documents ->
                val favoriteProducts = documents.map { it.id }
                fetchFavProducts(favoriteProducts)
            }
            .addOnFailureListener { exception ->
                Log.w("FetchFavorites", "Error getting favorite products: ", exception)
            }
    }


    private fun fetchFavProducts(favoriteProducts: List<String>) {
        viewModelScope.launch {
            _favProducts.emit((Resource.Loading()))
        }
        if (favoriteProducts.isEmpty()) {
            viewModelScope.launch {
                _favProducts.emit(Resource.Error("don't have favorite products found"))
            }
            return
        }
        val productDocuments = mutableListOf<Product>()
        for (productId in favoriteProducts) {
            firestore.collection("Products").document(productId).get()
                .addOnSuccessListener { productSnapshot ->
                    productSnapshot.toObject(Product::class.java)?.let { product ->
                        productDocuments.add(product)
                    }
                    if (productDocuments.size == favoriteProducts.size) {
                        viewModelScope.launch {
                            _favProducts.emit(Resource.Success(productDocuments))
                        }
                    }
                }.addOnFailureListener { exception ->
                    viewModelScope.launch {
                        _favProducts.emit(Resource.Error("Error getting product $productId: ${exception.message}"))
                    }
                }
        }
    }

    fun updateFavoriteProducts(productId: String, liked: Boolean) {
        val docRef =
            firestore.collection("user").document(auth.uid!!).collection("favoriteProducts")
                .document(productId)
        if (liked) {
            docRef.set(mapOf("liked" to "true"))
        } else {
            docRef.delete()
        }
    }

}

