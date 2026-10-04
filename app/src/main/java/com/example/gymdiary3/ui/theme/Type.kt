package com.example.gymdiary3.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.gymdiary3.R

/**
 * Brand typeface — Archivo (SIL OFL), one variable font driving two families:
 *  - [Archivo] (width 100) for all text and UI.
 *  - [ArchivoExpanded] (width 125) for the single hero number on a screen.
 */
@OptIn(ExperimentalTextApi::class)
private fun archivo(weight: FontWeight, width: Float = 100f) = Font(
    resId = R.font.archivo_variable,
    weight = weight,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weight.weight),
        FontVariation.width(width),
    ),
)

val Archivo = FontFamily(
    archivo(FontWeight.Normal),
    archivo(FontWeight.Medium),
    archivo(FontWeight.SemiBold),
    archivo(FontWeight.Bold),
)

val ArchivoExpanded = FontFamily(
    archivo(FontWeight.Medium, 125f),
    archivo(FontWeight.SemiBold, 125f),
)

private const val TABULAR = "tnum"

/**
 * The v2 type scale (replica/design/tokens.json "type"). Sentence case by default;
 * [overline] is the only uppercase style and is for tiny status/meta labels.
 */
object GdType {
    val hero = TextStyle(
        fontFamily = ArchivoExpanded, fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-0.5).sp,
        fontFeatureSettings = TABULAR
    )
    val title = TextStyle(
        fontFamily = Archivo, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.4).sp
    )
    val metric = TextStyle(
        fontFamily = Archivo, fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp, lineHeight = 26.sp, letterSpacing = (-0.2).sp,
        fontFeatureSettings = TABULAR
    )
    val section = TextStyle(
        fontFamily = Archivo, fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp, lineHeight = 22.sp
    )
    val bodyStrong = TextStyle(
        fontFamily = Archivo, fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp, lineHeight = 22.sp
    )
    val body = TextStyle(
        fontFamily = Archivo, fontWeight = FontWeight.Normal,
        fontSize = 15.sp, lineHeight = 22.sp
    )
    val label = TextStyle(
        fontFamily = Archivo, fontWeight = FontWeight.Medium,
        fontSize = 13.sp, lineHeight = 18.sp
    )
    val labelNum = label.copy(fontFeatureSettings = TABULAR)
    val meta = TextStyle(
        fontFamily = Archivo, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp
    )
    val metaNum = meta.copy(fontFeatureSettings = TABULAR)
    val overline = TextStyle(
        fontFamily = Archivo, fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.8.sp
    )
    /** Large numeric input (weight/reps while logging). */
    val input = TextStyle(
        fontFamily = Archivo, fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp, lineHeight = 30.sp, fontFeatureSettings = TABULAR
    )
}

/** Material slots mapped onto the v2 scale so stock components inherit it. */
val Typography = Typography(
    displayLarge = GdType.hero,
    displayMedium = GdType.hero,
    displaySmall = GdType.title,
    headlineLarge = GdType.title,
    headlineMedium = GdType.title.copy(fontSize = 24.sp, lineHeight = 30.sp),
    headlineSmall = GdType.metric,
    titleLarge = GdType.section,
    titleMedium = GdType.bodyStrong,
    titleSmall = GdType.label.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = GdType.body,
    bodyMedium = GdType.label,
    bodySmall = GdType.meta,
    labelLarge = GdType.bodyStrong,
    labelMedium = GdType.label,
    labelSmall = GdType.meta,
)
