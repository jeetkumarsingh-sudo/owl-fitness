package com.example.gymdiary3.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Apex design-system tokens — "Carbon & Crimson".
 *
 * A single source of truth for color, spacing, radius and elevation so screens
 * compose from shared primitives instead of re-styling surfaces by hand. Legacy
 * [OwlColors] remains for screens not yet migrated; new/redesigned screens use [Apex].
 */
object Apex {

    // ---- Surfaces: layered carbon depth (darkest page → raised cards) ----
    val Base        = Color(0xFF000000) // true OLED black — page background
    val Surface1    = Color(0xFF0C0C0E) // faint lift off the base
    val Surface2    = Color(0xFF151518) // standard card
    val Surface3    = Color(0xFF1D1D21) // raised / inner card
    val Surface4    = Color(0xFF26262B) // controls, chips, pressed states

    // Translucent "glass" fills for layered panels over the atmosphere
    val Glass       = Color(0xB3151518) // ~70% Surface2
    val GlassStrong = Color(0xE617171B)

    // ---- Accent ramp: crimson ----
    val Accent       = Color(0xFFE11D48) // primary brand accent
    val AccentBright = Color(0xFFFB2C5A) // highlight / active
    val AccentDeep   = Color(0xFF9F1239) // pressed / dim
    val AccentSoft   = Color(0xFFF43F5E) // soft rose for secondary accents
    val AccentGlow   = Color(0x40E11D48) // radial atmosphere glow (25%)
    val AccentWash   = Color(0x14E11D48) // 8% tint fills behind accent content

    // ---- Semantic ----
    val Positive = Color(0xFF10B981) // gains / success / streak
    val Warning  = Color(0xFFF59E0B)
    val Negative = Color(0xFFEF4444)
    val Info     = Color(0xFF38BDF8)

    // ---- Text ----
    val TextPrimary   = Color(0xFFFAFAFA)
    val TextSecondary = Color(0xFFA1A1AA)
    val TextMuted     = Color(0xFF71717A)
    val TextFaint     = Color(0xFF52525B)

    // ---- Hairline borders (white at low alpha → "thin borders / layered depth") ----
    val Hairline       = Color(0x14FFFFFF) // ~8%
    val HairlineStrong = Color(0x24FFFFFF) // ~14%
    val HairlineFaint  = Color(0x0AFFFFFF) // ~4%

    val Scrim = Color(0x99000000)

    // ---- Spacing scale (dp) ----
    val space1 = 4.dp
    val space2 = 8.dp
    val space3 = 12.dp
    val space4 = 16.dp
    val space5 = 20.dp
    val space6 = 24.dp
    val space8 = 32.dp
    val space10 = 40.dp

    // ---- Corner radii ----
    val radiusSm = 12.dp
    val radiusMd = 18.dp
    val radiusLg = 24.dp
    val radiusXl = 32.dp
}

/**
 * Motion spec. Durations in millis; easings tuned for a confident, premium feel
 * (quick to start, gentle to settle). Respect [LocalReducedMotion] at call sites.
 */
object Motion {
    const val fast = 180
    const val medium = 320
    const val slow = 520
    const val counter = 900       // stat count-ups
    const val draw = 1100         // chart / bar draw-in

    val emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)      // decelerate
    val standard: Easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
    val springy: Easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)  // slight overshoot
}

/** True when the OS "remove animations" setting is on (animator duration scale == 0). */
val LocalReducedMotion = compositionLocalOf { false }

@Composable
fun reducedMotionFromSystem(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        runCatching {
            Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
    }
}
