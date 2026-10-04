package com.example.gymdiary3.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import com.example.gymdiary3.ui.design.ContentFrame
import com.example.gymdiary3.ui.design.Gd
import com.example.gymdiary3.ui.theme.GymDiaryTheme
import com.example.gymdiary3.ui.theme.LocalReducedMotion
import com.github.takahirom.roborazzi.captureRoboImage
import java.io.File

/** Phone-sized virtual device used by every screenshot test. */
const val PHONE = "w412dp-h915dp-xxhdpi"

/** Tall variant for reviewing a whole scrolling screen in one image. */
const val PHONE_TALL = "w412dp-h2200dp-xxhdpi"

/** Small phone (e.g. a 5" device); used with a large font scale to find truncation. */
const val PHONE_SMALL = "w360dp-h760dp-xhdpi"

/** Tablet portrait / phone landscape width class (Medium). */
const val WIDE = "w840dp-h1000dp-xhdpi"

private fun shotFile(name: String): String {
    val dir = System.getProperty("screenshots.dir") ?: "build/screenshots"
    File(dir).mkdirs()
    return File(dir, "$name.png").absolutePath
}

/**
 * Renders [content] the way the app does (theme + page background), with reduced
 * motion on so every capture shows final, settled frames, then saves a PNG.
 */
fun ComposeContentTestRule.snap(name: String, fontScale: Float = 1f, content: @Composable () -> Unit) {
    setContent {
        GymDiaryTheme {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalReducedMotion provides true,
                LocalDensity provides Density(density.density, fontScale)
            ) {
                Box(Modifier.fillMaxSize().background(Gd.Bg)) { ContentFrame { content() } }
            }
        }
    }
    waitForIdle()
    onRoot().captureRoboImage(shotFile(name))
}

/** Re-capture after an interaction (e.g. expanding a section), overwriting [name]. */
fun captureRoot(rule: ComposeContentTestRule, name: String) {
    rule.onRoot().captureRoboImage(shotFile(name))
}
