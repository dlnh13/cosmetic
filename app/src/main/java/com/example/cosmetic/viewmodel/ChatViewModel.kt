package com.example.cosmetic.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cosmetic.CosmeticApplication
import com.example.cosmetic.data.Message
import com.example.cosmetic.data.User
import com.example.cosmetic.util.Resource
import com.example.cosmetic.util.Uid.getUserName
import com.example.cosmetic.util.getTime
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    app: Application
) : AndroidViewModel(app) {
    private val _messages = MutableStateFlow<Resource<List<Message>>>(Resource.Unspecified())
    val messages = _messages.asStateFlow()
    private val _user = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val user = _user.asStateFlow()
    var username = ""

    init {
        getUserNameFun()
        fetchMessage()
    }

    fun getUserNameFun() {
        viewModelScope.launch {
            username = getUserName(auth.uid!!)
        }
//        firestore.collection("user").document(auth.uid!!)
//            .addSnapshotListener { value, error ->
//                if (error != null) {
//                    viewModelScope.launch {
//                        _user.emit(Resource.Error(error.message.toString()))
//                    }
//                } else {
//                    val user = value?.toObject(User::class.java)
//                    user?.let {
//                        username = user.firstName
//                        viewModelScope.launch {
//                            _user.emit(Resource.Success(it))
//                        }
//                    }
//
//                }
//            }
    }

    fun sendMessage(
        sender: String,
        receiver: String,
        message: String
    ) {
     //   val context = getApplication<CosmeticApplication>().applicationContext
        val hashMap = hashMapOf<String, Any>(
            "sender" to sender,
            "receiver" to receiver,
            "message" to message,
            "time" to getTime()
        )
//        val uniqueId = listOf(sender, receiver).sorted()
//        uniqueId.joinToString(separator = "")

//        val friendnamesplit = friendname.split("\\s".toRegex())[0]
//        val mysharedPrefs = SharedPrefs(context)
//        mysharedPrefs.setValue("friendid", receiver)
//        mysharedPrefs.setValue("chatroomid", uniqueId.toString())
//        mysharedPrefs.setValue("friendname", friendnamesplit)
        firestore.collection("Messages").document(auth.uid!!).collection("chats")
            .document(getTime().toString()).set(hashMap).addOnCompleteListener { task ->

                val setHashap = hashMapOf<String, Any>(
                    "sender" to sender,
                    "time" to getTime(),
                    "message" to message,
                    "name" to username
                )
                firestore.collection("Conversations").document("${auth.uid}")
                    .set(setHashap)
            }
//        firestore.collection("Conversations").document(receiver)
//            .update(
//                "message",
//                messages.value!!,
//                "time",
//                getTime(),
//                "person",
//                username
//            )
    }

    fun fetchMessage() {
        viewModelScope.launch {
            _messages.emit((Resource.Loading()))
        }
//        val uniqueId = listOf(auth.uid!!, friendid).sorted()
//        uniqueId.joinToString(separator = "")

        firestore.collection("Messages").document(auth.uid!!).collection("chats")
            .orderBy("time", Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    viewModelScope.launch { _messages.emit(Resource.Error(error?.message.toString())) }
                    return@addSnapshotListener
                }
                else {
                    val messagesList = mutableListOf<Message>()
                    value!!.documents.forEach { document ->
                        val messageModel = document.toObject(Message::class.java)

                        if (messageModel!!.sender.equals(auth.uid!!) && messageModel.receiver.equals(
                                "00000") ||
                            messageModel.sender.equals("00000") && messageModel.receiver.equals(
                                auth.uid!!
                            )
                        ) {
                            messageModel.let {
                                messagesList.add(it!!)
                            }
                        }
                    }
                    viewModelScope.launch {
                        _messages.emit(Resource.Success(messagesList))
                    }
                }
            }

    }
}
