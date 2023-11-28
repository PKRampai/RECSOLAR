package com.example.recsolar.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.recsolar.R
import com.example.recsolar.adapters.UsersAdapter
import com.example.recsolar.databinding.ActivitySignInBinding
import com.example.recsolar.models.User
import com.example.recsolar.utilities.Constants
import com.example.recsolar.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.appcompat.app.AppCompatDelegate
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec


class SignInActivity : AppCompatActivity() {

    var binding: ActivitySignInBinding? = null
    private lateinit var preferenceManager: PreferenceManager
    var OperatorEmail="test@gmail.com"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        preferenceManager =  PreferenceManager(applicationContext)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setListeners()

    }
    // Function to decrypt a string
    fun decryptString(encryptedInput: String?): String? {
        val KEY_ALIAS = "Rec_Key_Alias"  // Same key alias used for encryption

        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            // Get the key
            val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey

            // Decode the base64 string
            val combined = Base64.decode(encryptedInput, Base64.DEFAULT)

            // Separate IV and encrypted data
            val ivSize = 12  // GCM recommended IV size
            val iv = ByteArray(ivSize)
            val encryptedBytes = ByteArray(combined.size - ivSize)

            System.arraycopy(combined, 0, iv, 0, ivSize)
            System.arraycopy(combined, ivSize, encryptedBytes, 0, encryptedBytes.size)

            // Decrypt the data
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            // Decrypt and convert to string
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
    fun isValidSignUpDetails(): Boolean {
        if (binding?.inputEmail?.text.toString().trim().isEmpty()) {
            showToast("Enter Email")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding?.inputEmail?.text.toString()).matches()) {
            showToast("Enter valid email")
            return false
        } else if (binding?.inputPassword?.text.toString().trim().isEmpty()) {
            showToast("Enter Password")
            return false
        } else {
            return true
        }
    }

    private fun loading(isLoading:Boolean) {
        // Your signUp logic goes here

        if(isLoading){
            binding?.buttonSignIn?.visibility=(View.INVISIBLE)
            binding?.progressBar?.visibility=(View.VISIBLE)

        }else{
            binding?.buttonSignIn?.visibility=(View.VISIBLE)
            binding?.progressBar?.visibility=(View.INVISIBLE)
        }
    }

    private fun signIn() {
        loading(isLoading = true)
        val enteredEmail = binding?.inputEmail?.text.toString().lowercase()

        val database = FirebaseFirestore.getInstance()
        database.collection(KEY_COLLECTION_USERS)
            .whereEqualTo(KEY_EMAIL, enteredEmail)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null && task.result!!.documents.size > 0) {
                    val documentSnapshot = task.result!!.documents[0]

                    // Get the encrypted password from the database
                    val encryptedPassword = documentSnapshot.getString(KEY_PASSWORD)

                    // Decrypt the password
                    val decryptedPassword = decryptString(encryptedPassword)

                    // Compare the entered password with the decrypted one
                    if (binding?.inputPassword?.text.toString() == encryptedPassword) {
                        // Passwords match, sign in successful
                        preferenceManager.putBoolean(KEY_IS_SIGNED_IN, true)
                        preferenceManager.putString(KEY_USER_ID, documentSnapshot.id)
                        preferenceManager.putString(KEY_NAME, documentSnapshot.getString(KEY_NAME).toString())
                        preferenceManager.putString(KEY_IMAGE, documentSnapshot.getString(KEY_IMAGE).toString())

                        // Start the MainActivity
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    } else {
                        // Passwords don't match
                        loading(isLoading = false)
                        showToast("Incorrect password")
                    }
                } else {
                    // User not found
                    loading(isLoading = false)
                    showToast("Unable to sign in. User not found.")
                }
            }
    }


//    private fun signIn() {
//        loading(isLoading = true)
//        var enteredEmail =binding?.inputEmail?.text.toString().lowercase().toString()
//
//        val database = FirebaseFirestore.getInstance()
//        database.collection(KEY_COLLECTION_USERS)
//            .whereEqualTo(KEY_EMAIL, binding?.inputEmail?.text.toString())
//            .whereEqualTo(KEY_PASSWORD, binding?.inputPassword?.text.toString())
//            .get()
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful && task.result != null && task.result!!.documents.size > 0) {
//                    val documentSnapshot = task.result!!.documents[0]
//                    preferenceManager.putBoolean(KEY_IS_SIGNED_IN, true)
//                    preferenceManager.putString(KEY_USER_ID, documentSnapshot.id)
//                    preferenceManager.putString(KEY_NAME, documentSnapshot.getString(KEY_NAME).toString())
//                    preferenceManager.putString(KEY_IMAGE,documentSnapshot.getString(KEY_IMAGE).toString()
//                    )
//
////                    if(binding?.inputEmail?.text.toString()==OperatorEmail){
//                        val intent = Intent(applicationContext, MainActivity::class.java)
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                        startActivity(intent)
//
////                    }
////                else{
////                        database.collection(KEY_COLLECTION_USERS)
////                            .whereEqualTo(KEY_EMAIL, OperatorEmail)
////                            .get()
////                            .addOnCompleteListener { task ->
////                                loading(false)
////                                val currentUserId = preferenceManager.getString(KEY_USER_ID)
////                                if (task.isSuccessful && task.result != null) {
////                                    val users = ArrayList<User>()
////                                    for (queryDocumentSnapshot in task.result) {
////                                        if (currentUserId == queryDocumentSnapshot.id) {
////                                            continue
////                                        }
////                                        val user = User("","","","","","","")
////                                        user.name = queryDocumentSnapshot.getString(KEY_NAME)?:""
////                                        user.email = queryDocumentSnapshot.getString(KEY_EMAIL)?:""
////                                        user.image = queryDocumentSnapshot.getString(KEY_IMAGE)?: ""
////                                        user.token = queryDocumentSnapshot.getString(KEY_FCM_TOKEN)?:""
////                                        user.id = queryDocumentSnapshot.getId()
////                                        users.add(user)
////                                        val intent = Intent(applicationContext, ChatActivity::class.java)
////                                        intent.putExtra(KEY_USER, user)
////                                        startActivity(intent)
////                                        break
////                                    }
////
////                                } else {
////                                }
////
////                            }
////
////                    }
//
//
//
//                } else {
//                    loading(isLoading = false)
//                    showToast("Unable to sign in")
//                }
//            }
//    }

    private fun setListeners() {

        binding?.textCreateNewAccount?.setOnClickListener {
            startActivity(Intent(applicationContext, SignUpActivity::class.java))
        }
        binding?.buttonSignIn?.setOnClickListener{

            if(isValidSignUpDetails()){
                signIn()
            }
        }
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