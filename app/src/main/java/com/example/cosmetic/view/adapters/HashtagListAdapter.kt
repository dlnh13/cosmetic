package com.example.cosmetic.view.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.cosmetic.data.Hashtag
import com.example.cosmetic.databinding.HashtagRvItemBinding

class HashtagListAdapter : RecyclerView.Adapter<HashtagListAdapter.HashtagListViewHolder>() {

    inner class HashtagListViewHolder(private val binding: HashtagRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(hashtag: Hashtag) {
            binding.count.text = "${hashtag.postIds.size } bài viết"
            binding.hashtag.text = hashtag.hashtag
        }
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Hashtag>() {
        override fun areItemsTheSame(oldItem: Hashtag, newItem: Hashtag): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Hashtag, newItem: Hashtag): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HashtagListViewHolder {
        return HashtagListViewHolder(
            HashtagRvItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: HashtagListViewHolder, position: Int) {
        val hashtag = differ.currentList[position]
        holder.bind(hashtag)

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(hashtag!!)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    var onItemClick: ((Hashtag) -> Unit)? = null

}