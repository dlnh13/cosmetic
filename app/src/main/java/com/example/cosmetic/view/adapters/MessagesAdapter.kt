package com.example.cosmetic.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.cosmetic.R
import com.example.cosmetic.data.Message
import com.example.cosmetic.util.Uid.getUid
import java.text.SimpleDateFormat

class MessagesAdapter() : RecyclerView.Adapter<MessagesHolder>() {

    private val LEFT = 0
    private val RIGHT = 1

    private val diffCallback = object : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == RIGHT) {
            val view = inflater.inflate(R.layout.chat_right_item, parent, false)
            MessagesHolder(view)
        } else {
            val view = inflater.inflate(R.layout.chat_left_item, parent, false)
            MessagesHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: MessagesHolder, position: Int) {
        val message = differ.currentList[position]
        val formatter = SimpleDateFormat("HH:mm dd-MM")
        //val date = Date(message.time!!)
        holder.messageText.visibility = View.VISIBLE
        holder.timeOfSent.visibility = View.VISIBLE

        holder.messageText.text = message.message
        holder.timeOfSent.text = formatter.format(message.time)
    }

    override fun getItemViewType(position: Int): Int {
        val message = differ.currentList[position]
        return if (message.sender == getUid()) RIGHT else LEFT
    }

}

class MessagesHolder(itemView: View) : RecyclerView.ViewHolder(itemView.rootView) {
    val messageText: TextView = itemView.findViewById(R.id.tvMessage)
    val timeOfSent: TextView = itemView.findViewById(R.id.tvTime)

}