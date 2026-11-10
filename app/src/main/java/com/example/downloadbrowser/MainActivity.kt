package com.example.downloadbrowser

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var urlInput: EditText
    private lateinit var btnGo: Button
    private lateinit var txtDownloadStatus: TextView
    private lateinit var downloadProgress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        urlInput = findViewById(R.id.urlInput)
        btnGo = findViewById(R.id.btnGo)
        txtDownloadStatus = findViewById(R.id.txtDownloadStatus)
        downloadProgress = findViewById(R.id.downloadProgress)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadsImagesAutomatically = true
        webView.webViewClient = WebViewClient()

        webView.setDownloadListener { url, userAgent, contentDisposition, mimetype, _ ->
            val filename = URLUtil.guessFileName(url, contentDisposition, mimetype)
            try {
                val request = DownloadManager.Request(Uri.parse(url)).apply {
                    setMimeType(mimetype)
                    addRequestHeader("User-Agent", userAgent)
                    setTitle(filename)
                    setDescription("Downloading via Free Download Pro...")
                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
                    allowScanningByMediaScanner()
                }

                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                dm.enqueue(request)

                txtDownloadStatus.text = "Downloading: $filename"
                Toast.makeText(this, "Started ADM Download: $filename", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        btnGo.setOnClickListener {
            var url = urlInput.text.toString().trim()
            if (url.isNotEmpty()) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://www.google.com/search?q=$url"
                }
                webView.loadUrl(url)
            }
        }

        webView.loadUrl("https://www.google.com")
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
