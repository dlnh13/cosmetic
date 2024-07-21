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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _searchResults =
        MutableStateFlow<Resource<List<Product>>>(Resource.Unspecified())
    val searchResults: StateFlow<Resource<List<Product>>> = _searchResults

    fun search(input: String) {
        viewModelScope.launch {
            _searchResults.emit(Resource.Loading())
        }

        val normalizedInput = input.lowercase() // Normalize to lowercase

        firestore.collection("Products")
            .get()
            .addOnSuccessListener { result ->
                val resultsList = result.toObjects(Product::class.java)
                val searchResultsList = resultsList.filter {
                    it.name.contains(normalizedInput, ignoreCase = true)
                }
                viewModelScope.launch {
                    _searchResults.emit(Resource.Success(searchResultsList))
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _searchResults.emit(Resource.Error(it.message.toString()))
                }
            }
    }
}