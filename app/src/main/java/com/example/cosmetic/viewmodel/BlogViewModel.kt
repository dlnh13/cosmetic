package com.example.cosmetic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cosmetic.data.Post
import com.example.cosmetic.util.Resource
import com.example.cosmetic.util.Uid.getUid
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
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
    private var blogsDocuments = emptyList<DocumentSnapshot>()

    init {
        fetchPosts()
    }

    fun likeClicked(post: Post, isLiked: Boolean) {

//        val postCollection = firestore.collection("Posts")
//        val index = posts.value.data?.indexOf(post)
//        if (index != null && index != -1) {
//            val documentId = blogsDocuments[index].id
//            //if (isLiked) {
//            viewModelScope.launch { _posts.emit(Resource.Loading()) }
//            firestore.runTransaction { transition ->
//                val documentRef = postCollection.document(documentId)
//                val document = transition.get(documentRef)
//                val postObject = document.toObject(Post::class.java)
//                postObject?.let { Post ->
//                    val updatedLikers = post.likers?.toMutableList() ?: mutableListOf() // Danh sách likers cần được cập nhật
//                    if (isLiked) {
//                        updatedLikers.remove(getUid())
//                        val newLikesCount = updatedLikers.size
//                        val newPostObject = Post.copy(likes = newLikesCount, likers = updatedLikers)
//                        updatedLikers.remove(getUid())
//
//                        transition.set(documentRef, newPostObject)
//
//                    } else {
//
//                        updatedLikers.add(getUid())
//                        val newLikesCount = updatedLikers.size
//                        val newPostObject = Post.copy(likes = newLikesCount, likers = updatedLikers)
//                        transition.set(documentRef, newPostObject)
//
//                    }
//                }
//            }
        ///////////////////////////////////////

        val documentRef = firestore.collection("Posts").document(post.postId!!)
        documentRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val postFB = document.toObject(Post::class.java)
                    val updatedLikers = postFB!!.likers?.toMutableSet() ?: mutableSetOf()
                        // Danh sách likers cần được cập nhật
                    if (isLiked) {   //User has already liked the post
                        updatedLikers.remove(getUid())
                        documentRef.update(
                            "likers", updatedLikers.toList(),
                            "likes", updatedLikers.size
                        )
                    } else {
                        updatedLikers.add(getUid())
                        documentRef.update(
                            "likers", updatedLikers.toList(),
                            "likes", updatedLikers.size
                        )
                    }
                }
            }
//     else
//    {
//        documentRef.get()
//            .addOnSuccessListener { document ->
//                if (document != null && document.exists()) {
//                    val likes = document.getLong("likes")?.toInt() ?: 0
//                    documentRef.update(
//                        "likes", likes + 1,
//                        "likers", FieldValue.arrayUnion(getUid())
//                    )
//                    val post = document.toObject(Post::class.java)
//                    val likersList = post?.likers?.toList() ?: emptyList()
//                    val likeCount = likersList.size
//                    Log.d("likeCount", "$likeCount")
//                }
//            }
//    }


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
                    blogsDocuments = value!!.documents
                    val blogs = value.toObjects(Post::class.java)
                    viewModelScope.launch {
                        _posts.emit(Resource.Success(blogs))
                    }
                }
            }
    }

}