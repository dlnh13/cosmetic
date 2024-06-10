package com.example.cosmetic.fragments.shopping

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cosmetic.R
import com.example.cosmetic.activities.LoginRegisterActivity
import com.example.cosmetic.adapters.HomeViewpagerAdapter
import com.example.cosmetic.adapters.SpecialProductAdapter
import com.example.cosmetic.databinding.FragmentHomeBinding
import com.example.cosmetic.fragments.categories.HaircareFragment
import com.example.cosmetic.fragments.categories.MainCategoryFragment
import com.example.cosmetic.fragments.categories.MakeupFragment
import com.example.cosmetic.fragments.categories.MakeuptoolFragment
import com.example.cosmetic.fragments.categories.PerfumeFragment
import com.example.cosmetic.fragments.categories.SkinCareFragment
import com.example.cosmetic.util.Resource
import com.example.cosmetic.util.Uid.checkAuth
import com.example.cosmetic.util.VerticalItemDecoration
import com.example.cosmetic.viewmodel.NotificationViewModel
import com.example.cosmetic.viewmodel.SearchViewModel
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import org.checkerframework.checker.units.qual.s

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var searchProductsAdapter: SpecialProductAdapter
    private val viewModel by viewModels<SearchViewModel>()
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val categoriesFragment = arrayListOf(
            MainCategoryFragment(),
            MakeupFragment(),
            MakeuptoolFragment(),
            SkinCareFragment(),
            HaircareFragment(),
            PerfumeFragment()
        )
        setupSearchRv()
        if (auth.currentUser == null) {
            binding.btnMess.visibility = View.GONE
            binding.imgNotification.visibility = View.GONE

        }
        lifecycleScope.launchWhenStarted {
            viewModel.searchResults.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        Log.d("HomeFragment", "Loading search results...")

                        //binding.progressbar.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        //binding.progressbar.visibility = View.GONE
                        searchProductsAdapter.differ.submitList(it.data)
                    }

                    is Resource.Error -> {
                        // binding.progressbar.visibility = View.GONE
                        Toast.makeText(requireContext(), "Error: ${it.message}", Toast.LENGTH_SHORT)
                            .show()
                    }

                    else -> Unit
                }
            }
        }

        binding.viewpagerHome.isUserInputEnabled = false
        val viewPager2Adapter =
            HomeViewpagerAdapter(categoriesFragment, childFragmentManager, lifecycle)
        binding.viewpagerHome.adapter = viewPager2Adapter
        TabLayoutMediator(binding.tabLayout, binding.viewpagerHome) { tab, position ->
            when (position) {
                0 -> tab.text = "Home"
                1 -> tab.text = "Makeup"
                2 -> tab.text = "Makeup Tool"
                3 -> tab.text = "SkinCare"
                4 -> tab.text = "HairCare"
                5 -> tab.text = "Perfume"
            }
        }.attach()

        binding.btnMess.setOnClickListener {
            checkAuth(requireActivity())
            findNavController().navigate(R.id.action_homeFragment_to_chatFragment)
        }
        binding.imgNotification.setOnClickListener {
            checkAuth(requireActivity())
            findNavController().navigate(R.id.action_homeFragment_to_notificationFragment)
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null && query.isNotEmpty()) {
                    binding.searchView.clearFocus()
                    binding.viewpagerHome.visibility = View.GONE
                    binding.rvSearchProduct.visibility = View.VISIBLE
                    viewModel.search(query)
                    return true
                }
                return false
            }

            //            if(hasFocus){
//                Log.d("///","search")
//            } else {
//                Log.d("ko has focus","search")
//
//                val searchInput = binding.edSearch.text.trim().toString()
//                if (searchInput.isNotEmpty()){
//                    binding.viewpagerHome.visibility = View.GONE
//                    binding.rvSearchProduct.visibility = View.VISIBLE
//                    setupSearchRv()
//                    viewModel.search(searchInput)
//                }
//            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    binding.viewpagerHome.visibility = View.VISIBLE
                    binding.rvSearchProduct.visibility = View.GONE
                } else {
                    binding.viewpagerHome.visibility = View.GONE
                    binding.rvSearchProduct.visibility = View.VISIBLE
                }
                return false
            }
        }
        )
    }

    //    private fun setupSearchView() {
//        binding.edSearch.setOnFocusChangeListener { _, hasFocus ->
//            if (hasFocus) {
//               // binding.viewpagerHome.visibility = View.GONE
//              //  binding.rvSearchProduct.visibility = View.VISIBLE
//            } else {
//                val searchInput = binding.edSearch.text.trim().toString()
//                if (searchInput.isNotEmpty()) {
//                    viewModel.search(searchInput)
//                }
//            }
//        }
//    }
    private fun setupSearchRv() {
        searchProductsAdapter = SpecialProductAdapter()
        binding.rvSearchProduct.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL, false
            )
            adapter = searchProductsAdapter
            addItemDecoration(VerticalItemDecoration())

        }
    }
}