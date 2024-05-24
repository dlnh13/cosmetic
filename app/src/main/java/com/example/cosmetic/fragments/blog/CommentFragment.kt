package com.example.cosmetic.fragments.blog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cosmetic.adapters.CommentsAdapter
import com.example.cosmetic.data.Comment
import com.example.cosmetic.databinding.FragmentCommentsBinding
import com.example.cosmetic.util.Resource
import com.example.cosmetic.util.Uid.getProfileImg
import com.example.cosmetic.util.Uid.getUid
import com.example.cosmetic.util.getTime
import com.example.cosmetic.viewmodel.CommentViewModel

import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.util.UUID

@AndroidEntryPoint
class CommentFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentCommentsBinding
    private val args by navArgs<CommentFragmentArgs>()
    private val viewModel by viewModels<CommentViewModel>()
    private lateinit var commentsAdapter: CommentsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCommentsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val post = args.post
        lateinit var  imagePath :String
        viewModel.fetchComments(post)
        setupCommentsRv()
        lifecycleScope.launchWhenStarted {
            viewModel.comments.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarAddress.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        commentsAdapter.differ.submitList(it.data)
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
            viewModel.comment.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                    }

                    is Resource.Success -> {
                        binding.edComment.setText("")
                        binding.edComment.requestFocus()
                    }

                    is Resource.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            imagePath =  getProfileImg(getUid())
        }
        binding.btnPostComment.setOnClickListener {
            binding.apply {
                val comment = Comment(
                    commentId = UUID.randomUUID().toString(),
                    postId = post.postId,
                    uid = getUid(),
                    time = getTime(),
                    content = edComment.text.toString(),
                    profileImage = imagePath
                )
                viewModel.saveComment(comment)
            }
        }
    }

    private fun setupCommentsRv() {
        commentsAdapter = CommentsAdapter()
        binding.rvComments.apply {
            adapter = commentsAdapter
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }

    // set up vi tri fragment
    override fun onStart() {
        super.onStart()

        // Tùy chỉnh vị trí xuất hiện của BottomSheetDialog
        val bottomSheet =
            (dialog as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            val layoutParams = it.layoutParams
            val windowHeight = resources.displayMetrics.heightPixels
            val desiredHeight = windowHeight - dpToPx(30) // Cách đáy 10dp
            layoutParams.height = desiredHeight
            it.layoutParams = layoutParams
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        commentsAdapter.clear()
    }
}