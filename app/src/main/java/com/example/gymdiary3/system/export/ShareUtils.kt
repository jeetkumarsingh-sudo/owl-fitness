package com.example.gymdiary3.system.export

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.core.content.FileProvider
import com.example.gymdiary3.domain.model.SessionWithSets
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ShareUtils {
    fun captureView(view: View): Bitmap {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(view.resources.displayMetrics.widthPixels, View.MeasureSpec.AT_MOST)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(widthSpec, heightSpec)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun shareImage(context: Context, bitmap: Bitmap) {
        val file = File(context.cacheDir, "workout_summary.png")
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share Workout Image"))
    }

    fun shareText(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share Workout Text"))
    }

    fun buildShareText(sessionWithSets: SessionWithSets, unit: String): String {
        val sb = StringBuilder()
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

        sb.append("Owl Fitness Workout Summary\n")
        sb.append("---------------------------\n")
        sb.append("Date: ${sdf.format(Date(sessionWithSets.date))}\n")
        
        sb.append("Duration: ${sessionWithSets.duration / 60000} min\n\n")

        sessionWithSets.exercises.forEach { (exercise, sets) ->
            sb.append("$exercise\n")
            sets.forEach {
                sb.append("- Set ${it.setNumber}: ${it.weight}$unit x ${it.reps}\n")
            }
            sb.append("\n")
        }

        sb.append("---------------------------\n")
        sb.append("Total Volume: ${sessionWithSets.totalVolume.toInt()} $unit\n")
        sb.append("Total Sets: ${sessionWithSets.sets.size}\n")
        
        return sb.toString()
    }
}
