package com.example.cosmetic.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cosmetic.data.Product
import com.example.cosmetic.util.Resource
import com.example.cosmetic.util.Uid.getUid
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainCategoryViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
): ViewModel(){

    private val _bestSellerProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val bestSellerProducts: StateFlow<Resource<List<Product>>> = _bestSellerProducts

    private val _specialProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val specialProducts: StateFlow<Resource<List<Product>>> = _specialProducts

    private val _discountProducts = MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val discountProducts: StateFlow<Resource<List<Product>>> = _discountProducts
    private val pagingInfo = PagingInfo()
    init {
        fetchBestSellerProducts()
        fetchSpecialProducts()
        fetchDiscountProducts()
    }
    fun fetchBestSellerProducts(){
        viewModelScope.launch {
            _bestSellerProducts.emit(Resource.Loading())
        }
        firestore.collection("Products")
            .whereEqualTo("category","Best Seller").get().addOnSuccessListener { result ->
                val bestSellerProductsList = result.toObjects(Product::class.java)
                viewModelScope.launch {
                    _bestSellerProducts.emit(Resource.Success(bestSellerProductsList))
                }
            }.addOnFailureListener{
                viewModelScope.launch {
                    _bestSellerProducts.emit(Resource.Error(it.message.toString()))
                }
            }
    }
    fun fetchSpecialProducts(){
        viewModelScope.launch {
            _specialProducts.emit((Resource.Loading()))
        }
        firestore.collection("Products")
            .whereEqualTo("category","Special Product").get().addOnSuccessListener { result ->
                val specialProductsList = result.toObjects(Product::class.java)
                viewModelScope.launch {
                    _specialProducts.emit(Resource.Success(specialProductsList))
                }
            }.addOnFailureListener{
                viewModelScope.launch {
                    _specialProducts.emit(Resource.Error(it.message.toString()))
                }
            }
    }
    fun fetchDiscountProducts() {
        if (!pagingInfo.isPagingEnd) {
            viewModelScope.launch {
                _discountProducts.emit((Resource.Loading()))
            }
            firestore.collection("Products")
                .whereEqualTo("category", "Discount Product")
                .limit(pagingInfo.discountProductsPage * 10).get().addOnSuccessListener { result ->
                    val discountProductsList = result.toObjects(Product::class.java)
                    pagingInfo.isPagingEnd = discountProducts == pagingInfo.oldDiscountProducts
                    pagingInfo.oldDiscountProducts = discountProductsList
                    viewModelScope.launch {
                        _discountProducts.emit(Resource.Success(discountProductsList))
                    }
                    pagingInfo.discountProductsPage++
                }.addOnFailureListener {
                    viewModelScope.launch {
                        _discountProducts.emit(Resource.Error(it.message.toString()))
                    }
                }
        }
    }
    fun addFavorite(product: Product){
        val documentReference = firestore.collection("Products")
        firestore.collection("Products").document(product.id!!).collection("likedByUsers")
            .document(getUid()).set(mapOf("previousPercentage" to product.offerPercentage))
        firestore.collection("user").document(auth.uid!!).collection("favoriteProducts").document(product.id).set(mapOf("previousPercentage" to product.offerPercentage))
    }
}
internal data class PagingInfo(
     var discountProductsPage : Long = 1,
     var oldDiscountProducts: List<Product> = emptyList(),
    var isPagingEnd : Boolean= false
)