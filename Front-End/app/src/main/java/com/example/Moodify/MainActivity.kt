package com.example.Moodify

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Ensure this XML exists

        auth = Firebase.auth

        // Handle deep link from email verification
        val data: Uri? = intent?.data
        if (data != null) {
            handleSignInWithEmailLink(data.toString())
        } else {
            checkUserLoginStatus()
        }
    }

    private fun checkUserLoginStatus() {
        val user = auth.currentUser
        if (user != null && user.isEmailVerified) {
            startActivity(Intent(this, MainMenuActivity::class.java))
            finish()
        } else {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }

    private fun handleSignInWithEmailLink(emailLink: String) {
        val email = "user@example.com" // TODO: Retrieve email from SharedPreferences

        if (auth.isSignInWithEmailLink(emailLink)) {
            auth.signInWithEmailLink(email, emailLink)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Email Verified & Logged In!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainMenuActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Verification Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
