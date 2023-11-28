package com.example.recsolar.activities

import android.content.Context
import android.text.Editable
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE) // Disable manifest processing for unit tests
class SignUpActivityTest {

//    private val signUpActivity = SignUpActivity()
    private val signUpActivity = Robolectric.setupActivity(SignUpActivity::class.java)
    private val context: Context = ApplicationProvider.getApplicationContext()


    fun stringToEditable(input: String?): Editable {
        // If the input is null, return an empty Editable
        if (input == null) {
            return Editable.Factory.getInstance().newEditable("")
        }

        return Editable.Factory.getInstance().newEditable(input)
    }
    @Test
    fun isValidSignUpDetails_validDetails_returnsTrue() {
        // Set up valid details


        signUpActivity.encodedImage = "encodedImage"
        signUpActivity.sysBrand = "Sunsynk"
        signUpActivity.sysType = "5kw Solar"
        signUpActivity.binding?.inputName?.text = stringToEditable("John Doe")
        signUpActivity.binding?.inputAddress?.text = stringToEditable("123 Main St")
        signUpActivity.binding?.inputEmail?.text = stringToEditable("test6@example.com")
        signUpActivity.binding?.inputPassword?.text = stringToEditable("password123")
        signUpActivity.binding?.inputConfirmPassword?.text = stringToEditable("password123")

        // Call the method to test
        val isValid = signUpActivity.isValidSignUpDetails()

        // Assert that the result is true
        assertTrue(isValid)
    }

    @Test
    fun isValidSignUpDetails_invalidDetails_returnsFalse() {
        // Set up invalid details
        signUpActivity.encodedImage = null
        signUpActivity.sysBrand = null
        signUpActivity.sysType = null
        signUpActivity.binding?.inputName?.text = stringToEditable("")
        signUpActivity.binding?.inputAddress?.text = stringToEditable("")
        signUpActivity.binding?.inputEmail?.text = stringToEditable("invalidEmail")
        signUpActivity.binding?.inputPassword?.text = stringToEditable("password123")
        signUpActivity.binding?.inputConfirmPassword?.text = stringToEditable("password456")

        // Call the method to test
        val isValid = signUpActivity.isValidSignUpDetails()

        // Assert that the result is false
        assertFalse(isValid)
    }
}
