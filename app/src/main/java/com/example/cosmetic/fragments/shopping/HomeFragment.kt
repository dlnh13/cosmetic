package com.example.cosmetic.fragments.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.cosmetic.R
import com.example.cosmetic.adapters.HomeViewpagerAdapter
import com.example.cosmetic.databinding.FragmentHomeBinding
import com.example.cosmetic.fragments.categories.HaircareFragment
import com.example.cosmetic.fragments.categories.MainCategoryFragment
import com.example.cosmetic.fragments.categories.MakeupFragment
import com.example.cosmetic.fragments.categories.MakeuptoolFragment
import com.example.cosmetic.fragments.categories.PerfumeFragment
import com.example.cosmetic.fragments.categories.SkinCareFragment
import com.google.android.material.tabs.TabLayoutMediator

class HomeFragment:Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
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
        binding.viewpagerHome.isUserInputEnabled = false
        val viewPager2Adapter = HomeViewpagerAdapter(categoriesFragment,childFragmentManager,lifecycle)
        binding.viewpagerHome.adapter = viewPager2Adapter
        TabLayoutMediator(binding.tabLayout,binding.viewpagerHome){
            tab, position ->
            when (position){
                0 -> tab.text = "Home"
                1 -> tab.text = "Makeup"
                2 -> tab.text = "Makeup Tool"
                3 -> tab.text = "SkinCare"
                4 -> tab.text = "HairCare"
                5 -> tab.text = "Perfume"
            }
        }.attach()
    }
}