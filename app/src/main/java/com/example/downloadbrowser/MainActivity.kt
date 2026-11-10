package com.example.downloadbrowser

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var etUrl: EditText
    private lateinit var btnStartDownload: Button
    private lateinit var tvStatus: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgressDetails: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etUrl = findViewById(R.id.etUrl)
        btnStartDownload = findViewById(R.id.btnStartDownload)
        tvStatus = findViewById(R.id.tvStatus)
        progressBar = findViewById(R.id.progressBar)
        tvProgressDetails = findViewById(R.id.tvProgressDetails)

        btnStartDownload.setOnClickListener {
            val url = etUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                startDownloadTask(url)
            } else {
                Toast.makeText(this, "Please enter a valid link!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startDownloadTask(url: String) {
        tvStatus.text = "Downloading..."
        btnStartDownload.isEnabled = false

        val destinationDir = getExternalFilesDir(null) ?: filesDir
        val fileName = "downloaded_file_" + System.currentTimeMillis()

        val downloader = MultiThreadDownloader(
            fileUrl = url,
            saveDir = destinationDir,
            fileName = fileName,
            numThreads = 4,
            onProgress = { progress, speed ->
                runOnUiThread {
                    progressBar.progress = progress
                    tvProgressDetails.text = "$progress% • $speed KB/s"
                }
            },
            onComplete = { file ->
                runOnUiThread {
                    tvStatus.text = "Download Complete!"
                    btnStartDownload.isEnabled = true
                    Toast.makeText(this, "Saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                }
            },
            onError = { error ->
                runOnUiThread {
                    tvStatus.text = "Error occurred"
                    btnStartDownload.isEnabled = true
                    Toast.makeText(this, "Failed: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )

        downloader.start()
    }
}
