package com.example.cosmetic.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.cosmetic.R
import com.example.cosmetic.databinding.ActivityShoppingBinding
import com.example.cosmetic.util.Resource
import com.example.cosmetic.viewmodel.CartViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class ShoppingActivity : AppCompatActivity() {
    val binding by lazy {
        ActivityShoppingBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val navController = findNavController(R.id.shoppingHostFragment)
        binding.bottomNavigation.setupWithNavController(navController)
        val auth = FirebaseAuth.getInstance()
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            if (item.itemId != R.id.homeFragment && auth.currentUser == null) {
                Toast.makeText(
                    applicationContext,
                    "Chưa đăng nhập!",
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(this, LoginRegisterActivity::class.java))
                this.finish()
                false
            } else {
                // Allow navigation to the selected fragment
                navController.navigate(item.itemId)
                true
            }
        }
        if (auth.currentUser != null) {
            val viewModel by viewModels<CartViewModel>()
        lifecycleScope.launchWhenStarted {
            viewModel.cartProducts.collectLatest {
                when (it) {
                    is Resource.Success -> {
                        val count = it.data?.size ?: 0
                        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
                        bottomNavigation.getOrCreateBadge(R.id.cartFragment).apply {
                            number = count
                            backgroundColor = resources.getColor(R.color.pink)
                        }
                    }
                    else -> Unit
                }
            }
        }
        }
    }
}