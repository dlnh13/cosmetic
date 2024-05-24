package com.example.cosmetic.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.Glide.init
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
    private val coroutineScope = MainScope() // CoroutineScope tied to the main thread


    inner class PostsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView.rootView) {
        private val viewPagerAdapter = ViewPager2Images()
        val btnComment: ImageView = itemView.findViewById(R.id.btnComment)
        val imgLike: ImageView = itemView.findViewById(R.id.imgLike)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvCaption: TextView = itemView.findViewById(R.id.tvCaption)
        val tvLikes: TextView = itemView.findViewById(R.id.tvLikes)
        val viewPagerPostImages: ViewPager2 = itemView.findViewById(R.id.viewPagerPostImages)
        val imgProfile: ImageView = itemView.findViewById(R.id.imgProfile)
        fun bind(post: Post) {
            coroutineScope.launch {
                // Reset trạng thái của ViewPager2
                viewPagerPostImages.adapter = null
                // Gán adapter và dữ liệu mới cho ViewPager2
                viewPagerPostImages.adapter = viewPagerAdapter
                viewPagerAdapter.differ.submitList(post.imagePost)
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
    ): PostsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.feed_item, parent, false)
        return PostsViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        val post = differ.currentList[position]
        holder.itemView.apply {
            holder.bind(post)
            var isLikedByCurrentUser: Boolean

            Glide.with(holder.itemView.context).load(post.profileImage)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // Vô hiệu hóa cache
                .into(holder.imgProfile)
            coroutineScope.launch {
                holder.tvName.text = getUserName(post.uid)
            }
            holder.tvLikes.text = "${post.likes} Likes"
            holder.tvTime.text = post.time.toString()
            holder.tvCaption.text = post.caption.toString()
            holder.btnComment.setOnClickListener {
                onComment?.invoke(post)
            }
            isLikedByCurrentUser = post.likers?.contains(getUid()) ?: false
            if (isLikedByCurrentUser) {
                holder.imgLike.setImageResource(R.drawable.heartfilled)
                holder.imgLike.setColorFilter(ContextCompat.getColor(context, R.color.red));
            } else {
                holder.imgLike.setImageResource(R.drawable.heart)
            }
            holder.imgLike.setOnClickListener {
                onClick?.invoke(post, isLikedByCurrentUser)
                isLikedByCurrentUser = !isLikedByCurrentUser
                if (isLikedByCurrentUser) {

                    holder.imgLike.setImageResource(R.drawable.heartfilled)
                    holder.imgLike.setColorFilter(ContextCompat.getColor(context, R.color.red));
                } else {
                    holder.imgLike.setImageResource(R.drawable.heart)
                }
            }
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




