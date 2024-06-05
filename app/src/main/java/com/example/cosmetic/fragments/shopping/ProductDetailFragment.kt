package com.example.cosmetic.fragments.shopping

import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cosmetic.R
import com.example.cosmetic.adapters.ColorsAdapter
import com.example.cosmetic.adapters.SizesAdapter
import com.example.cosmetic.adapters.ViewPager2Images
import com.example.cosmetic.data.CartProduct
import com.example.cosmetic.databinding.FragmentProductDetailBinding
import com.example.cosmetic.util.Resource
import com.example.cosmetic.util.hideBottomNavigationView
import com.example.cosmetic.viewmodel.DetailsViewModel
import com.example.cosmetic.viewmodel.NotificationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductDetailFragment : Fragment() {
    private val args by navArgs<ProductDetailFragmentArgs>()
    private lateinit var binding: FragmentProductDetailBinding
    private val viewPagerAdapter by lazy { ViewPager2Images() }
    private val sizesAdapter by lazy { SizesAdapter() }
    private val colorsAdapter by lazy { ColorsAdapter() }
    private var selectedColor: String? = null
    private var selectedSize: String? = null
    private val viewModel by viewModels<DetailsViewModel>()
    private val notificationViewModel by viewModels<NotificationViewModel>()
    private var isLiked = true
    private var productId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        hideBottomNavigationView()
        binding = FragmentProductDetailBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val product = args.product
        productId = product.id
        lifecycleScope.launchWhenStarted {
            notificationViewModel.favoriteProducts.collectLatest { favoriteProducts ->
                if (favoriteProducts.contains(product.id)) {
                    binding.icFavorite.setImageResource(R.drawable.ic_favorite_red)
                    binding.icFavorite.tag = "liked"
                }
            }
        }
        setupSizesRv()
        setupColorsRv()
        setupViewpager()
        binding.imageClose.setOnClickListener {
            findNavController().navigateUp()
        }
        sizesAdapter.onItemClick = {
            selectedSize = it
        }
        colorsAdapter.onItemClick = {
            selectedColor = it
        }
        binding.buttonAddToCart.setOnClickListener {
            viewModel.addUpdateProductInCart(CartProduct(product, 1, selectedColor, selectedSize))
        }
        binding.apply {
            tvProductName.text = product.name
            tvProductPrice.text = "$ ${product.price}"
            tvProductDescription.text = product.description

            if (product.colors.isNullOrEmpty()) {
                tvProductColors.visibility = View.INVISIBLE
            }
            if (product.sizes.isNullOrEmpty()) {
                tvProductSize.visibility = View.INVISIBLE
            }
            lifecycleScope.launchWhenStarted {
                viewModel.addToCart.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            binding.buttonAddToCart.startAnimation()
                        }

                        is Resource.Success -> {
                            binding.buttonAddToCart.revertAnimation()
                            binding.buttonAddToCart.setBackgroundColor(resources.getColor(R.color.black))
                        }

                        is Resource.Error -> {
                            binding.buttonAddToCart.stopAnimation()
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }

                        else -> Unit
                    }
                }
            }

        }
        viewPagerAdapter.differ.submitList(product.images)
        product.colors?.let { colorsAdapter.differ.submitList(it) }
        product.sizes?.let { sizesAdapter.differ.submitList(it) }
        binding.icFavorite.setOnClickListener {
            if (binding.icFavorite.tag == "liked") {
                binding.icFavorite.tag = "notlike"
                binding.icFavorite.setImageResource(R.drawable.ic_favorite)
                isLiked = false

            } else {
                binding.icFavorite.tag = "liked"
                binding.icFavorite.setImageResource(R.drawable.ic_favorite_red)

                isLiked = true
            }
            notificationViewModel.updateFavoriteProducts(productId, isLiked)

        }
    }

    private fun setupViewpager() {
        binding.apply {
            viewPagerProductImages.adapter = viewPagerAdapter
        }
    }

    private fun setupColorsRv() {
        binding.rvColors.apply {
            adapter = colorsAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupSizesRv() {
        binding.rvSizes.apply {
            adapter = sizesAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }
}