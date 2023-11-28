package com.example.recsolar.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Base64
import android.widget.Toast
import com.example.recsolar.R
import com.example.recsolar.databinding.ActivityContactDetailsBinding
import com.example.recsolar.models.User
import com.google.firebase.firestore.FirebaseFirestore

class ContactDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityContactDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding?.imageBack?.setOnClickListener { onBackPressed() }

        // Retrieve user information from intent
        val user = intent.getSerializableExtra(KEY_USER) as User

        val database = FirebaseFirestore.getInstance()
        database.collection(KEY_COLLECTION_USERS)
            .whereEqualTo(KEY_EMAIL, user.email)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null && task.result!!.documents.size > 0) {
                    val documentSnapshot = task.result!!.documents[0]


                    user.name= documentSnapshot.getString(KEY_NAME).toString()
                    user.image=documentSnapshot.getString(KEY_IMAGE).toString()
                    user.email=documentSnapshot.getString(KEY_EMAIL).toString()
                    user.system=documentSnapshot.getString(KEY_SYSTEMTYPE).toString()
                    user.address=documentSnapshot.getString(KEY_ADDRESS).toString()

                    binding.textName.text = user.name
                    binding.textEmail.text = Editable.Factory.getInstance().newEditable(user.email)
                    binding.textAddress.text = Editable.Factory.getInstance().newEditable(user.address)
                    binding.textSystem.text = Editable.Factory.getInstance().newEditable(user.system)
                    binding.imageProfile.setImageBitmap(getUserImage(user.image))
                    showToast(user.name)

                }


            }


        if(user.email==""){

            database.collection(KEY_COLLECTION_USERS)
                .document(user.id)
                .get()
                .addOnCompleteListener {
                        task ->
                    if (task.isSuccessful) {
                        val document = task.result

                        if (document != null && document.exists()) {
                            // Retrieve data from the document and set it to the user object
                            user.name = document.getString(KEY_NAME).toString()
                            user.image = document.getString(KEY_IMAGE).toString()
                            user.email = document.getString(KEY_EMAIL).toString()
                            user.system = document.getString(KEY_SYSTEMTYPE).toString()
                            user.address = document.getString(KEY_ADDRESS).toString()

                            binding.textName.text = user.name
                            binding.textEmail.text = Editable.Factory.getInstance().newEditable(user.email)
                            binding.textAddress.text = Editable.Factory.getInstance().newEditable(user.address)
                            binding.textSystem.text = Editable.Factory.getInstance().newEditable(user.system)
                            binding.imageProfile.setImageBitmap(getUserImage(user.image))
                        }
                    }

                }


        }

    }
    private fun getUserImage(encodedImage: String): Bitmap {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}