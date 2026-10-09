package com.example.Moodify

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FacialRecognitionActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var detectEmotionBtn: Button
    private lateinit var emotionTextView: TextView
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService

    private val flaskUrl = "https://9c4e-45-199-187-139.ngrok-free.app/detect-emotion" // Change to your Flask URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facial_recognition)

        previewView = findViewById(R.id.previewView)
        detectEmotionBtn = findViewById(R.id.detectEmotionBtn)
        emotionTextView = findViewById(R.id.emotionTextView)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Request Camera Permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        } else {
            startCamera()
        }

        detectEmotionBtn.setOnClickListener {
            captureImage() // Capture and analyze the frame
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider) // ✅ Live preview inside camera box
            }

            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureImage() {
        val photoFile = File(externalCacheDir, "temp.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    if (photoFile.exists()) {
                        val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                        detectEmotion(bitmap)
                    } else {
                        showToast("Failed to capture image!")
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    showToast("Capture failed: ${exception.message}")
                }
            }
        )
    }

    private fun detectEmotion(bitmap: Bitmap) {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val imageBytes = outputStream.toByteArray()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image", "photo.jpg",
                imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, imageBytes.size)
            )
            .build()

        val request = Request.Builder()
            .url(flaskUrl)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { showToast("Server error: ${e.message}") }
            }

            override fun onResponse(call: Call, response: Response) {
                val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                val emotion = jsonResponse.optString("emotion", "unknown")

                runOnUiThread {
                    emotionTextView.text = "Detected Emotion: $emotion"
                    showConfirmEmotionDialog(emotion)
                }
            }
        })
    }

    private fun showConfirmEmotionDialog(emotion: String) {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Confirm Emotion")
            .setMessage("Detected Emotion: $emotion\nDo you want to generate music?")
            .setPositiveButton("Yes") { _, _ ->
                sendEmotionToGenerateMusic(emotion)
            }
            .setNegativeButton("Retry") { _, _ ->
                emotionTextView.text = "Detecting Emotion..."
            }
            .setCancelable(false)
            .create()

        dialog.show()
    }

    private fun sendEmotionToGenerateMusic(emotion: String) {
        val intent = Intent(this, GenerateMusicActivity::class.java)
        intent.putExtra("emotion", emotion)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
