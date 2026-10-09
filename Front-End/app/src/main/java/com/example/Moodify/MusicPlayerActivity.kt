package com.example.Moodify

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MusicPlayerActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var playButton: Button
    private lateinit var downloadButton: Button
    private lateinit var mediaPlayer: MediaPlayer
    private var midiFiles: MutableList<String> = mutableListOf()
    private var selectedFile: String? = null

    // ✅ Flask Server Base URL (Change to match your server)
    private val flaskBaseUrl = "private val flaskUrl = \"https://9c4e-45-199-187-139.ngrok-free.app/generated_music/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)

        listView = findViewById(R.id.listView)
        playButton = findViewById(R.id.playButton)
        downloadButton = findViewById(R.id.downloadButton)
        mediaPlayer = MediaPlayer()

        // ✅ Retrieve MIDI file list from intent
        val midiJson = intent.getStringExtra("midi_files")
        if (!midiJson.isNullOrEmpty()) {
            midiFiles = parseMIDIJson(midiJson)
        } else {
            showToast("No MIDI files available!")
            return
        }

        // ✅ Show MIDI files in ListView
        val adapter = ArrayAdapter(this, R.layout.list_item, midiFiles)
        listView.adapter = adapter

        // ✅ Handle file selection
        listView.setOnItemClickListener { _, _, position, _ ->
            selectedFile = midiFiles[position]
        }

        // ✅ Play selected file
        playButton.setOnClickListener {
            selectedFile?.let { playMIDI(it) } ?: showToast("Please select a file to play")
        }

        // ✅ Download selected file
        downloadButton.setOnClickListener {
            selectedFile?.let { downloadMIDI(it) } ?: showToast("Please select a file to download")
        }
    }

    private fun parseMIDIJson(json: String): MutableList<String> {
        val jsonArray = JSONArray(json)
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            val fullUrl = jsonArray.getString(i)
            val fileName = fullUrl.substringAfterLast("/") // Extract filename from URL
            list.add(fileName)
        }
        return list
    }


    private fun playMIDI(fileName: String) {
        val fileUrl = "https://9c4e-45-199-187-139.ngrok-free.app/generated_music/$fileName"
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(fileUrl)
            mediaPlayer.prepareAsync()
            mediaPlayer.setOnPreparedListener { it.start() }
            showToast("Playing: $fileName")
        } catch (e: Exception) {
            showToast("Error playing MIDI file")
            Log.e("MusicPlayer", "Error playing MIDI", e)
        }
    }

    private fun downloadMIDI(fileName: String) {
        val fileUrl = "https://9c4e-45-199-187-139.ngrok-free.app/generated_music/$fileName"
        val request = Request.Builder().url(fileUrl).build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { showToast("Download failed: ${e.message}") }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    runOnUiThread { showToast("Server error: ${response.code}") }
                    return
                }

                val filePath = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), fileName)
                response.body?.byteStream()?.use { input ->
                    FileOutputStream(filePath).use { output ->
                        input.copyTo(output)
                    }
                }

                runOnUiThread { showToast("Downloaded: ${filePath.absolutePath}") }
            }
        })
    }

    private fun showToast(message: String) {
        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}
