package com.example.recsolar.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.example.recsolar.R
import com.example.recsolar.models.ImageData

class FullImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_image)

        val imageView: ImageView = findViewById(R.id.fullImageView)

        val imageData = intent.getParcelableExtra<ImageData>("image")
        if (imageData != null) {
            val bitmap = BitmapFactory.decodeByteArray(imageData.byteArray, 0, imageData.byteArray.size)
            imageView.setImageBitmap(bitmap)
        }
    }
}