package com.example.Moodify

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class ChangeEmailActivity : AppCompatActivity() {

    private lateinit var newEmailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var changeEmailButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_email)

        // Initialize views
        newEmailEditText = findViewById(R.id.newEmailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        changeEmailButton = findViewById(R.id.changeEmailButton)
        progressBar = findViewById(R.id.progressBar)
        errorTextView = findViewById(R.id.errorTextView)

        // Set click listener for the Change Email button
        changeEmailButton.setOnClickListener {
            val newEmail = newEmailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (newEmail.isEmpty() || password.isEmpty()) {
                errorTextView.text = "Please fill all fields."
            } else {
                changeEmail(newEmail, password)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun changeEmail(newEmail: String, password: String) {
        // Show progress bar
        progressBar.visibility = ProgressBar.VISIBLE
        errorTextView.text = ""

        // Simulate a network request to check if the email is available and verify the password
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Step 1: Verify password
                val passwordResponse = verifyPassword(password)
                if (!passwordResponse.isSuccessful || passwordResponse.body()?.isValid != true) {
                    runOnUiThread {
                        progressBar.visibility = ProgressBar.GONE
                        errorTextView.text = "Invalid password."
                    }
                    return@launch
                }

                // Step 2: Check if the new email is already in use
                val emailResponse = checkEmailAvailability(newEmail)
                if (!emailResponse.isSuccessful || emailResponse.body()?.isAvailable != true) {
                    runOnUiThread {
                        progressBar.visibility = ProgressBar.GONE
                        errorTextView.text = "Email is already in use."
                    }
                    return@launch
                }

                // Step 3: Update the email in the database
                val updateResponse = updateEmail(newEmail)
                if (updateResponse.isSuccessful && updateResponse.body()?.isUpdated == true) {
                    runOnUiThread {
                        progressBar.visibility = ProgressBar.GONE
                        Toast.makeText(this@ChangeEmailActivity, "Email updated successfully!", Toast.LENGTH_SHORT).show()
                        finish() // Close the activity
                    }
                } else {
                    runOnUiThread {
                        progressBar.visibility = ProgressBar.GONE
                        errorTextView.text = "Failed to update email."
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    progressBar.visibility = ProgressBar.GONE
                    errorTextView.text = "An error occurred. Please try again."
                    Log.e("ChangeEmailActivity", "Error: ${e.message}")
                }
            }
        }
    }

    private suspend fun verifyPassword(password: String): Response<PasswordVerificationResponse> {
        // Simulate a network request to verify the password
        return RetrofitClient.apiService.verifyPassword(PasswordVerificationRequest(password))
    }

    private suspend fun checkEmailAvailability(email: String): Response<EmailAvailabilityResponse> {
        // Simulate a network request to check if the email is available
        return RetrofitClient.apiService.checkEmailAvailability(EmailAvailabilityRequest(email))
    }

    private suspend fun updateEmail(email: String): Response<EmailUpdateResponse> {
        // Simulate a network request to update the email
        return RetrofitClient.apiService.updateEmail(EmailUpdateRequest(email))
    }
}