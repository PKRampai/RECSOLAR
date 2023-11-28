package com.example.recsolar.adapters

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.recsolar.activities.FullImageActivity
import com.example.recsolar.databinding.ItemContainerReceivedMessageBinding
import com.example.recsolar.databinding.ItemContainerSentMessageBinding
import com.example.recsolar.models.ChatMessage
import com.example.recsolar.models.ImageData
import java.io.ByteArrayOutputStream

class ChatAdapter(
    private val receiverProfileImage: Bitmap,
    private val chatMessages: List<ChatMessage>,
    private val senderId: String,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val binding = ItemContainerSentMessageBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                val viewHolder = SentMessageViewHolder(binding)

                // Set the onImageClickListener for SentMessageViewHolder
                viewHolder.onImageClickListener = object : SentMessageViewHolder.OnImageClickListener {
                    override fun onImageClick(bitmap: Bitmap?) {
                        // Handle image click, e.g., show a full-screen image
                        if (bitmap != null) {
                            val intent = Intent(parent.context, FullImageActivity::class.java)
                            val byteArrayOutputStream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                            val byteArray = byteArrayOutputStream.toByteArray()
                            val imageData = ImageData(byteArray)
                            intent.putExtra("image", imageData)
                            parent.context.startActivity(intent)
                        }
                    }
                }

                viewHolder
            }
            VIEW_TYPE_RECEIVED -> {
                val binding = ItemContainerReceivedMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                val viewHolder = ReceivedMessageViewHolder(binding, receiverProfileImage)
                viewHolder.onImageClickListener = object : ReceivedMessageViewHolder.OnImageClickListener {
                    override fun onImageClick(bitmap: Bitmap?) {
                        // Handle image click, e.g., show a full-screen image
                        if (bitmap != null) {

                            val intent = Intent(parent.context, FullImageActivity::class.java)
                            val byteArrayOutputStream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                            val byteArray = byteArrayOutputStream.toByteArray()
                            val imageData = ImageData(byteArray)
                            intent.putExtra("image", imageData)
                            parent.context.startActivity(intent)
                        }
                    }
                }
                viewHolder
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatMessage = chatMessages[position]
        when (holder) {
            is SentMessageViewHolder -> holder.setData(chatMessage)
            is ReceivedMessageViewHolder -> holder.setData(chatMessage)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (chatMessages[position].senderId == senderId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun getItemCount(): Int {
        return chatMessages.size
    }

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }
}

class SentMessageViewHolder(private val binding: ItemContainerSentMessageBinding) :
    RecyclerView.ViewHolder(binding.root) {
    interface OnImageClickListener {
        fun onImageClick(bitmap: Bitmap?)
    }

    var onImageClickListener: OnImageClickListener? = null

    fun setData(chatMessage: ChatMessage) {
        binding.textMessage.text = chatMessage.message
        binding.textDateTime.text = chatMessage.dateTime

        if (chatMessage.imageBitmap != "") {
            // Set the Bitmap in the ImageView
            val decodedBitmap = getConversationImage(chatMessage.imageBitmap)
            binding.imageMessage.setImageBitmap(decodedBitmap)
            binding.imageMessage.visibility = View.VISIBLE
            binding.textMessage.visibility = View.GONE

        }
        binding.imageMessage.setOnClickListener {
            onImageClickListener?.onImageClick(getConversationImage(chatMessage.imageBitmap))
        }
    }

    private fun getConversationImage(encodedImage: String?): Bitmap? {
        if (encodedImage.isNullOrEmpty()) {
            return null
        }

        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}

class ReceivedMessageViewHolder(
    private val binding: ItemContainerReceivedMessageBinding,
    private val receiverProfileImage: Bitmap
) : RecyclerView.ViewHolder(binding.root) {

    interface OnImageClickListener {
        fun onImageClick(bitmap: Bitmap?)
    }

    var onImageClickListener: OnImageClickListener? = null

    fun setData(chatMessage: ChatMessage) {
        binding.textMessage.text = chatMessage.message
        binding.textDateTime.text = chatMessage.dateTime

        if (chatMessage.imageBitmap != "") {
            // Set the Bitmap in the ImageView
            val decodedBitmap = getConversationImage(chatMessage.imageBitmap)
            binding.imageMessage.setImageBitmap(decodedBitmap)
            binding.imageMessage.visibility = View.VISIBLE
            binding.textMessage.visibility = View.GONE


        }

        binding.imageProfile.setImageBitmap(receiverProfileImage)

        binding.imageMessage.setOnClickListener {
            onImageClickListener?.onImageClick(getConversationImage(chatMessage.imageBitmap))
        }
    }

    private fun getConversationImage(encodedImage: String?): Bitmap? {
        if (encodedImage.isNullOrEmpty()) {
            return null
        }

        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}
