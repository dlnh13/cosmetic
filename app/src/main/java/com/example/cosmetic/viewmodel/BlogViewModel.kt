package com.example.cosmetic.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cosmetic.data.Post
import com.example.cosmetic.util.Resource
import com.example.cosmetic.util.Uid.getUid
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlogViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val _posts = MutableStateFlow<Resource<List<Post>>>(Resource.Unspecified())
    val posts = _posts.asStateFlow()

    init {
        fetchPosts()
    }

    fun likeClicked(post: Post, isLiked: Boolean) {

        val documentRef = firestore.collection("Posts").document(post.postId!!)
        if (isLiked) {
            // User has already liked the post
            documentRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val likes = document.getLong("likes")?.toInt() ?: 0
                        documentRef.update(
                            "likes", likes - 1,
                            "likers", FieldValue.arrayRemove(getUid())
                        )

                    }
                }
        } else {
            documentRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val likes = document.getLong("likes")?.toInt() ?: 0
                        documentRef.update(
                            "likes", likes + 1,
                            "likers", FieldValue.arrayUnion(getUid())
                        )
                    }
                }
        }

    }


    fun fetchPosts() {
        viewModelScope.launch {
            _posts.emit((Resource.Loading()))
        }

        firestore.collection("Posts").orderBy("time", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    viewModelScope.launch { _posts.emit(Resource.Error(error?.message.toString())) }
                    return@addSnapshotListener
                } else {
                    val postsList = mutableListOf<Post>()
                    value!!.documents.forEach { document ->
                        val postModal = document.toObject(Post::class.java)
                        postModal.let {
                            postsList.add(it!!)
                        }
                    }
                    viewModelScope.launch {
                        _posts.emit(Resource.Success(postsList))
                    }
                }
            }
    }

}