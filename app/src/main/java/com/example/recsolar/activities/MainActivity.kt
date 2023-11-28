package com.example.recsolar.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import com.example.recsolar.R
import com.example.recsolar.databinding.ActivityMainBinding
import com.example.recsolar.utilities.Constants
import com.example.recsolar.utilities.PreferenceManager
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.example.recsolar.adapters.RecentConversationsAdapter
import com.example.recsolar.listeners.ConversationListener
import com.example.recsolar.models.ChatMessage
import com.example.recsolar.models.User
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.messaging.FirebaseMessaging
import java.util.Collections

class MainActivity : AppCompatActivity(), ConversationListener{

    private var binding: ActivityMainBinding? = null
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var conversations: MutableList<ChatMessage>
    private lateinit var conversationsAdapter: RecentConversationsAdapter
    private lateinit var database: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        preferenceManager = PreferenceManager(applicationContext)
        init()
        loadUserDetails()
        getToken()
        setListeners()
        listenConversations()
    }


    private fun init(){
        conversations = ArrayList()
        conversationsAdapter = RecentConversationsAdapter(conversations, this)
        binding?.conversationsRecyclerView?.adapter = conversationsAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun setListeners() {
        binding?.imageSignOut?.setOnClickListener {
            signOut()
        }
        binding?.fabNewChat?.setOnClickListener {
            val intent = Intent(applicationContext, UsersActivity::class.java)
           startActivity(intent)
        }
    }


    private fun loadUserDetails() {
        binding?.textName?.text = preferenceManager.getString(KEY_NAME)
        val bytes = Base64.decode(preferenceManager.getString(KEY_IMAGE), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding?.imageProfile?.setImageBitmap(bitmap)
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun listenConversations(){
        database.collection(KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(KEY_SENDER_ID, preferenceManager.getString(KEY_USER_ID))
            .addSnapshotListener(eventListener)
        database.collection(KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(KEY_RECEIVER_ID, preferenceManager.getString(KEY_USER_ID))
            .addSnapshotListener(eventListener)
    }

    private val eventListener: EventListener<QuerySnapshot> = EventListener { value, error ->
        if (error != null) {
            return@EventListener
        }

        //Adapted from:Youtube
        //profile:https://www.youtube.com/@chiragkachhadiya
        //Date:18 July 2021 - 8 Aug 2021
        //Link:https://www.youtube.com/watch?v=ENK4ONrRm8s&list=PLam6bY5NszYOhXkY7jOS4EQAKcQwkXrp4&pp=iAQB
        if (value != null) {
            for (documentChange in value.documentChanges) {
                if (documentChange.type == DocumentChange.Type.ADDED) {
                    val senderId = documentChange.document.getString(KEY_SENDER_ID)
                    val receiverId = documentChange.document.getString(KEY_RECEIVER_ID)

                    val chatMessage = ChatMessage("", "", "", "", "", null, "", "", "").apply {
                        this.senderId = senderId.toString()
                        this.receiverId = receiverId.toString()

                        if (preferenceManager.getString(KEY_USER_ID) == senderId) {
                            this.conversationImage =
                                documentChange.document.getString(KEY_RECEIVER_IMAGE)!!
                            this.conversationName =
                                documentChange.document.getString(KEY_RECEIVER_NAME)!!
                            this.conversationId =
                                documentChange.document.getString(KEY_RECEIVER_ID)!!
                        } else {
                            this.conversationImage =
                                documentChange.document.getString(KEY_SENDER_IMAGE)!!
                            this.conversationName =
                                documentChange.document.getString(KEY_SENDER_NAME)!!
                            this.conversationId = documentChange.document.getString(KEY_SENDER_ID)!!
                        }
                        this.message = documentChange.document.getString(KEY_LAST_MESSAGE)!!
                        this.dateObject = documentChange.document.getDate(KEY_TIMESTAMP)
                    }
                    conversations.add(chatMessage)
                } else if (documentChange.type == DocumentChange.Type.MODIFIED) {
                    for (i in conversations.indices) {
                        val senderId = documentChange.document.getString(KEY_SENDER_ID)
                        val receiverId = documentChange.document.getString(KEY_RECEIVER_ID)

                        if (conversations[i].senderId == senderId && conversations[i].receiverId == receiverId) {
                            conversations[i].message =
                                documentChange.document.getString(KEY_LAST_MESSAGE)!!
                            conversations[i].dateObject =
                                documentChange.document.getDate(KEY_TIMESTAMP)
                            break
                        }
                    }
                }
            }
            conversations.sortWith(compareByDescending { it.dateObject })
            conversationsAdapter.notifyDataSetChanged()
            binding?.conversationsRecyclerView?.smoothScrollToPosition(0)
            binding?.conversationsRecyclerView?.visibility = View.VISIBLE
            binding?.progressBar?.visibility = View.GONE
        }
    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            updateToken(token)
        }
    }

    private fun updateToken(token: String) {
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(KEY_COLLECTION_USERS).document(
            preferenceManager.getString(KEY_USER_ID).toString()
        )
        documentReference.update(KEY_FCM_TOKEN, token)
            .addOnFailureListener { e ->
                showToast("Unable to update token")
            }
    }

    private fun signOut() {
        showToast("Signing out...")
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(KEY_COLLECTION_USERS).document(
            preferenceManager.getString(KEY_USER_ID).toString()
        )
        val updates = HashMap<String, Any>()
        updates[KEY_FCM_TOKEN] = FieldValue.delete()

        documentReference.update(updates)
            .addOnSuccessListener {
                preferenceManager.clear()
                startActivity(Intent(applicationContext, SignInActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                showToast("Unable to sign out")
            }

    }


    override fun onConversationClicked(user: User) {
        val intent = Intent(applicationContext, ChatActivity::class.java)
        intent.putExtra(KEY_USER, user)
        startActivity(intent)
    }


    val KEY_COLLECTION_USERS = "users"
    val KEY_NAME = "name"
    val KEY_ADDRESS = "address"
    val KEY_EMAIL = "email"
    val KEY_SYSTEMTYPE = "systemType"

    val KEY_PASSWORD = "password"
    val KEY_PREFERENCE_NAME = "chatAppPreference"
    val KEY_IS_SIGNED_IN = "isSignedIn"
    val KEY_USER_ID = "userId"
    val KEY_IMAGE = "image"
    val KEY_USER ="user"
    val KEY_FCM_TOKEN = "fcmToken"

    val KEY_COLLECTION_CHAT = "chat"
    val KEY_SENDER_ID ="senderID"
    val KEY_RECEIVER_ID ="receiverID"
    val KEY_MESSAGE ="message"
    val KEY_TIMESTAMP ="timestamp"

    val KEY_COLLECTION_CONVERSATIONS ="conversations"
    val KEY_SENDER_NAME ="senderName"
    val KEY_RECEIVER_NAME ="receiverName"
    val KEY_SENDER_IMAGE ="senderImage"
    val KEY_RECEIVER_IMAGE ="receiverImage"
    val KEY_LAST_MESSAGE ="lastMessage"


}