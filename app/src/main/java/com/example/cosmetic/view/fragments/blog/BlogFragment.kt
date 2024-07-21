package com.example.cosmetic.view.fragments.blog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cosmetic.R
import com.example.cosmetic.view.adapters.PostsAdapter
import com.example.cosmetic.databinding.FragmentBlogBinding
import com.example.cosmetic.util.Resource
import com.example.cosmetic.viewmodel.BlogViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class BlogFragment : Fragment(R.layout.fragment_blog) {
    private lateinit var binding: FragmentBlogBinding
    private val viewModel by viewModels<BlogViewModel>()
    private lateinit var postsAdapter: PostsAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBlogBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPostRv()
        lifecycleScope.launchWhenStarted {
            viewModel.posts.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarChat.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        binding.progressbarChat.visibility = View.GONE
                        postsAdapter.differ.submitList(it.data)
                    }

                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        binding.progressbarChat.visibility = View.GONE
                    }

                    else -> Unit
                }
            }
        }
        binding.btnAdd.setOnClickListener {
            findNavController().navigate(R.id.action_blogFragment_to_createPostFragment)
        }
        postsAdapter.onClick = { post, isLiked ->
            viewModel.likeClicked(post, isLiked)
        }

        postsAdapter.onComment = {
            val action = BlogFragmentDirections.actionBlogFragmentToCommentFragment(it)
            findNavController().navigate(action)
        }
        postsAdapter.onHashtagClick = {hashtag ->
            val action = BlogFragmentDirections.actionBlogFragmentToSearchHashtagFragment(hashtag)
            findNavController().navigate(action)
        }
        binding.btnSearch.setOnClickListener {
            val action = BlogFragmentDirections.actionBlogFragmentToSearchHashtagFragment(null)
            findNavController().navigate(action)
        }
    }

    private fun setupPostRv() {
        postsAdapter = PostsAdapter()
        binding.rvPosts.apply {
            adapter = postsAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        postsAdapter.clear()
    }
}