package com.example.cosmetic.view.fragments.settings

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
import androidx.recyclerview.widget.RecyclerView
import com.example.cosmetic.R
import com.example.cosmetic.view.adapters.FavoriteProductsAdapter
import com.example.cosmetic.databinding.FragmentListFavoriteBinding
import com.example.cosmetic.util.Resource
import com.example.cosmetic.util.VerticalItemDecoration
import com.example.cosmetic.util.hideBottomNavigationView
import com.example.cosmetic.viewmodel.ListFavViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ListFavFragment : Fragment(){
    private lateinit var binding: FragmentListFavoriteBinding
    private val favoriteProductsAdapter by lazy { FavoriteProductsAdapter() }
    private val viewModel by viewModels<ListFavViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        hideBottomNavigationView()
        binding = FragmentListFavoriteBinding.inflate(inflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFavProductsRv()
        lifecycleScope.launchWhenStarted {
            viewModel.favProducts.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarNoti.visibility = View.VISIBLE
                        binding.rvFavProducts.visibility = View.GONE
                        binding.linearEmpty.visibility = View.GONE
                    }

                    is Resource.Success -> {
                        binding.progressbarNoti.visibility = View.GONE
                        if (it.data!!.isEmpty()) {
                            binding.linearEmpty.visibility = View.VISIBLE
                            binding.rvFavProducts.visibility = View.GONE
                        } else {
                            binding.linearEmpty.visibility = View.GONE
                            binding.rvFavProducts.visibility = View.VISIBLE
                            favoriteProductsAdapter.differ.submitList(it.data)
                        }
                    }

                    is Resource.Error -> {
                        binding.progressbarNoti.visibility = View.GONE
                        if (it.message == "don't have favorite products found") {
                            binding.linearEmpty.visibility = View.VISIBLE
                            binding.rvFavProducts.visibility = View.GONE
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Error ${it.message}",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }

                    else -> Unit
                }
            }
        }
        favoriteProductsAdapter.onClick = {
            viewModel.getUserFavoriteProductsId()
            val b = Bundle().apply {
                putParcelable("product", it)
            }
            findNavController().navigate(R.id.action_listFavFragment_to_productDetailFragment, b)
        }
        favoriteProductsAdapter.onFavoriteClick = { product, isLiked ->
            viewModel.updateFavoriteProducts(product.id, isLiked)
        }
    }

    private fun setupFavProductsRv() {
        binding.rvFavProducts.apply {
            adapter = favoriteProductsAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            addItemDecoration(VerticalItemDecoration())
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getUserFavoriteProductsId()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        favoriteProductsAdapter.clear()
    }
    }