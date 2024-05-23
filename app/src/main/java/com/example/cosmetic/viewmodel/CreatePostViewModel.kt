package com.example.cosmetic.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cosmetic.CosmeticApplication
import com.example.cosmetic.data.Post
import com.example.cosmetic.data.User
import com.example.cosmetic.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.checkerframework.checker.units.qual.s
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

    fun uploadPost(post: Post, imagesUri : List<Uri>) {
        val areInputsValid = post.caption!!.trim().isNotEmpty() && imagesUri != null
        if (!areInputsValid) {
            viewModelScope.launch {
                _post.emit(Resource.Error("Check your inputs"))
            }
            return
        }
        viewModelScope.launch {
            _post.emit(Resource.Loading())
        }
        saveImage(post, imagesUri)
    }
fun saveImage(post: Post,imagesUri : List<Uri>){
    var selectedImages = mutableListOf<String>()
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
}