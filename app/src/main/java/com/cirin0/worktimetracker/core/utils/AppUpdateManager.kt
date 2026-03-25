package com.cirin0.worktimetracker.core.utils

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class AppUpdateManager(private val context: Context) {

    /**
     * Скачує APK файл і повертає прогрес скачування
     * @param downloadUrl URL для скачування APK
     * @return Flow з прогресом (0-100)
     */
    fun downloadApk(downloadUrl: String): Flow<UpdateProgress> = flow {
        withContext(Dispatchers.IO) {
            try {
                emit(UpdateProgress.Loading(0))

                val apkFile = getApkFile()
                val url = URL(downloadUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val fileLength = connection.contentLength
                val input = connection.inputStream
                val output = FileOutputStream(apkFile)

                val buffer = ByteArray(1024 * 10)
                var count: Int
                var downloaded: Long = 0

                while (input.read(buffer).also { count = it } != -1) {
                    output.write(buffer, 0, count)
                    downloaded += count
                    val progress = ((downloaded * 100) / fileLength).toInt()
                    emit(UpdateProgress.Loading(progress))
                }

                output.close()
                input.close()
                connection.disconnect()

                emit(UpdateProgress.Success(apkFile))
            } catch (e: Exception) {
                emit(UpdateProgress.Error(e.message ?: "Unknown error"))
            }
        }
    }

    /**
     * Встановлює APK файл
     */
    fun installApk(apkFile: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "com.cirin0.worktimetracker.provider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getApkFile(): File {
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?: throw Exception("Cannot access downloads directory")
        return File(downloadsDir, "app-update.apk")
    }
}

sealed class UpdateProgress {
    data class Loading(val progress: Int) : UpdateProgress()
    data class Success(val apkFile: File) : UpdateProgress()
    data class Error(val message: String) : UpdateProgress()
}


