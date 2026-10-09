package com.example.Moodify

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class GenerateMusicActivity : AppCompatActivity() {

    private lateinit var emotionTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var confirmEmotionButton: Button
    private lateinit var retryButton: Button
    private var detectedEmotion: String? = null

    // ✅ Update Flask URL with actual ngrok URL
    private val flaskUrl = "https://9c4e-45-199-187-139.ngrok-free.app/generate-music"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_generation)

        emotionTextView = findViewById(R.id.emotionTextView)
        progressBar = findViewById(R.id.progressBar)
        confirmEmotionButton = findViewById(R.id.confirmEmotionButton)
        retryButton = findViewById(R.id.retryButton)

        // ✅ Get detected emotion from intent
        detectedEmotion = intent.getStringExtra("emotion") ?: "Unknown"
        emotionTextView.text = "Detected Emotion: $detectedEmotion"

        // ✅ Confirm button to generate music
        confirmEmotionButton.setOnClickListener {
            detectedEmotion?.let { sendEmotionToServer(it) }
        }

        // ✅ Retry button to go back to facial recognition
        retryButton.setOnClickListener {
            restartFacialRecognition()
        }
    }

    private fun sendEmotionToServer(emotion: String) {
        // ✅ Show loading UI
        emotionTextView.text = "Generating music for: $emotion..."
        progressBar.visibility = View.VISIBLE
        confirmEmotionButton.visibility = View.GONE
        retryButton.visibility = View.GONE

        val json = JSONObject().put("emotion", emotion)
        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(flaskUrl)
            .post(requestBody)
            .build()

        val client = OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS) // ✅ Increase timeout for long processing
            .readTimeout(120, TimeUnit.SECONDS)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    showToast("Server Error: ${e.message}")
                    confirmEmotionButton.visibility = View.VISIBLE
                    retryButton.visibility = View.VISIBLE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("GenerateMusicActivity", "Server Response: $responseBody") // ✅ Debugging

                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(responseBody ?: "{}")
                    val status = jsonResponse.optString("status", "error")

                    if (status == "success") {
                        val midiFiles = jsonResponse.optJSONArray("midi_files") ?: JSONArray()
                        Log.d("GenerateMusicActivity", "MIDI Files: $midiFiles") // ✅ Debugging

                        // ✅ Move to Music Player Activity
                        navigateToMusicPlayer(midiFiles)
                    } else {
                        runOnUiThread {
                            progressBar.visibility = View.GONE
                            showToast("Music generation failed: ${jsonResponse.optString("message")}")
                            confirmEmotionButton.visibility = View.VISIBLE
                            retryButton.visibility = View.VISIBLE
                        }
                    }
                } else {
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        showToast("Error: Invalid response from server")
                        confirmEmotionButton.visibility = View.VISIBLE
                        retryButton.visibility = View.VISIBLE
                    }
                }
            }
        })
    }

    private fun restartFacialRecognition() {
        val intent = Intent(this, FacialRecognitionActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMusicPlayer(midiFiles: JSONArray) {
        if (midiFiles.length() == 0) {
            showToast("No MIDI files received!")
            finish()
            return
        }

        runOnUiThread {
            progressBar.visibility = View.GONE
            emotionTextView.text = "Music Generated!"

            val intent = Intent(this, MusicPlayerActivity::class.java)
            intent.putExtra("midi_files", midiFiles.toString()) // ✅ Send list of MIDI files
            startActivity(intent)
            finish()
        }
    }

    private fun showToast(message: String) {
        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
    }
}
