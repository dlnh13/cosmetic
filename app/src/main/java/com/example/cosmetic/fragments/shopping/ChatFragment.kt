package com.example.cosmetic.fragments.shopping

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cosmetic.adapters.MessagesAdapter
import com.example.cosmetic.databinding.FragmentChatBinding
import com.example.cosmetic.util.Resource
import com.example.cosmetic.util.Uid.getUid
import com.example.cosmetic.viewmodel.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ChatFragment:Fragment() {
    private lateinit var binding: FragmentChatBinding
    private val viewModel by viewModels<ChatViewModel>()
    private lateinit var messagesAdapter: MessagesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.messages.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarChat.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {

                        binding.progressbarChat.visibility = View.GONE
                        messagesAdapter.differ.submitList(it.data)
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        binding.progressbarChat.visibility = View.GONE
                    }
                    else -> Unit
                }
            }
        }
        binding.icClose.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.btnSend.setOnClickListener {
            val messageContent = binding.edMessage.text.toString().trim()
            if (messageContent.isNotEmpty()) {
                viewModel.sendMessage(
                    getUid(),
                    "00000",
                    "aaa",
                    binding.edMessage.text.toString()
                )
                binding.edMessage.setText("")
                binding.edMessage.requestFocus()
            } else {
                Toast.makeText(requireContext(), "Chưa nhập tin nhắn!", Toast.LENGTH_SHORT).show()

            }
        }
        setupMessageRv()
    }

    private fun setupMessageRv() {
        messagesAdapter = MessagesAdapter(getUid())
        binding.rvMessages.apply {
            adapter = messagesAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }


}