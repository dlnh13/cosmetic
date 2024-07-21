package com.example.cosmetic.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cosmetic.data.CartProduct
import com.example.cosmetic.data.Comment
import com.example.cosmetic.data.Message
import com.example.cosmetic.data.Post
import com.example.cosmetic.data.User
import com.example.cosmetic.data.order.Order
import com.example.cosmetic.util.RegisterValidation
import com.example.cosmetic.util.Resource
import com.example.cosmetic.util.validateEmail
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
class CommentViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    val auth: FirebaseAuth
) : ViewModel() {
    private val _comment = MutableStateFlow<Resource<Comment>>(Resource.Unspecified())
    val comment = _comment.asStateFlow()
    private val _comments = MutableStateFlow<Resource<List<Comment>>>(Resource.Unspecified())
    val comments = _comments.asStateFlow()
    private var commentsDocuments = emptyList<DocumentSnapshot>()
    fun fetchComments(post: Post) {
        viewModelScope.launch {
            _comments.emit((Resource.Loading()))
        }
        firestore.collection("Posts").document(post.postId!!).collection("comments")
            .addSnapshotListener { value, error ->
                if (error != null || value == null)
                    viewModelScope.launch { _comments.emit(Resource.Error(error?.message.toString())) }
                else {
                    commentsDocuments = value.documents
                    val comments = value.toObjects(Comment::class.java)
                    val sortedComments = comments.sortedBy { it.time }
                    viewModelScope.launch { _comments.emit(Resource.Success(sortedComments)) }
                }
            }
    }

    fun saveComment(comment: Comment) {
        val areInputsValid = comment.content.trim().isNotEmpty()
        if (!areInputsValid) {
            viewModelScope.launch {
                _comment.emit(Resource.Error("Check your inputs"))
            }
            return
        }
        firestore.runTransaction { transaction ->
            val documentRef =
                firestore.collection("Posts").document(comment.postId).collection("comments")
                    .document(comment.commentId)
            transaction.set(documentRef, comment)
        }.addOnSuccessListener {
            viewModelScope.launch {
                _comment.emit(Resource.Success(comment))
            }
        }
            .addOnFailureListener {
                viewModelScope.launch {
                    _comment.emit(Resource.Error(it.message.toString()))
                }
            }

    }

}