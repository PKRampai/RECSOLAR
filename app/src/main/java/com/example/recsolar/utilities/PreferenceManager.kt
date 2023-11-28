package com.example.recsolar.utilities

import android.content.Context
import android.content.SharedPreferences
import com.example.recsolar.utilities.Constants


//Adapted from:Youtube
//profile:https://www.youtube.com/@chiragkachhadiya
//Date:18 July 2021 - 8 Aug 2021
//Link:https://www.youtube.com/watch?v=ENK4ONrRm8s&list=PLam6bY5NszYOhXkY7jOS4EQAKcQwkXrp4&pp=iAQB
class PreferenceManager(context: Context) {
    public val KEY_PREFERENCE_NAME = "chatAppPreference"

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(KEY_PREFERENCE_NAME, Context.MODE_PRIVATE)

    fun putBoolean(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun putString(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun clear() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

}
