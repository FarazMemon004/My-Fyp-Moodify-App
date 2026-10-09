package com.example.Moodify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var signUpButton: Button
    private lateinit var signInText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)


        auth = FirebaseAuth.getInstance()

        database = FirebaseDatabase.getInstance().getReference("Users")


        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        signUpButton = findViewById(R.id.signUpButton)
        signInText = findViewById(R.id.signInLink)


        signUpButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (validateInputs(username, email, password, confirmPassword)) {
                registerUser(username, email, password)
            }
        }


        signInText.setOnClickListener {
            signInLink(it)
        }
    }

    private fun validateInputs(username: String, email: String, password: String, confirmPassword: String): Boolean {
        if (username.isEmpty()) {
            showToast("Username is required")
            return false
        }
        if (email.isEmpty()) {
            showToast("Email is required")
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Enter a valid email")
            return false
        }
        if (password.length < 6) {
            showToast("Password must be at least 6 characters")
            return false
        }
        if (password != confirmPassword) {
            showToast("Passwords do not match")
            return false
        }
        return true
    }

    private fun registerUser(username: String, email: String, password: String) {
        signUpButton.isEnabled = false
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                signUpButton.isEnabled = true
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        saveUserToDatabase(user.uid, username, email)
                        sendEmailVerification(user)
                    }
                } else {
                    Log.e("SignUpActivity", "Sign-Up Failed", task.exception)
                    showToast("Sign-Up Failed: ${task.exception?.message}")
                }
            }
    }

    private fun saveUserToDatabase(userId: String, username: String, email: String) {
        val user = mapOf(
            "username" to username,
            "email" to email
        )

        database.child(userId).setValue(user)
            .addOnSuccessListener {
                Log.d("SignUpActivity", "User data saved successfully")
            }
            .addOnFailureListener {
                Log.e("SignUpActivity", "Failed to save user data", it)
            }
    }

    private fun sendEmailVerification(user: com.google.firebase.auth.FirebaseUser) {
        user.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showToast("Verification Email Sent! Please check your inbox.")
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
            } else {
                Log.e("SignUpActivity", "Email Verification Failed", task.exception)
                showToast("Failed to send email verification.")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun signInLink(view: View) {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }
}
