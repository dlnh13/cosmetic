package com.example.cosmetic.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cosmetic.R
import com.example.cosmetic.data.Comment
import com.example.cosmetic.databinding.CommentItemBinding
import com.example.cosmetic.util.Uid.getProfileImg
import com.example.cosmetic.util.Uid.getUserName
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class CommentsAdapter : RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder>() {
    private val coroutineScope = MainScope() // CoroutineScope tied to the main thread

    inner class CommentsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView.rootView) {
        val imgProfile: ImageView = itemView.findViewById(R.id.imgProfile)
        val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        val tvComment: TextView = itemView.findViewById(R.id.tvComment)

//        binding.apply
//        {
//            coroutineScope.launch {
//                Glide.with(itemView).load(getProfileImg(comment.uid)).into(imgProfile)
//            }
//            tvComment.text = comment.content
//            coroutineScope.launch {
//                tvUserName.text = getUserName(comment.uid)
//
//            }
//        }

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
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.comment_item, parent, false)
        return CommentsViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentsAdapter.CommentsViewHolder, position: Int) {
        val comment = differ.currentList[position]
        holder.itemView.apply {
            coroutineScope.launch {
                Glide.with(holder.itemView.context).load(getProfileImg(comment.uid))
                    .into(holder.imgProfile)
            }
            holder.tvComment.text = comment.content
            coroutineScope.launch {
                holder.tvUserName.text = getUserName(comment.uid)
            }
        }
    }

        override fun getItemCount(): Int {
            return differ.currentList.size
        }

        fun clear() {
            coroutineScope.cancel()
        }
    }