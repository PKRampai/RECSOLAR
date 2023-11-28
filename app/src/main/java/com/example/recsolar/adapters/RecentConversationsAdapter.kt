package com.example.recsolar.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.recsolar.databinding.ItemContainerRecentConversationsBinding
import com.example.recsolar.listeners.ConversationListener
import com.example.recsolar.models.ChatMessage
import com.example.recsolar.models.User

class RecentConversationsAdapter(private val chatMessages: List<ChatMessage>, private val conversationListener: ConversationListener) : RecyclerView.Adapter<RecentConversationsAdapter.ConversationViewHolder>() {

    inner class ConversationViewHolder(private val binding: ItemContainerRecentConversationsBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun setData(chatMessage: ChatMessage) {
        binding.imageProfile.setImageBitmap(getConversationImage(chatMessage.conversationImage))
            binding.textName.text = chatMessage.conversationName
            binding.textRecentMessage.text = chatMessage.message
    //Adapted from:Youtube
    //profile:https://www.youtube.com/@chiragkachhadiya
    //Date:18 July 2021 - 8 Aug 2021
    //Link:https://www.youtube.com/watch?v=ENK4ONrRm8s&list=PLam6bY5NszYOhXkY7jOS4EQAKcQwkXrp4&pp=iAQB

            binding.root.setOnClickListener {
                val user = User("", "", "", "", "", "", "")
                user.id = chatMessage.conversationId
                user.name = chatMessage.conversationName
                user.image = chatMessage.conversationImage
                conversationListener.onConversationClicked(user)
            }
    }

        private fun getConversationImage(encodedImage: String): Bitmap {
            val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemContainerRecentConversationsBinding.inflate(inflater, parent, false)
        return ConversationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val chat = chatMessages[position]
        holder.setData(chat)
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }





}