package com.example.recsolar.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.recsolar.adapters.ChatAdapter
import com.example.recsolar.databinding.ActivityChatBinding
import com.example.recsolar.models.ChatMessage
import com.example.recsolar.models.User
import com.example.recsolar.utilities.Constants
import com.example.recsolar.utilities.PreferenceManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.Date
import java.util.Locale

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var receiverUser: User

    private lateinit var chatMessages: ArrayList<ChatMessage>
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var database: FirebaseFirestore
    private var conversationId: String? = null
    private var imageBitmap: Bitmap? = null
    private var encodedImage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        loadReceiverDetails()
        init()
        listenMessages()
    }

//Adapted from:Youtube
//profile:https://www.youtube.com/@chiragkachhadiya
//Date:18 July 2021 - 8 Aug 2021
//Link:https://www.youtube.com/watch?v=ENK4ONrRm8s&list=PLam6bY5NszYOhXkY7jOS4EQAKcQwkXrp4&pp=iAQB

    private fun init() {
        preferenceManager = PreferenceManager(applicationContext)
        chatMessages = ArrayList()
        chatAdapter = ChatAdapter(
            getBitmapFromEncodedString(receiverUser.image),
            chatMessages,
            preferenceManager.getString(KEY_USER_ID) ?: ""
        )


        binding.chatRecyclerView.adapter = chatAdapter

        database = FirebaseFirestore.getInstance()
    }
    private fun sendMessageWithImage(encodedImage: String?) {
        val message = hashMapOf(
            KEY_SENDER_ID to preferenceManager.getString(KEY_USER_ID),
            KEY_RECEIVER_ID to receiverUser.id,
            KEY_TIMESTAMP to Date()
        )

        if (!encodedImage.isNullOrEmpty()) {
            message[KEY_IMAGE_BITMAP] = encodedImage
        }

        database.collection(KEY_COLLECTION_CHAT)
            .add(message)
            .addOnSuccessListener { documentReference ->
                if (conversationId != null) {
                    updateConversation(binding.inputMessage.text.toString())
                } else {
                    val conversation = hashMapOf(
                        KEY_SENDER_ID to preferenceManager.getString(KEY_USER_ID),
                        KEY_SENDER_NAME to preferenceManager.getString(KEY_NAME),
                        KEY_SENDER_IMAGE to preferenceManager.getString(KEY_IMAGE),
                        KEY_RECEIVER_ID to receiverUser.id,
                        KEY_RECEIVER_NAME to receiverUser.name,
                        KEY_RECEIVER_IMAGE to receiverUser.image,
                        KEY_LAST_MESSAGE to binding.inputMessage.text.toString(),
                        KEY_TIMESTAMP to FieldValue.serverTimestamp()
                    )
                    addConversation(conversation)
                }
                // Reset imageBitmap and encodedImage after sending

            }
            .addOnFailureListener { e ->
                // Handle failure
                Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun sendMessage() {
        if (binding.inputMessage.text.isNullOrEmpty() && encodedImage == null) {
            // Handle case where both message and image are empty
        } else {
            val message = hashMapOf(
                KEY_SENDER_ID to preferenceManager.getString(KEY_USER_ID),
                KEY_RECEIVER_ID to receiverUser.id,
                KEY_TIMESTAMP to Date()
            )
            if (!binding.inputMessage.text.isNullOrEmpty()) {
                message[KEY_MESSAGE] = binding.inputMessage.text.toString()
            }

            if (imageBitmap != null) {
                message[KEY_IMAGE] = encodedImage
            }

            database.collection(KEY_COLLECTION_CHAT)
                .add(message)
                .addOnSuccessListener { documentReference ->
                    if (conversationId != null) {
                        updateConversation(binding.inputMessage.text.toString())
                    } else {
                        val conversation = hashMapOf(
                            KEY_SENDER_ID to preferenceManager.getString(KEY_USER_ID),
                            KEY_SENDER_NAME to preferenceManager.getString(KEY_NAME),
                            KEY_SENDER_IMAGE to preferenceManager.getString(KEY_IMAGE),
                            KEY_RECEIVER_ID to receiverUser.id,
                            KEY_RECEIVER_NAME to receiverUser.name,
                            KEY_RECEIVER_IMAGE to receiverUser.image,
                            KEY_LAST_MESSAGE to binding.inputMessage.text.toString(),
                            KEY_TIMESTAMP to FieldValue.serverTimestamp()
                        )
                        addConversation(conversation)
                    }
                    binding.inputMessage.text = null
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    Toast.makeText(this, "Failed to send message: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun listenMessages() {
        database.collection(KEY_COLLECTION_CHAT)
            .whereEqualTo(KEY_SENDER_ID, preferenceManager.getString(KEY_USER_ID))
            .whereEqualTo(KEY_RECEIVER_ID, receiverUser.id)
            .addSnapshotListener(eventListener)

        database.collection(KEY_COLLECTION_CHAT)
            .whereEqualTo(KEY_SENDER_ID, receiverUser.id)
            .whereEqualTo(KEY_RECEIVER_ID, preferenceManager.getString(KEY_USER_ID))
            .addSnapshotListener(eventListener)
    }

    private val eventListener = EventListener<QuerySnapshot> { value, error ->
        if (error != null) {
            return@EventListener
        }
        if (value != null) {
            val count = chatMessages.size

            for (documentChange in value.documentChanges) {
                val timestamp = documentChange.document.getDate(KEY_TIMESTAMP)
                if (documentChange.type == DocumentChange.Type.ADDED) {
                    var chatMessage = ChatMessage("","","","","",null,"","","")
                    chatMessage.senderId = documentChange.document.getString(KEY_SENDER_ID)?:""
                    chatMessage.receiverId = documentChange.document.getString(KEY_RECEIVER_ID)?:""
                    chatMessage.message = documentChange.document.getString(KEY_MESSAGE)?:""
                    val imagebitmap=documentChange.document.getString(KEY_IMAGE_BITMAP)?:""
                    if(timestamp != null) {
                        chatMessage.dateTime =
                            getReadableDateTime(timestamp)
                    }
                    if(imagebitmap != null) {
                        chatMessage.imageBitmap =
                            imagebitmap
                    }
                    chatMessage.dateObject = documentChange.document.getDate(KEY_TIMESTAMP)
                    chatMessages.add(chatMessage)
                }
            }
            chatMessages.sortBy { it.dateObject }
            if (count == 0) {
                chatAdapter.notifyDataSetChanged()
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size, chatMessages.size)
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size - 1)
            }
            binding.chatRecyclerView.visibility = View.VISIBLE
        }
        binding.progressBar.visibility = View.GONE
        if(conversationId == null){
            checkForConversation()
        }
    }

    private fun getBitmapFromEncodedString(encodedImage: String): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun loadReceiverDetails() {
        receiverUser = intent.getSerializableExtra(KEY_USER) as User
        binding.textName.text = receiverUser.name
    }

    private fun setListeners() {
        binding.imageBack.setOnClickListener { onBackPressed() }
        binding.layoutSend.setOnClickListener{
            sendMessage()
        }

        binding.layoutAttachment.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImage.launch(intent)
        }

        binding.imageinfo.setOnClickListener {
            if(KEY_ADMIN_ID==preferenceManager.getString(KEY_USER_ID) ){

            val intent = Intent(this, ContactDetailsActivity::class.java)
            intent.putExtra(KEY_USER, receiverUser)
            startActivity(intent)
        }
        }
    }

    private fun getReadableDateTime(date: Date): String {
        val dateFormat = SimpleDateFormat("MMMM dd, yy - hh:mm a", Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun addConversation(conversation: HashMap<String, Any?> ) {
        database.collection(KEY_COLLECTION_CONVERSATIONS)
            .add(conversation)
            .addOnSuccessListener{ documentReference -> conversationId = documentReference.id }
    }

    private fun updateConversation(message: String) {
        val documentReference = database.collection(KEY_COLLECTION_CONVERSATIONS).document(
            conversationId.toString()
        )
        val updateData = hashMapOf(
            KEY_LAST_MESSAGE to message,
            KEY_TIMESTAMP to Date()
        )
        documentReference.update(updateData as Map<String, Any>)
    }
    private fun checkForConversationRemotely(senderId: String, receiverId: String) {
        database.collection(KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(KEY_SENDER_ID,senderId)
            .whereEqualTo(KEY_RECEIVER_ID, receiverId)
            .get()
            .addOnCompleteListener(conversationOnCompleteListener)
    }
    private fun checkForConversation() {
        val userId = preferenceManager.getString(KEY_USER_ID) ?: return

        if (chatMessages.size != 0) {
            checkForConversationRemotely(
                userId,
                receiverUser.id
            )
            checkForConversationRemotely(
                receiverUser.id,
                userId
            )
        }
    }

    private fun encodeImage(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (result.data != null) {
                val imageUri = result.data?.data
                try {
                    val inputStream: InputStream? = contentResolver.openInputStream(imageUri!!)
                    if (inputStream != null) {
                        imageBitmap = BitmapFactory.decodeStream(inputStream)
                        encodedImage = encodeImage(imageBitmap!!)
                        // Now, call the function to send the message with the image
                        sendMessageWithImage(encodedImage)
                        imageBitmap = null
                        encodedImage = null
                        binding.inputMessage.text = null
                        Toast.makeText(this, "Image bitmap decoded successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        // Handle the case where inputStream is null
                        Toast.makeText(this, "Failed to open image", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

    val conversationOnCompleteListener = OnCompleteListener<QuerySnapshot> { task ->
        if (task.isSuccessful && task.result != null && task.result!!.documents.size > 0) {
            val documentSnapshot: DocumentSnapshot = task.result!!.documents[0]
            conversationId = documentSnapshot.id
        }
    }
}

// Constants
val KEY_ADMIN_ID="Aq68Y0Xe19BGYkwlCRzZ"


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
val KEY_IMAGE_BITMAP="imageBitmap"


val KEY_COLLECTION_CONVERSATIONS ="conversations"
val KEY_SENDER_NAME ="senderName"
val KEY_RECEIVER_NAME ="receiverName"
val KEY_SENDER_IMAGE ="senderImage"
val KEY_RECEIVER_IMAGE ="receiverImage"
val KEY_LAST_MESSAGE ="lastMessage"
