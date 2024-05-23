package com.example.cosmetic.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cosmetic.data.Comment
import com.example.cosmetic.databinding.CommentItemBinding
import com.example.cosmetic.util.Uid.getProfileImg
import com.example.cosmetic.util.Uid.getUserName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class CommentsAdapter : RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder>() {
    private val coroutineScope = MainScope() // CoroutineScope tied to the main thread

    inner class CommentsViewHolder(val binding: CommentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            binding.apply {
                coroutineScope.launch {
                    Glide.with(itemView).load(getProfileImg(comment.uid)).into(imgProfile)
                }
//                Glide.with(itemView).load(comment.profileImage).into(imgProfile)
                tvComment.text = comment.content
                coroutineScope.launch {
                    tvUserName.text = getUserName(comment.uid)

                }
            }
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CommentsAdapter.CommentsViewHolder {
        return CommentsViewHolder(
            CommentItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: CommentsAdapter.CommentsViewHolder, position: Int) {
        val comment = differ.currentList[position]
        holder.bind(comment)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun clear() {
        coroutineScope.cancel()
    }
}