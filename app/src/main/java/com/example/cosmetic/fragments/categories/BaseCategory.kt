package com.example.cosmetic.fragments.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cosmetic.R
import com.example.cosmetic.adapters.SpecialProductAdapter
import com.example.cosmetic.databinding.FragmentBaseCategoryBinding

open class BaseCategory : Fragment(R.layout.fragment_base_category) {
    private lateinit var binding: FragmentBaseCategoryBinding
    protected lateinit var offerAdapter: SpecialProductAdapter
    protected lateinit var specialProductsAdapter: SpecialProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBaseCategoryBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOfferRv()
        setupSpecialProductsRv()
        binding.rvOffer.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView,dx:Int, dy:Int){
                super.onScrolled(recyclerView, dx, dy)
                if(!recyclerView.canScrollVertically(1)&& dx!= 0){
                    onOfferPagingRequest()
                }
            }
        })
        binding.nestedScrollBaseCategory.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener{ v, _, scrollY, _, _ ->
            if(v.getChildAt(0).bottom<=v.height+scrollY){
                onSpecialProductsPagingRequest()
            }
        })
    }

    open fun onOfferPagingRequest() {

    }

    open fun onSpecialProductsPagingRequest() {

    }

    private fun setupSpecialProductsRv() {
        specialProductsAdapter = SpecialProductAdapter()
        binding.rvSpecialProducts.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL, false)
//                GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
            adapter = specialProductsAdapter
        }
    }

    private fun setupOfferRv() {
        offerAdapter = SpecialProductAdapter()
        binding.rvOffer.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL, false
            )
            adapter = offerAdapter
        }
    }
}