package com.example.recsolar.models

import android.app.people.ConversationStatus
import android.graphics.Bitmap
import java.util.Date

data class ChatMessage(
    var senderId: String,
    var receiverId: String,
    var message: String,
    var dateTime: String,
    var imageBitmap: String,
    var dateObject: Date?,

    var conversationId: String,
    var conversationName:String,
    var conversationImage: String
){
    val hasImage: Boolean
        get() = imageBitmap != null
}
