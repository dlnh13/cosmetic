package com.example.cosmetic.view.fragments.categories

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.cosmetic.data.Category
import com.example.cosmetic.util.Resource
import com.example.cosmetic.viewmodel.CategoryViewModel
import com.example.cosmetic.viewmodel.factory.BaseCategoryViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
@AndroidEntryPoint
class PerfumeFragment: BaseCategory() {
    @Inject
    lateinit var firestore: FirebaseFirestore
    val viewModel by viewModels<CategoryViewModel>{
        BaseCategoryViewModelFactory(firestore, Category.Perfume)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenStarted {
            viewModel.offerProducts.collectLatest {
                when (it){
                    is Resource.Loading -> {
                    }
                    is Resource.Success -> {
                        offerAdapter.differ.submitList(it.data)
                    }
                    is Resource.Error -> {
                        Snackbar.make(requireView(),it.message.toString(), Snackbar.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.specialProducts.collectLatest {
                when (it){
                    is Resource.Loading -> {
                    }
                    is Resource.Success -> {
                        specialProductsAdapter.differ.submitList(it.data)
                    }
                    is Resource.Error -> {
                        Snackbar.make(requireView(),it.message.toString(), Snackbar.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }
    }
}