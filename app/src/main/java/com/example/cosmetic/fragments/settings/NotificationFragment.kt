package com.example.cosmetic.fragments.settings

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

import com.example.cosmetic.adapters.NotificationAdapter
import com.example.cosmetic.databinding.FragmentNotiBinding
import com.example.cosmetic.util.Resource
import com.example.cosmetic.util.VerticalItemDecoration
import com.example.cosmetic.viewmodel.NotificationViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class NotificationFragment : Fragment(){
    private lateinit var binding : FragmentNotiBinding
    private val viewModel by viewModels<NotificationViewModel>()

    private val notificationAdapter by lazy { NotificationAdapter() }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotiBinding.inflate(inflater)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupNotiRv()
        notificationAdapter.onClick = {
            val b = Bundle().apply {
                putParcelable("product", it)
            }
            findNavController().navigate(R.id.action_notificationFragment_to_productDetailFragment, b)
        }
        lifecycleScope.launchWhenStarted {
            viewModel.notifications.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarNoti.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        binding.progressbarNoti.visibility = View.GONE
                        if (it.data!!.isEmpty()) {
                            binding.linearEmpty.visibility = View.VISIBLE
                            binding.rvNotifications.visibility = View.GONE
                        } else {
                            binding.linearEmpty.visibility = View.GONE
                            binding.rvNotifications.visibility = View.VISIBLE
                            notificationAdapter.differ.submitList(it.data)
                        }
                    }

                    is Resource.Error -> {
                        binding.progressbarNoti.visibility = View.GONE
                        if (it.message == "don't have favorite products found") {
                            binding.linearEmpty.visibility = View.VISIBLE
                            binding.rvNotifications.visibility = View.GONE
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
    }
    private fun setupNotiRv() {
        binding.rvNotifications.apply {
            adapter = notificationAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            addItemDecoration(VerticalItemDecoration())
        }
    }
}