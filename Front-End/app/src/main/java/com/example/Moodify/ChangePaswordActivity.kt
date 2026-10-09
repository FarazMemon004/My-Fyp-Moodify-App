package com.example.Moodify
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var etOldPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnConfirm: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        auth = FirebaseAuth.getInstance()

        // Initialize Views
        etOldPassword = findViewById(R.id.etOldPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnConfirm = findViewById(R.id.btnConfirm)

        btnConfirm.setOnClickListener {
            changePassword()
        }
    }

    private fun changePassword() {
        val oldPassword = etOldPassword.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        if (user != null && user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)

            // Re-authenticate the user
            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Change Password
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, MainMenuActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this, "Error: ${updateTask.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Old password is incorrect", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
