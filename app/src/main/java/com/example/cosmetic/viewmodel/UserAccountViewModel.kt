package com.example.cosmetic.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cosmetic.CosmeticApplication
import com.example.cosmetic.data.User
import com.example.cosmetic.util.RegisterValidation
import com.example.cosmetic.util.Resource
import com.example.cosmetic.util.validateEmail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class UserAccountViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: StorageReference,
    app: Application
) : AndroidViewModel(app) {
    private val _user = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val user = _user.asStateFlow()
    private val _updateInfo = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val updateInfo = _updateInfo.asStateFlow()

    init {
        getUser()
    }

    fun getUser() {
        viewModelScope.launch {
            _user.emit(Resource.Loading())
        }
        firestore.collection("user").document(auth.uid!!)
            .get().addOnSuccessListener {
                val user = it.toObject(User::class.java)
                user?.let {
                    viewModelScope.launch {
                        _user.emit(Resource.Success(it))
                    }
                }

            }.addOnFailureListener {
                viewModelScope.launch {
                    _user.emit(Resource.Error(it.message.toString()))
                }

            }
    }
    fun updateInfo(user: User, imageUri : Uri?) {
        val areInputsValid = validateEmail(user.email) is RegisterValidation.Success
                && user.firstName.trim().isNotEmpty()
                && user.lastName.trim().isNotEmpty()

        if (!areInputsValid) {
            viewModelScope.launch {
                _user.emit(Resource.Error("Check your inputs"))
            }
            return
        }
        viewModelScope.launch {
            _updateInfo.emit(Resource.Loading())
        }

        if(imageUri == null){
            saveUserInfo(user,true)
        } else {
            saveUserInfoWithNewImg(user,imageUri)
        }

    }

    private fun saveUserInfoWithNewImg(user: User, imageUri: Uri) {
        viewModelScope.launch {
            try {
                val imageBitmap = MediaStore.Images.Media.getBitmap(
                    getApplication<CosmeticApplication>().contentResolver,
                    imageUri
                )
                val byteArrayOutputStream = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 96, byteArrayOutputStream)
                val imageByteArray = byteArrayOutputStream.toByteArray()
                val imageDirectory =
                    storage.child("profileImages/${auth.uid}/${UUID.randomUUID()}")
                val result = imageDirectory.putBytes(imageByteArray).await()
                val imageUrl = result.storage.downloadUrl.await().toString()
                saveUserInfo(user.copy(imagePath = imageUrl), false)
            } catch (e: Exception) {
                viewModelScope.launch {
                    _user.emit(Resource.Error(e.message.toString()))
                }
            }
        }
    }

    private fun saveUserInfo(user: User, shouldRetrivedOldImg: Boolean) {
        firestore.runTransaction { transaction ->
val documentRef = firestore.collection("user").document(auth.uid!!)
            if(shouldRetrivedOldImg){
                val currentUser = transaction.get(documentRef).toObject(User::class.java)
                val newUser = user.copy(imagePath = currentUser?.imagePath?:"")
                transaction.set(documentRef,newUser)
            }else{
                transaction.set(documentRef,user)
            }
        }.addOnSuccessListener {
            viewModelScope.launch {
                _updateInfo.emit(Resource.Success(user))
            }
        }
            .addOnFailureListener {
                viewModelScope.launch {
                    _updateInfo.emit(Resource.Error(it.message.toString()))
                }
            }


    }
}




