package com.example.recsolar.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.recsolar.R
import com.example.recsolar.adapters.UsersAdapter
import com.example.recsolar.databinding.ActivitySignUpBinding
import com.example.recsolar.databinding.ActivityUsersBinding
import com.example.recsolar.listeners.UserListener
import com.example.recsolar.models.User
import com.example.recsolar.utilities.Constants
import com.example.recsolar.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore

class UsersActivity : AppCompatActivity(), UserListener {

    private var binding: ActivityUsersBinding? = null
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        preferenceManager =  PreferenceManager(applicationContext)
        setListeners()
        getUsers()

    }

    private fun setListeners(){
        binding?.imageBack?.setOnClickListener { onBackPressed() }
    }

    private fun getUsers() {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(KEY_COLLECTION_USERS)
            .get()
            .addOnCompleteListener { task ->
                loading(false)
                val currentUserId = preferenceManager.getString(KEY_USER_ID)
                if (task.isSuccessful && task.result != null) {
                    val users = ArrayList<User>()
                    for (queryDocumentSnapshot in task.result) {
                        if (currentUserId == queryDocumentSnapshot.id) {
                            continue
                        }
                        val user = User("","","","","","","")
                        user.name = queryDocumentSnapshot.getString(KEY_NAME)?:""
                        user.email = queryDocumentSnapshot.getString(KEY_EMAIL)?:""
                        user.image = queryDocumentSnapshot.getString(KEY_IMAGE)?: ""
                        user.token = queryDocumentSnapshot.getString(KEY_FCM_TOKEN)?:""
                        user.id = queryDocumentSnapshot.getId()
                        if(KEY_ADMIN_ID==preferenceManager.getString(KEY_USER_ID) ){
                        users.add(user)
                        }else {
                            // If the current user is not equal to the operator ID,
                            // check if the user's email is equal to the operator ID
                            if (user.id == KEY_ADMIN_ID) {
                                // If true, add the user to the list
                                users.add(user)
                            }
                        }
                    }
                    if (users.isNotEmpty()) {
                        val userAdapter = UsersAdapter(users,this)
                        binding?.usersRecyclerview?.adapter = userAdapter
                        binding?.usersRecyclerview?.visibility = View.VISIBLE
                    } else {
                        showErrorMessage()
                    }
                } else {
                    showErrorMessage()
                }

                }
            }

    private fun showErrorMessage(){
        binding?.textErrorMessage?.text = String.format("%s","No User available")
        binding?.textErrorMessage?.visibility = View.VISIBLE
    }

    private fun loading(isLoading : Boolean){

        if(isLoading){
            binding?.progressBar?.visibility = View.VISIBLE
        }else{
            binding?.progressBar?.visibility = View.INVISIBLE
        }
    }



    override fun onUserClicked(user:User){
        val intent = Intent(this,ChatActivity::class.java)
        intent.putExtra(KEY_USER,user)
        startActivity(intent)
        finish()


    }

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

    val KEY_COLLECTION_CONVERSATIONS ="conversations"
    val KEY_SENDER_NAME ="senderName"
    val KEY_RECEIVER_NAME ="receiverName"
    val KEY_SENDER_IMAGE ="senderImage"
    val KEY_RECEIVER_IMAGE ="receiverImage"
    val KEY_LAST_MESSAGE ="lastMessage"
}