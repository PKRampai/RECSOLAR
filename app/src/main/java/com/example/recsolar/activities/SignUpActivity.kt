package com.example.recsolar.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.recsolar.R
import com.example.recsolar.databinding.ActivitySignUpBinding
import com.example.recsolar.utilities.Constants
import com.example.recsolar.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec


class SignUpActivity : AppCompatActivity() {
    var binding: ActivitySignUpBinding? = null
    private lateinit var preferenceManager:PreferenceManager
    private lateinit var btnCapture : Button

    var sysBrand: String? = null
    var sysType : String?= null
    private lateinit var userSystem : String


    // Function to encrypt a string

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(getLayoutInflater())
        setContentView(binding!!.root)
        preferenceManager =  PreferenceManager(applicationContext)



        setListeners()

        btnCapture= findViewById(R.id.buttonSigUp)

        btnCapture.setOnClickListener {

            if(isValidSignUpDetails()){
                signUp()
            }
        }

    }

    private fun setListeners() {
        binding?.textSignIn?.setOnClickListener { onBackPressed() }
        binding?.layoutImage?.setOnClickListener { v ->
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }

        val brands = listOf("Sunsynk", "Kodak", "Growcol", "Deye", "Luxpower", "Other")

        val bTypes = listOf(
            "5kw Back-up","5kw Solar",
            "8kw Back-up","8kw Solar",
            "10kw Back-up","10KW Solar",
            "12kw Back-up","12kw Solar",
            "15kw Back-up","15kw Solar",
            "20kw Back-up","20kw Solar"
        )

        val brandAdapter= ArrayAdapter(this,R.layout.list_item,brands)

        binding?.inputSystemType?.setAdapter(brandAdapter)

        binding?.inputSystemType?.onItemClickListener = AdapterView.OnItemClickListener {
                adapterView, view, i, l ->
            val systemBrand = adapterView.getItemAtPosition(i).toString()
            showToast(systemBrand)
            sysBrand = systemBrand
//            userSystem = sysBrand
        }

        val typeAdapter= ArrayAdapter(this,R.layout.list_item,bTypes)

        binding?.inputSystemTypes?.setAdapter(typeAdapter)

        binding?.inputSystemTypes?.onItemClickListener = AdapterView.OnItemClickListener {
                adapterView, view, i, l ->
            val systemType = adapterView.getItemAtPosition(i).toString()
            showToast(systemType)
            sysType = systemType
//           userSystem = userSystem + " " + sysType
        }


    }

    fun showToast(message: String) {
        val context: Context = applicationContext ?: return

        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun signUp() {
        val enteredEmail = binding?.inputEmail?.text.toString().lowercase()

        // Check if the email already exists in the database
        val database = FirebaseFirestore.getInstance()
        database.collection(KEY_COLLECTION_USERS)
            .whereEqualTo(KEY_EMAIL, enteredEmail)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Email already exists, show an error message
                    loading(false)
                    showToast("Email already exists. Please use a different email.")
                } else {
                    // Email doesn't exist, proceed with user registration
                    val user = HashMap<String, Any>()
                    user[KEY_NAME] = binding?.inputName?.text.toString()
                    user[KEY_EMAIL] = enteredEmail
                    user[KEY_PASSWORD] = binding?.inputPassword?.text.toString()
                    user[KEY_ADDRESS] = binding?.inputAddress?.text.toString()
                    user[KEY_SYSTEMTYPE] = "$sysBrand : $sysType "
                    user[KEY_IMAGE] = this.encodedImage!!

                    database.collection(KEY_COLLECTION_USERS)
                        .add(user)
                        .addOnSuccessListener { documentReference ->
                            // On success
                            loading(false)

                            // Set the isSignedIn preference to true
                            preferenceManager.putBoolean(KEY_IS_SIGNED_IN, true)

                            // Store the user's ID, name, and encoded image in preferences
                            preferenceManager.putString(KEY_USER_ID, documentReference.id)
                            preferenceManager.putString(KEY_NAME, binding?.inputName?.text.toString())
                            preferenceManager.putString(KEY_IMAGE, this.encodedImage!!)

                            // Create an Intent to start the MainActivity
                            val intent = Intent(applicationContext, MainActivity::class.java)

                            // Add flags to clear the back stack
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                            // Start the MainActivity
                            startActivity(intent)

                        }
                        .addOnFailureListener { exception ->
                            // On failure
                            loading(false)
                            showToast(exception.message.toString())
                        }
                }
            }
            .addOnFailureListener { exception ->
                // On failure
                loading(false)
                showToast(exception.message.toString())
            }
    }


//    private fun signUp() {
//var enteredEmail =binding?.inputEmail?.text.toString().lowercase().toString()
//        Toast.makeText(this,"Working" , Toast.LENGTH_SHORT).show()
//        // Your signUp logic goes here
//        loading(true)
//
//        // Set loading to true
//        val database = FirebaseFirestore.getInstance()
//        val user = HashMap<String, Any>()
//        user[KEY_NAME] = binding?.inputName?.text.toString()
//        user[KEY_EMAIL] = enteredEmail
//        user[KEY_PASSWORD] = binding?.inputPassword?.text.toString()
//        user[KEY_ADDRESS]=binding?.inputAddress?.text.toString()
//        user[KEY_SYSTEMTYPE]=sysType+" "+sysBrand
//        user[KEY_IMAGE] = this.encodedImage!!
//
//        database.collection(KEY_COLLECTION_USERS)
//            .add(user)
//            .addOnSuccessListener { documentReference ->
//                // On success
//                // Set loading to false
//
//// Set the isSignedIn preference to true
//                preferenceManager.putBoolean(KEY_IS_SIGNED_IN, true)
//
//// Store the user's ID, name, and encoded image in preferences
//                preferenceManager.putString(KEY_USER_ID, documentReference.id)
//                preferenceManager.putString(KEY_NAME, binding?.inputName?.text.toString())
//                preferenceManager.putString(KEY_IMAGE, this.encodedImage!!)
//
//// Create an Intent to start the MainActivity
//                val intent = Intent(applicationContext, MainActivity::class.java)
//
//// Add flags to clear the back stack
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//
//// Start the MainActivity
//                startActivity(intent)
//
//            }
//            .addOnFailureListener { exception ->
//                // On failure
//                loading(false)
//                showToast(exception.message.toString())
//            }
//
//    }

    var encodedImage: String? = null

    private fun encodeImage(bitmap: Bitmap): String {
        val previewWidth = 150
        val previewHeight = (bitmap.height * previewWidth) / bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)

        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (result.data != null) {
                val imageUri = result.data?.data
                try {
                    val inputStream: InputStream? = contentResolver.openInputStream(imageUri!!)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    binding?.imageProfile?.setImageBitmap(bitmap)
                    binding?.textAddImage?.visibility = View.GONE
                    encodedImage = encodeImage(bitmap)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }


    fun isValidSignUpDetails(): Boolean {
        val context: Context = applicationContext ?: return false

        if(encodedImage==null){
            showToast("Please select a profile picture")
            return false

        }else if (binding?.inputName?.text.toString().trim().isEmpty()) {
            showToast("Enter Name")
            return false
        } else if (binding?.inputAddress?.text.toString().trim().isEmpty()) {
            showToast("Enter Address")
            return false

        } else if (binding?.inputEmail?.text.toString().trim().isEmpty()) {
            showToast("Enter Email")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding?.inputEmail?.text.toString()).matches()) {
            showToast("Enter valid email")
            return false
        } else if (binding?.inputPassword?.text.toString().trim().isEmpty()) {
            showToast("Enter Password")
            return false
        } else if (binding?.inputConfirmPassword?.text.toString().trim().isEmpty()) {
            showToast("Confirm your Password")
            return false
        } else if (binding?.inputPassword?.text.toString() != binding?.inputConfirmPassword?.text.toString()) {
            showToast("Confirm Password and Password must be the same")
            return false
        } else if (sysBrand==null || sysType==null) {
            showToast("select system information")
            return false
        } else {
            return true
        }
    }

    private fun loading(isLoading:Boolean) {
        if(isLoading){
            binding?.buttonSigUp?.visibility=(View.INVISIBLE)
            binding?.progressBar?.visibility=(View.VISIBLE)

        }else{
            binding?.buttonSigUp?.visibility=(View.VISIBLE)
            binding?.progressBar?.visibility=(View.INVISIBLE)
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