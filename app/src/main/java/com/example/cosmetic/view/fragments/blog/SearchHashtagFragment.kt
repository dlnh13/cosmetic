package com.example.cosmetic.view.fragments.blog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cosmetic.R
import com.example.cosmetic.view.adapters.HashtagListAdapter
import com.example.cosmetic.view.adapters.PostsAdapter
import com.example.cosmetic.databinding.FragmentSearchHashtagBinding
import com.example.cosmetic.util.Resource
import com.example.cosmetic.viewmodel.BlogViewModel
import com.example.cosmetic.viewmodel.SearchHashtagViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class SearchHashtagFragment : Fragment(R.layout.fragment_search_hashtag) {
    private lateinit var binding: FragmentSearchHashtagBinding
    private val viewModel by viewModels<SearchHashtagViewModel>()
    private val blogViewModel by activityViewModels<BlogViewModel>()
    private lateinit var hashtagListAdapter: HashtagListAdapter
    private lateinit var searchPostsAdapter: PostsAdapter
    private val args by navArgs<SearchHashtagFragmentArgs>()
    private  var clickedHashtag: String? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clickedHashtag = args.hashtag
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchHashtagBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHashtagListRv()
        setupPostRv()
        if( clickedHashtag != null){
            Log.d("search", "$clickedHashtag")
            viewModel.getHashtagId(clickedHashtag!!)}
        lifecycleScope.launchWhenStarted {
            viewModel.hashtagList.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarChat.visibility = View.VISIBLE
                        binding.rvPosts.visibility = View.GONE
                        binding.rvHashtagList.visibility = View.VISIBLE
                        binding.linearEmpty.visibility = View.GONE
                    }

                    is Resource.Success -> {
                        if(it.data.isNullOrEmpty() ){
                            binding.rvPosts.visibility = View.GONE
                            binding.progressbarChat.visibility = View.GONE
                            binding.rvHashtagList.visibility = View.GONE
                            binding.linearEmpty.visibility = View.VISIBLE
                        } else {
                            binding.rvPosts.visibility = View.GONE
                            binding.progressbarChat.visibility = View.GONE
                            binding.rvHashtagList.visibility = View.VISIBLE
                            binding.linearEmpty.visibility = View.GONE
                            hashtagListAdapter.differ.submitList(it.data)
                        }
                    }

                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        binding.progressbarChat.visibility = View.GONE
                        binding.rvHashtagList.visibility = View.VISIBLE
                        binding.rvPosts.visibility = View.GONE
                        binding.linearEmpty.visibility = View.GONE

                    }

                    else -> Unit
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.postList.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarChat.visibility = View.VISIBLE
                        binding.rvPosts.visibility = View.GONE
                        binding.rvHashtagList.visibility = View.GONE
                    }

                    is Resource.Success -> {
                        binding.rvPosts.visibility = View.VISIBLE
                        binding.progressbarChat.visibility = View.GONE
                        binding.rvHashtagList.visibility = View.GONE
                        searchPostsAdapter.differ.submitList(it.data)
                    }

                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        binding.progressbarChat.visibility = View.GONE
                        binding.rvHashtagList.visibility = View.GONE
                        binding.rvPosts.visibility = View.VISIBLE
                    }

                    else -> Unit
                }
            }
        }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null && query.isNotEmpty()) {
                    binding.searchView.clearFocus()
                    viewModel.searchHashtag(query)
                    return true
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    binding.rvPosts.visibility = View.VISIBLE
                    binding.rvHashtagList.visibility = View.GONE
                    binding.linearEmpty.visibility = View.GONE
                } else {
                    binding.rvPosts.visibility = View.GONE
                    binding.linearEmpty.visibility = View.GONE
                    binding.rvHashtagList.visibility = View.GONE
                }
                return false
            }
        })
        hashtagListAdapter.onItemClick = {
            viewModel.getPosts(it)
        }
        searchPostsAdapter.onClick = { post, isLiked ->
            blogViewModel.likeClicked(post, isLiked)
        }
        searchPostsAdapter.onComment = {
            val action = SearchHashtagFragmentDirections.actionSearchHashtagFragmentToCommentFragment(it)
            findNavController().navigate(action)
        }
        binding.imageClose.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupHashtagListRv() {
        hashtagListAdapter = HashtagListAdapter()
        binding.rvHashtagList.apply {
            adapter = hashtagListAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }
    private fun setupPostRv() {
        searchPostsAdapter = PostsAdapter()
        binding.rvPosts.apply {
            adapter = searchPostsAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }
}