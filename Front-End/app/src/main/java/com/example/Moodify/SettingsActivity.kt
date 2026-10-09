package com.example.Moodify
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        auth = FirebaseAuth.getInstance()

        emailEditText = findViewById(R.id.layout_change_email)
        passwordEditText = findViewById(R.id.layout_change_password)
        saveButton = findViewById(R.id.btnSave)

        // Pre-fill with current user info
        val currentUser = auth.currentUser
        if (currentUser != null) {
            emailEditText.setText(currentUser.email)
        }

        saveButton.setOnClickListener {
            val newEmail = emailEditText.text.toString().trim()
            val newPassword = passwordEditText.text.toString().trim()

            if (newEmail.isNotEmpty()) {
                currentUser?.verifyBeforeUpdateEmail(newEmail)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Email updated!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to update email", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            if (newPassword.isNotEmpty()) {
                currentUser?.updatePassword(newPassword)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password updated!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
