package com.example.recsolar.models

import android.graphics.Bitmap
import java.io.Serializable

data class User(
    var name: String,
    var image: String,
    var email: String,
    var token: String,
    var address: String,
    var system: String,
    var id:String
) : Serializable