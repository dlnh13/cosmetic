package com.example.cosmetic.fragments.shopping

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.material3.Snackbar
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cosmetic.R
import com.example.cosmetic.adapters.AddressAdapter
import com.example.cosmetic.adapters.BillingProductsAdapter
import com.example.cosmetic.data.Address
import com.example.cosmetic.data.CartProduct
import com.example.cosmetic.data.order.Order
import com.example.cosmetic.data.order.OrderStatus
import com.example.cosmetic.databinding.FragmentBillingBinding
import com.example.cosmetic.util.Resource
import com.example.cosmetic.viewmodel.BillingViewModel
import com.example.cosmetic.viewmodel.OrderViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BillingFragment : Fragment() {
    private lateinit var binding: FragmentBillingBinding
    private val addressAdapter by lazy { AddressAdapter() }
    private val billingProductsAdapter by lazy { BillingProductsAdapter() }
    private val billingViewModel by viewModels<BillingViewModel>()
    private val args by navArgs<BillingFragmentArgs>()
    private var products = emptyList<CartProduct>()
    private var totalPrice = 0f
    private var selectedAddress: Address? = null
    private var coins = 0
    private val perCoin = 1000
    private var discountedPrice = 0f
    private var usedCoins = 0

    private val orderViewModel by viewModels<OrderViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        products = args.products.toList()
        totalPrice = args.totalPrice

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentBillingBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBillingProductsRv()
        setupAddressRv()

        val pinkColor = ColorStateList.valueOf(Color.parseColor("#FFC0CB"))
        binding.switchCoin.thumbTintList = pinkColor
        binding.icCoin.setColorFilter(ContextCompat.getColor(requireContext(), R.color.yellow));

        lifecycleScope.launch {
            coins = orderViewModel.getCoins()
            binding.tvCoins.text = "Bạn đang có ${coins} xu"
            binding.tvDiscountMoney.text = "[- ₫ ${coins * 1000}]"
        }
        if (!args.payment) {
            binding.apply {
                buttonPlaceOrder.visibility = View.INVISIBLE
                totalBoxContainer.visibility = View.INVISIBLE
                middleLine.visibility = View.INVISIBLE
                bottomLine.visibility = View.INVISIBLE
                linearCoins.visibility = View.INVISIBLE
            }
        }
        binding.imageAddAddress.setOnClickListener {
            findNavController().navigate(R.id.action_billingFragment_to_addressFragment)
        }
        lifecycleScope.launchWhenStarted {
            billingViewModel.address.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarAddress.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        addressAdapter.differ.submitList(it.data)
                        binding.progressbarAddress.visibility = View.GONE
                    }

                    is Resource.Error -> {
                        binding.progressbarAddress.visibility = View.GONE
                        Toast.makeText(requireContext(), "Error ${it.message}", Toast.LENGTH_SHORT)
                            .show()
                    }

                    else -> Unit
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            orderViewModel.order.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.buttonPlaceOrder.startAnimation()
                    }

                    is Resource.Success -> {
                        binding.buttonPlaceOrder.revertAnimation()
                        findNavController().navigateUp()
                        Snackbar.make(requireView(), "Đã đặt hàng!", Snackbar.LENGTH_LONG).show()
                    }

                    is Resource.Error -> {
                        binding.progressbarAddress.visibility = View.GONE
                        Toast.makeText(requireContext(), "Error ${it.message}", Toast.LENGTH_SHORT)
                            .show()
                    }

                    else -> Unit
                }
            }
        }
        billingProductsAdapter.differ.submitList(products)


        binding.tvTotalPrice.text = "$ $totalPrice"

        addressAdapter.onClick = {
            selectedAddress = it
            if (!args.payment) {
                val b = Bundle().apply { putParcelable("address", selectedAddress) }
                findNavController().navigate(R.id.action_billingFragment_to_addressFragment, b)
            }
        }
        binding.buttonPlaceOrder.setOnClickListener {
            if (selectedAddress == null) {
                Toast.makeText(
                    requireContext(),
                    "Bạn chưa chọn địa chỉ giao hàng!",
                    Toast.LENGTH_SHORT
                )
                    .show()
                return@setOnClickListener
            }
            showOrderConfirmationDialog()
        }
        binding.switchCoin.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                discountedPrice = totalPrice - perCoin * coins
                if(discountedPrice < 0){
                    discountedPrice = 0f
                    usedCoins = (totalPrice / perCoin).toInt()
                } else {
                    usedCoins = coins
                }
                binding.tvTotalPrice.text = "₫ ${discountedPrice}"
            } else {
                usedCoins = 0
                binding.tvTotalPrice.text = "₫ $totalPrice"
            }
        }
    }

    private fun showOrderConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(requireContext()).apply {
            setTitle("Đặt hàng")
            setMessage("Xác nhận đặt đơn hàng?")
            setNegativeButton("Không") { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton("Đặt hàng") { dialog, _ ->
                val finalPrice = if (binding.switchCoin.isChecked) discountedPrice else totalPrice
                val order = Order(
                    OrderStatus.Ordered.status,
                    finalPrice,
                    products,
                    selectedAddress!!
                )
                orderViewModel.placeOrder(order,usedCoins)
                dialog.dismiss()
                val coinsReceived = (order.totalPrice / 10000).toInt()
                showCoinDialog("Coin", "Bạn đã nhận được $coinsReceived xu")

            }
        }
        alertDialog.create()
        alertDialog.show()
    }

    private fun setupAddressRv() {
        binding.rvAddress.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = addressAdapter
        }
    }

    private fun setupBillingProductsRv() {
        binding.rvProducts.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = billingProductsAdapter
        }
    }
    private fun showCoinDialog(title: String, message: String) {
        val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_coins)
        icon?.let {
            DrawableCompat.setTint(it, ContextCompat.getColor(requireContext(), R.color.yellow))
        }
        AlertDialog.Builder(requireContext()).apply {
            setTitle(title)
            setMessage(message)
            setIcon(icon)
            setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        }.create().show()
    }
}
