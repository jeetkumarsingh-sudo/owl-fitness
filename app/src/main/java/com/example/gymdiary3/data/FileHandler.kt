package com.example.gymdiary3.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object FileHandler {
    fun writeToCache(context: Context, content: String): Uri? {
        return try {
            val fileName = "owl_fitness_export_${System.currentTimeMillis()}.csv"
            val file = File(context.cacheDir, fileName)
            file.writeText(content)
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
