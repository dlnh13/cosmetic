package com.example.cosmetic.adapters

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Glide.init
import com.example.cosmetic.R
import com.example.cosmetic.data.Address
import com.example.cosmetic.data.CartProduct
import com.example.cosmetic.data.Post
import com.example.cosmetic.databinding.ColorRvItemBinding
import com.example.cosmetic.databinding.FeedItemBinding
import com.example.cosmetic.helper.getProductPrice
import com.example.cosmetic.util.Uid
import com.example.cosmetic.util.Uid.getUid
import com.example.cosmetic.util.Uid.getUserName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class PostsAdapter : RecyclerView.Adapter<PostsAdapter.PostsViewHolder>() {
    var isLikedByCurrentUser: Boolean = false
    private val coroutineScope = MainScope() // CoroutineScope tied to the main thread

    inner class PostsViewHolder(val binding: FeedItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val viewPagerAdapter = ViewPager2Images()

        fun bind(post: Post) {
            binding.apply {
                viewPagerPostImages.adapter = viewPagerAdapter
                viewPagerAdapter.differ.submitList(post.imagePost)
                //tvName.text = Uid.getUserName(post.uid)
                Glide.with(itemView).load(post.profileImage).into(imgProfile)
                coroutineScope.launch {
                    tvName.text = getUserName(post.uid)
                }
                tvLikes.text = "${post.likes} Likes"
                tvTime.text = post.time.toString()
                tvCaption.text = post.caption.toString()
                isLikedByCurrentUser = post.likers?.contains(getUid()) ?: false

                val imageResource = if (isLikedByCurrentUser) {
                    // Nếu người dùng đã like bài viết trước đó
                    R.drawable.heartfilled
                } else {
                    // Nếu người dùng chưa like bài viết trước đó
                    R.drawable.ic_favorite
                }
                imgLike.setImageResource(imageResource)

                imgLike.setOnClickListener {
                    onClick?.invoke(post, isLikedByCurrentUser)
                    // Cập nhật trạng thái của isLikedByCurrentUser
                    isLikedByCurrentUser = !isLikedByCurrentUser
                }
            }
        }

    }

    private val diffCallback = object : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostsAdapter.PostsViewHolder {
        return PostsViewHolder(
            FeedItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }


    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        val post = differ.currentList[position]
        holder.bind(post)
        holder.binding.btnComment.setOnClickListener {
            onComment?.invoke(post)
        }


    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onClick: ((Post, Boolean) -> Unit)? = null
    var onComment: ((Post) -> Unit)? = null

    fun clear() {
        coroutineScope.cancel()
    }
}