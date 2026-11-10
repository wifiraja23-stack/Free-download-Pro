package com.example.downloadbrowser

import java.io.File
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MultiThreadDownloader(
    private val fileUrl: String,
    private val saveDir: File,
    private val fileName: String,
    private val numThreads: Int = 4,
    private val onProgress: (progress: Int, speedKbps: Long) -> Unit,
    private val onComplete: (outputFile: File) -> Unit,
    private val onError: (exception: Exception) -> Unit
) {

    fun start() {
        thread {
            try {
                val url = URL(fileUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "HEAD"
                val fileSize = connection.contentLengthLong
                connection.disconnect()

                if (fileSize <= 0) {
                    onError(Exception("Cannot determine file size"))
                    return@thread
                }

                val outputFile = File(saveDir, fileName)
                val outputFileAccess = RandomAccessFile(outputFile, "rw")
                outputFileAccess.setLength(fileSize)
                outputFileAccess.close()

                val partSize = fileSize / numThreads
                var downloadedBytes = 0L
                var activeThreads = numThreads
                val startTime = System.currentTimeMillis()

                for (i in 0 until numThreads) {
                    val startByte = i * partSize
                    val endByte = if (i == numThreads - 1) fileSize - 1 else (startByte + partSize - 1)

                    thread {
                        try {
                            val partConn = url.openConnection() as HttpURLConnection
                            partConn.setRequestProperty("Range", "bytes=$startByte-$endByte")
                            val inputStream = partConn.inputStream
                            val raf = RandomAccessFile(outputFile, "rw")
                            raf.seek(startByte)

                            val buffer = ByteArray(8192)
                            var bytesRead: Int
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                raf.write(buffer, 0, bytesRead)
                                synchronized(this) {
                                    downloadedBytes += bytesRead
                                    val currentTime = System.currentTimeMillis()
                                    val timeDiff = (currentTime - startTime) / 1000
                                    val speed = if (timeDiff > 0) (downloadedBytes / 1024) / timeDiff else 0
                                    val progress = ((downloadedBytes * 100) / fileSize).toInt()
                                    onProgress(progress, speed)
                                }
                            }

                            raf.close()
                            inputStream.close()
                            partConn.disconnect()
                        } catch (e: Exception) {
                            onError(e)
                        } finally {
                            synchronized(this) {
                                activeThreads--
                                if (activeThreads == 0) {
                                    onComplete(outputFile)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}
