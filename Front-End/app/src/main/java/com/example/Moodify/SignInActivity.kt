package com.example.Moodify

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var rememberMeCheckBox: CheckBox
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var signUpLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("MoodifyPrefs", MODE_PRIVATE)

        emailEditText = findViewById(R.id.et_email)
        passwordEditText = findViewById(R.id.password)
        loginButton = findViewById(R.id.LoginButton)
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox)
        signUpLink = findViewById(R.id.signUpLink) // ✅ Get Sign-Up link TextView

        // 🔥 Check if user is already logged in
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            startActivity(Intent(this, MainMenuActivity::class.java))
            finish()
        }

        // ✅ Set Click Listener for "Sign Up" link
        signUpLink.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val rememberMe = rememberMeCheckBox.isChecked

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password, rememberMe)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, password: String, rememberMe: Boolean) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    if (rememberMe) {
                        sharedPreferences.edit {
                            putBoolean("isLoggedIn", true)
                        }
                    }
                    Toast.makeText(this, "Logged in successfully!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainMenuActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}
