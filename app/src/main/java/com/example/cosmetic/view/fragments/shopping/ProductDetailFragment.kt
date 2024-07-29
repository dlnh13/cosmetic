package com.example.cosmetic.view.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cosmetic.R
import com.example.cosmetic.view.adapters.ColorsAdapter
import com.example.cosmetic.view.adapters.SizesAdapter
import com.example.cosmetic.view.adapters.ViewPager2Images
import com.example.cosmetic.data.CartProduct
import com.example.cosmetic.databinding.FragmentProductDetailBinding
import com.example.cosmetic.util.Resource
import com.example.cosmetic.util.hideBottomNavigationView
import com.example.cosmetic.viewmodel.DetailsViewModel
import com.example.cosmetic.viewmodel.NotificationViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ProductDetailFragment : Fragment() {
    private val args by navArgs<ProductDetailFragmentArgs>()
    private lateinit var binding: FragmentProductDetailBinding
    private val viewPagerAdapter by lazy { ViewPager2Images() }
    private val sizesAdapter by lazy { SizesAdapter() }
    private val colorsAdapter by lazy { ColorsAdapter() }
    private var selectedColor: String? = null
    private var selectedSize: String? = null
    private var isLiked = true
    private var productId: String = ""
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private var detailsViewModel : DetailsViewModel? = null
    private var notificationViewModel: NotificationViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            if (auth.currentUser != null)
                  detailsViewModel = viewModels<DetailsViewModel>().value
    }
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
        if (auth.currentUser != null) {
            notificationViewModel = viewModels<NotificationViewModel>().value
            lifecycleScope.launchWhenStarted {
                notificationViewModel!!.favoriteProducts.collectLatest { favoriteProducts ->
                    if (favoriteProducts.contains(product.id)) {
                        binding.icFavorite.setImageResource(R.drawable.ic_favorite_red)
                        binding.icFavorite.tag = "liked"
                    }
                }
            }
        }else {
           // binding.buttonAddToCart.isEnabled = false
            binding.icFavorite.visibility = View.GONE
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
            if(auth.currentUser == null) {
                Toast.makeText(context, "Chưa đăng nhập", Toast.LENGTH_SHORT).show()
            } else {
                if (product.colors != null && selectedColor == null) {
                    Toast.makeText(context, "Vui lòng chọn màu sắc", Toast.LENGTH_SHORT).show()
                } else if (product.sizes != null && selectedSize == null) {
                    Toast.makeText(context, "Vui lòng chọn kích thước", Toast.LENGTH_SHORT).show()
                } else {
                    detailsViewModel?.addUpdateProductInCart(
                        CartProduct(
                            product,
                            1,
                            selectedColor,
                            selectedSize
                        )
                    )
                }
            }
        }
        binding.apply {
            tvProductName.text = product.name
            tvProductPrice.text = "đ ${product.price}"
            tvProductDescription.text = product.description

            if (product.colors.isNullOrEmpty()) {
                tvProductColors.visibility = View.INVISIBLE
            }
            if (product.sizes.isNullOrEmpty()) {
                tvProductSize.visibility = View.INVISIBLE
            }
            lifecycleScope.launchWhenStarted {
                detailsViewModel?.addToCart?.collectLatest {
                    when (it) {
                        is Resource.Loading -> {
                            binding.buttonAddToCart.startAnimation()
                        }

                        is Resource.Success -> {
                            binding.buttonAddToCart.revertAnimation()
                            Toast.makeText(requireContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
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
            notificationViewModel!!.updateFavoriteProducts(productId, isLiked)

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