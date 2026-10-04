package com.example.gymdiary3.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import com.example.gymdiary3.ui.design.Gd
import com.example.gymdiary3.ui.theme.GymDiaryTheme
import com.example.gymdiary3.ui.theme.LocalReducedMotion
import com.github.takahirom.roborazzi.captureRoboImage
import java.io.File

/** Phone-sized virtual device used by every screenshot test. */
const val PHONE = "w412dp-h915dp-xxhdpi"

/** Tall variant for reviewing a whole scrolling screen in one image. */
const val PHONE_TALL = "w412dp-h2200dp-xxhdpi"

private fun shotFile(name: String): String {
    val dir = System.getProperty("screenshots.dir") ?: "build/screenshots"
    File(dir).mkdirs()
    return File(dir, "$name.png").absolutePath
}

/**
 * Renders [content] the way the app does (theme + page background), with reduced
 * motion on so every capture shows final, settled frames, then saves a PNG.
 */
fun ComposeContentTestRule.snap(name: String, content: @Composable () -> Unit) {
    setContent {
        GymDiaryTheme {
            CompositionLocalProvider(LocalReducedMotion provides true) {
                Box(Modifier.fillMaxSize().background(Gd.Bg)) { content() }
            }
        }
    }
    waitForIdle()
    onRoot().captureRoboImage(shotFile(name))
}
