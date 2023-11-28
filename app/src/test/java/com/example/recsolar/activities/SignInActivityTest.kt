package com.example.recsolar.activities

import android.widget.EditText
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
class SignInActivityTest {

    private lateinit var signInActivity: SignInActivity

    @Before
    fun setUp() {
        // Initialize the activity manually to properly set up the environment
        signInActivity = Robolectric.buildActivity(SignInActivity::class.java)
            .create()
            .start()
            .resume()
            .get()

        // Set up Robolectric to execute tasks posted to the main looper
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
    }

    @Test
    fun isValidSignUpDetails_validDetails_returnsTrue() {
        signInActivity.binding?.inputEmail?.setText("emoface@gmail.com")
        signInActivity.binding?.inputPassword?.setText("Password")

        assertTrue(signInActivity.isValidSignUpDetails())
    }

    @Test
    fun isValidSignUpDetails_invalidEmail_returnsFalse() {
        signInActivity.binding?.inputEmail?.setText("invalid-email")
        signInActivity.binding?.inputPassword?.setText("password123")

        assertFalse(signInActivity.isValidSignUpDetails())
    }

    @Test
    fun isValidSignUpDetails_emptyPassword_returnsFalse() {
        signInActivity.binding?.inputEmail?.setText("test6@example.com")
        signInActivity.binding?.inputPassword?.setText("")

        assertFalse(signInActivity.isValidSignUpDetails())
    }

    @Test
    fun isValidSignUpDetails_invalidEmailAndEmptyPassword_returnsFalse() {
        signInActivity.binding?.inputEmail?.setText("invalid-email")
        signInActivity.binding?.inputPassword?.setText("")

        assertFalse(signInActivity.isValidSignUpDetails())
    }
}
