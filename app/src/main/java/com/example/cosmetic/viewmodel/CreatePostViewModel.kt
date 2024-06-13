package com.example.cosmetic.viewmodel

import android.app.Application
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cosmetic.CosmeticApplication
import com.example.cosmetic.data.Post
import com.example.cosmetic.data.User
import com.example.cosmetic.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: StorageReference,
    app: Application
) : AndroidViewModel(app) {
    private val _post = MutableStateFlow<Resource<Post>>(Resource.Unspecified())
    val post = _post.asStateFlow()

    fun uploadPost(post: Post, imagesUri: List<Uri>) {
        val areInputsValid = post.caption!!.trim().isNotEmpty() && !imagesUri.isNullOrEmpty()
        if (!areInputsValid) {
            viewModelScope.launch {
                _post.emit(Resource.Error("Check your inputs"))
            }
            return
        }
        viewModelScope.launch {
            _post.emit(Resource.Loading())
        }
        val listHashtags = extractHashtags(post.caption)
        val newPost: Post = post.copy(hashtag = listHashtags)
        saveImage(newPost, imagesUri)
        saveHashtag(listHashtags, post.postId)

    }

    private fun saveImage(post: Post, imagesUri: List<Uri>) {
        val selectedImages = mutableListOf<String>()
        viewModelScope.launch {
            try {
                imagesUri.forEach {
                    val imageBitmap = MediaStore.Images.Media.getBitmap(
                        getApplication<CosmeticApplication>().contentResolver,
                        it
                    )
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 96, byteArrayOutputStream)
                    val imageByteArray = byteArrayOutputStream.toByteArray()
                    val imageDirectory =
                        storage.child("postsImages/${post.postId}/${UUID.randomUUID()}")
                    val result = imageDirectory.putBytes(imageByteArray).await()
                    val imageUrl = result.storage.downloadUrl.await().toString()
                    selectedImages.add(imageUrl)
                }
                savePost(post.copy(imagePost = selectedImages))
            } catch (e: Exception) {
                viewModelScope.launch {
                    _post.emit(Resource.Error(e.message.toString()))
                }
            }
        }

    }

    private fun savePost(post: Post) {
        firestore.runTransaction { transaction ->
            val documentRef = firestore.collection("user").document(auth.uid!!)
            val currentUser = transaction.get(documentRef).toObject(User::class.java)
            val post = post.copy(profileImage = currentUser?.imagePath)
            firestore.collection("Posts").document(post.postId).set(post)
        }.addOnSuccessListener {
            viewModelScope.launch {
                _post.emit(Resource.Success(post))
            }
        }
            .addOnFailureListener {
                viewModelScope.launch {
                    _post.emit(Resource.Error(it.message.toString()))
                }
            }
    }

    private fun extractHashtags(caption: String): List<String> {
        val hashtagRegex = Regex("#(\\w+)")
        return hashtagRegex.findAll(caption)
            .map { it.groupValues[1] }
            .toList()
    }

    private fun saveHashtag(hashtagList: List<String>, postId: String) {
        viewModelScope.launch {
            try {
                hashtagList.forEach { hashtag ->
                    val hashtagDocRef = firestore.collection("Hashtags").document(hashtag)
                    val document = hashtagDocRef.get().await()
                    val existingPostIds =
                        document.data?.get("postIds") as? MutableList<String> ?: mutableListOf()
                    if (!existingPostIds.contains(postId)) {
                        existingPostIds.add(postId)
                    }
                    val hashtagData = mapOf(
                        "hashtag" to hashtag,
                        "postIds" to existingPostIds
                    )
                    hashtagDocRef.set(hashtagData).await()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding hashtag to Firestore.", e)
            }
        }
    }
}