package com.example.recsolar.listeners

import com.example.recsolar.models.User

interface ConversationListener {
    fun onConversationClicked(user: User)
}