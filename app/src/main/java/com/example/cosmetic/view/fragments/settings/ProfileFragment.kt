package com.example.cosmetic.view.fragments.settings

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.cosmetic.R
import com.example.cosmetic.view.activities.LoginRegisterActivity
import com.example.cosmetic.data.User
import com.example.cosmetic.databinding.FragmentProfileBinding
import com.example.cosmetic.util.Resource
import com.example.cosmetic.util.showBottomNavigationView
import com.example.cosmetic.viewmodel.OrderViewModel
import com.example.cosmetic.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private lateinit var binding: FragmentProfileBinding
    val viewModel by viewModels<ProfileViewModel>()
    private val orderViewModel by viewModels<OrderViewModel>()
    private var coins = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenStarted {
            coins = orderViewModel.getCoins()
            binding.tvCoin.text = "${coins} xu"
        }
        binding.icCoin.setColorFilter(ContextCompat.getColor(requireContext(), R.color.yellow));
        lifecycleScope.launchWhenStarted {
            viewModel.user.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                    }

                    is Resource.Success -> {
                        showUserInformation(it.data!!)
                    }

                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }
        binding.constraintProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_userAccountFragment)
        }
        binding.linearAllOrders.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_allOrdersFragment)
        }
        binding.linearFavorite.setOnClickListener{
            findNavController().navigate(R.id.action_profileFragment_to_listFavFragment)
        }
        binding.linearAddress.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToBillingFragment(
                0f,
                emptyArray(),
                false
            )
            findNavController().navigate(action)
        }
        binding.logout.setOnClickListener {
            viewModel.logout()
            val intent = Intent(requireActivity(), LoginRegisterActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }
    private fun showUserInformation(data: User) {
        binding.apply {
            Glide.with(this@ProfileFragment).load(data.imagePath)
                .error(ColorDrawable(Color.BLACK)).into(imageUser)
            tvUserName.text = "${data.firstName} ${data.lastName}"
        }

    }
    override fun onResume() {
        super.onResume()
        showBottomNavigationView()
    }
}