package com.example.cosmetic.view.fragments.blog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cosmetic.view.adapters.ViewPager2Images
import com.example.cosmetic.data.Post
import com.example.cosmetic.databinding.FragmentCreatePostBinding
import com.example.cosmetic.util.Resource
import com.example.cosmetic.util.Uid.getUid
import com.example.cosmetic.util.getTime
import com.example.cosmetic.viewmodel.CreatePostViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.util.UUID

@AndroidEntryPoint
class CreatePostFragment : Fragment() {
    private lateinit var binding: FragmentCreatePostBinding
    private val viewModel by viewModels<CreatePostViewModel>()
    private lateinit var imageActivityResultLauncher: ActivityResultLauncher<Intent>
    // private lateinit var cameraActivityResultLauncher: ActivityResultLauncher<Intent>
    private var selectedImages = mutableListOf<Uri>()
    private var displayImages = mutableListOf<String>()
    private val viewPagerAdapter by lazy { ViewPager2Images() }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreatePostBinding.inflate(inflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val intent = result.data
                    displayImages.clear()
                    selectedImages.clear()
                    //Multiple images selected
                    if (intent?.clipData != null) {
                        val count = intent.clipData?.itemCount ?: 0
                        (0 until count).forEach {
                            val imagesUri = intent.clipData?.getItemAt(it)?.uri
                            imagesUri?.let { selectedImages.add(it) }
                            ///////// images uri = link img de viewpager???
                            displayImages.add(imagesUri.toString())
                        }

                    } else { //One images was selected
                        val imageUri = intent?.data
                        imageUri?.let { selectedImages.add(it) }
                        displayImages.add(imageUri.toString())
                    }

                    viewPagerAdapter.differ.submitList(displayImages.toList())
                }

            }
//        cameraActivityResultLauncher =
//            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//                if (result.resultCode == Activity.RESULT_OK) {
//                    val imageBitmap = result.data?.extras?.get("data") as Bitmap
//                    val uri = saveImageToUri(imageBitmap)
//                    uri?.let {
//                        selectedImages.add(it)
//                        displayImages.add(it.toString())
//                        viewPagerAdapter.differ.submitList(displayImages.toList())
//                    }
//                }
//            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewpager()


        binding.btnAdd.setOnClickListener {
            addImageDialog()
        }

        binding.btnPost.setOnClickListener {
            binding.apply {
                val post = Post(
                    postId = UUID.randomUUID().toString(),
                    uid = getUid(),
                    time = getTime(),
                    caption = edCaption.text.toString().trim(),
                    imagePost = displayImages
                )
                viewModel.uploadPost(post, selectedImages)
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.post.collectLatest {
                when (it) {
                    is Resource.Loading -> {
                        binding.progressbarCreatePost.visibility = View.VISIBLE
                    }

                    is Resource.Success -> {
                        findNavController().navigateUp()
                    }

                    is Resource.Error -> {
                        binding.progressbarCreatePost.visibility = View.INVISIBLE
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        }

    }

    private fun addImageDialog() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose your profile picture")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> {
                    Toast.makeText(requireContext(), "No camera app found", Toast.LENGTH_SHORT)
                        .show()
                    //takePhotoWithCamera()

                }

                options[item] == "Choose from Gallery" -> {
                    pickImageFromGallery()
                }

                options[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

//    private fun takePhotoWithCamera() {
////        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
////        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
////            cameraActivityResultLauncher.launch(takePictureIntent)
////        } else {
////            Toast.makeText(requireContext(), "No camera app found", Toast.LENGTH_SHORT).show()
////        }
//        Toast.makeText(requireContext(), "No camera app found", Toast.LENGTH_SHORT).show()
//    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.type = "image/*"
        imageActivityResultLauncher.launch(intent)

    }

    //    private fun saveImageToUri(bitmap: Bitmap): Uri? {
//        val bytes = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
//        val path = MediaStore.Images.Media.insertImage(
//            requireActivity().contentResolver,
//            bitmap,
//            "Title_" + UUID.randomUUID(),
//            null
//        )
//        return Uri.parse(path)
//    }
    private fun setupViewpager() {
        binding.apply {
            viewPagerPostImages.adapter = viewPagerAdapter
        }
    }
}