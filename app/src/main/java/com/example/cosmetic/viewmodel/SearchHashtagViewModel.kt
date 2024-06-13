package com.example.cosmetic.viewmodel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cosmetic.data.Hashtag
import com.example.cosmetic.data.Post
import com.example.cosmetic.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel

class SearchHashtagViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _hashtagList = MutableStateFlow<Resource<List<Hashtag>>>(Resource.Unspecified())
    val hashtagList = _hashtagList.asStateFlow()
    private val _postList = MutableStateFlow<Resource<List<Post>>>(Resource.Unspecified())
    val postList = _postList.asStateFlow()
    private val hashtagObjects = mutableListOf<Hashtag>()

    init {
        fetchListHashtag()
    }

    private fun fetchListHashtag() {
        firestore.collection("Hashtags").addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed", e)
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                val resultsList = snapshot.toObjects(Hashtag::class.java)
                hashtagObjects.clear() // Clear previous data
                hashtagObjects.addAll(resultsList)
            } else {
                Log.d(TAG, "No data found")
            }
        }
    }

    fun searchHashtag(searchString: String) {
        val normalizedInput = searchString.lowercase() // Normalize to lowercase
        val searchResultsList = hashtagObjects.filter {
            it.hashtag.contains(normalizedInput, ignoreCase = true)
        }
        viewModelScope.launch {
            _hashtagList.emit(Resource.Success(searchResultsList))
        }
    }

    fun getPosts(hashtag: Hashtag) {
        viewModelScope.launch {
            _postList.emit(Resource.Loading())
        }
        val postObjects = mutableListOf<Post>()
        val docRef = firestore.collection("Posts")

        // Use addSnapshotListener for real-time updates
        hashtag.postIds.forEach { postId ->
            docRef.document(postId).addSnapshotListener { snapshot, e ->
                if (e != null) {
                    viewModelScope.launch {
                        _postList.emit(Resource.Error(e.message.toString()))
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val post = snapshot.toObject(Post::class.java)
                    post?.let {
                        val index = postObjects.indexOfFirst { it.postId == post.postId }
                        if (index != -1) {
                            postObjects[index] = post // Update the existing post
                        } else {
                            postObjects.add(post) // Add new post
                        }
                        viewModelScope.launch {
                            _postList.emit(Resource.Success(postObjects.toList()))
                        }
                    }
                }
            }
        }
    }

    fun getHashtagId(hashtag: String) {
        firestore.collection("Hashtags").document(hashtag)
            .get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val hashtagObj = documentSnapshot.toObject(Hashtag::class.java)
                    getPosts(hashtagObj!!)
                }
            }.addOnFailureListener {
                println("Lỗi khi truy cập cơ sở dữ liệu: $it")
            }
    }
}


