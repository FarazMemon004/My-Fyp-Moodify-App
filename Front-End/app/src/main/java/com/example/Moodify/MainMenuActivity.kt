package com.example.Moodify

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainMenuActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var hamburgerMenu: ImageView
    private lateinit var generateMusicButton: Button
    private var isSettingsExpanded = false // Track submenu state

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        // Initialize Views
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        hamburgerMenu = findViewById(R.id.hamburger)
        generateMusicButton = findViewById(R.id.btnGenerateMusic)

        // Ensure the navigation drawer opens when clicking the hamburger button
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.open_drawer, R.string.close_drawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Open navigation drawer only when clicking the hamburger menu
        hamburgerMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Handle Back Press using OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    finish() // Close the activity
                }
            }
        })

        // Handle Navigation Drawer Item Clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            handleNavigationItemClick(menuItem)
        }

        // Handle Generate Music Button Click
        generateMusicButton.setOnClickListener {
            startActivity(Intent(this, FacialRecognitionActivity::class.java))
        }
    }

    private fun handleNavigationItemClick(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.menu_change_email -> startActivity(Intent(this, ChangeEmailActivity::class.java))
            R.id.menu_change_password -> startActivity(Intent(this, ChangePasswordActivity::class.java))
            R.id.menu_logout -> {
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
            }
            R.id.menu_settings -> toggleSettingsSubMenu()

        }

        // Inside handleNavigationItemClick() function
        when (menuItem.itemId) {
            R.id.menu_logout -> {
                // Clear login state
                val sharedPreferences = getSharedPreferences("MoodifyPrefs", MODE_PRIVATE)
                sharedPreferences.edit() { clear() }

                // Logout user from Firebase
                FirebaseAuth.getInstance().signOut()

                // Redirect to Sign In screen
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun toggleSettingsSubMenu() {
        val menu = navigationView.menu
        isSettingsExpanded = !isSettingsExpanded // Toggle submenu visibility

        menu.findItem(R.id.menu_change_email).isVisible = isSettingsExpanded
        menu.findItem(R.id.menu_change_password).isVisible = isSettingsExpanded
    }
}
