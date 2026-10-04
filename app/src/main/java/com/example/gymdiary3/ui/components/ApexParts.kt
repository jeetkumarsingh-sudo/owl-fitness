package com.example.gymdiary3.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymdiary3.ui.theme.Apex
import com.example.gymdiary3.ui.theme.LocalReducedMotion
import com.example.gymdiary3.ui.theme.Motion

/* ----------------------------------------------------------------------------
 * Appear — staggered fade + rise entrance for list/grid items. Honors reduced
 * motion by appearing instantly. Pass the item index for the stagger.
 * ------------------------------------------------------------------------- */
@Composable
fun Modifier.appear(index: Int = 0, stepMs: Int = 45): Modifier {
    val reduced = LocalReducedMotion.current
    var shown by remember { mutableStateOf(reduced) }
    LaunchedEffect(Unit) {
        if (!reduced) {
            delay(index * stepMs.toLong())
            shown = true
        }
    }
    val a by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(320, easing = Motion.emphasized),
        label = "appear"
    )
    return this.graphicsLayer {
        alpha = a
        translationY = (1f - a) * 26f
    }
}

/* ----------------------------------------------------------------------------
 * Panel — the canonical Apex surface. Carbon fill, hairline border, a faint top
 * highlight for layered depth, and press-scale feedback when clickable.
 * ------------------------------------------------------------------------- */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ApexPanel(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    fill: Color = Apex.Surface2,
    border: Color = Apex.Hairline,
    accentEdge: Boolean = false,
    radius: Dp = Apex.radiusLg,
    content: @Composable () -> Unit
) {
    val reduced = LocalReducedMotion.current
    val shape = RoundedCornerShape(radius)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduced && onClick != null) 0.97f else 1f,
        animationSpec = tween(Motion.fast, easing = Motion.standard),
        label = "panelScale"
    )

    val borderBrush = if (accentEdge) {
        Brush.verticalGradient(listOf(Apex.Accent.copy(alpha = 0.55f), Apex.Hairline))
    } else {
        Brush.verticalGradient(listOf(Apex.HairlineStrong, Apex.HairlineFaint))
    }

    var m = modifier
        .scale(scale)
        .clip(shape)
        .background(fill, shape)
        // Top highlight: brightens the upper edge like light catching a surface
        .background(
            Brush.verticalGradient(
                0f to Color.White.copy(alpha = 0.04f),
                0.35f to Color.Transparent
            ),
            shape
        )
        .border(BorderStroke(1.dp, borderBrush), shape)
    if (onClick != null || onLongClick != null) {
        m = m.combinedClickable(
            interactionSource = interaction,
            indication = null,
            onClick = { onClick?.invoke() },
            onLongClick = onLongClick
        )
    }
    Box(m) { content() }
}

/* ----------------------------------------------------------------------------
 * Primary CTA — crimson gradient, pill, press-scale. The confident main action.
 * ------------------------------------------------------------------------- */
@Composable
fun ApexCta(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null
) {
    val reduced = LocalReducedMotion.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled && !reduced) 0.97f else 1f,
        animationSpec = tween(Motion.fast, easing = Motion.standard),
        label = "ctaScale"
    )
    val shape = RoundedCornerShape(100)
    val brush = if (enabled) {
        Brush.horizontalGradient(listOf(Apex.AccentBright, Apex.Accent))
    } else {
        Brush.horizontalGradient(listOf(Apex.Surface4, Apex.Surface3))
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale)
            .clip(shape)
            .background(brush, shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            leading?.invoke()
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) Color.White else Apex.TextMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/* ----------------------------------------------------------------------------
 * Section label — tracked, uppercase, understated. Accent variant for emphasis.
 * ------------------------------------------------------------------------- */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false
) {
    Text(
        text = text.uppercase(),
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        color = if (accent) Apex.AccentSoft else Apex.TextSecondary,
        letterSpacing = 1.6.sp,
        fontWeight = FontWeight.SemiBold
    )
}

/* ----------------------------------------------------------------------------
 * Count-up text — animates from 0 on first appearance and tweens smoothly when
 * the value changes. Jumps instantly under reduced motion.
 * ------------------------------------------------------------------------- */
@Composable
fun CountUpText(
    target: Int,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = "",
    durationMillis: Int = Motion.counter
) {
    val reduced = LocalReducedMotion.current
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val value by animateIntAsState(
        targetValue = if (started || reduced) target else 0,
        animationSpec = tween(if (reduced) 0 else durationMillis, easing = Motion.emphasized),
        label = "countInt"
    )
    Text(text = "$prefix$value$suffix", style = style, color = color, modifier = modifier)
}

@Composable
fun CountUpDecimalText(
    target: Float,
    decimals: Int,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = "",
    durationMillis: Int = Motion.counter
) {
    val reduced = LocalReducedMotion.current
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val value by animateFloatAsState(
        targetValue = if (started || reduced) target else 0f,
        animationSpec = tween(if (reduced) 0 else durationMillis, easing = Motion.emphasized),
        label = "countDec"
    )
    Text(
        text = prefix + "%.${decimals}f".format(value) + suffix,
        style = style,
        color = color,
        modifier = modifier
    )
}

/* ----------------------------------------------------------------------------
 * Stat tile — label + animated value, sits inside a panel or a row.
 * ------------------------------------------------------------------------- */
@Composable
fun StatValue(
    value: Int,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Apex.TextPrimary,
    suffix: String = ""
) {
    Column(modifier) {
        CountUpText(
            target = value,
            style = MaterialTheme.typography.headlineMedium,
            color = valueColor,
            suffix = suffix
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = Apex.TextMuted,
            letterSpacing = 1.sp
        )
    }
}

/* ----------------------------------------------------------------------------
 * Weekly activity bars — seven slots, filled bars animate up for active days.
 * `activeDays` holds day indices (0=first day of week) that had a workout.
 * ------------------------------------------------------------------------- */
@Composable
fun WeekActivityBars(
    activeDays: Set<Int>,
    dayInitials: List<String>,
    todayIndex: Int,
    modifier: Modifier = Modifier,
    barMaxHeight: Dp = 48.dp
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        dayInitials.forEachIndexed { i, initial ->
            val active = i in activeDays
            val isToday = i == todayIndex
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                val reduced = LocalReducedMotion.current
                var started by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { started = true }
                val frac by animateFloatAsState(
                    targetValue = if (!started && !reduced) 0f else if (active) 1f else 0.18f,
                    animationSpec = tween(if (reduced) 0 else Motion.draw, easing = Motion.emphasized),
                    label = "bar$i"
                )
                Box(
                    modifier = Modifier
                        .height(barMaxHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(barMaxHeight * frac.coerceIn(0.06f, 1f))
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (active)
                                    Brush.verticalGradient(listOf(Apex.AccentBright, Apex.Accent))
                                else
                                    Brush.verticalGradient(listOf(Apex.Surface4, Apex.Surface3))
                            )
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    initial,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isToday) Apex.AccentSoft else Apex.TextMuted,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

/* ----------------------------------------------------------------------------
 * Chip / pill.
 * ------------------------------------------------------------------------- */
@Composable
fun ApexChip(
    text: String,
    modifier: Modifier = Modifier,
    tint: Color = Apex.AccentSoft,
    fill: Color = Apex.AccentWash
) {
    Box(
        modifier
            .clip(RoundedCornerShape(100))
            .background(fill)
            .border(BorderStroke(1.dp, tint.copy(alpha = 0.35f)), RoundedCornerShape(100))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}

/* ----------------------------------------------------------------------------
 * Shimmer skeleton — content-aware loading placeholder.
 * ------------------------------------------------------------------------- */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    radius: Dp = Apex.radiusSm
) {
    val reduced = LocalReducedMotion.current
    val shape = RoundedCornerShape(radius)
    if (reduced) {
        Box(modifier.clip(shape).background(Apex.Surface3))
        return
    }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1300), RepeatMode.Restart),
        label = "shimmerX"
    )
    Box(
        modifier
            .clip(shape)
            .background(Apex.Surface3)
            .drawWithContent {
                drawContent()
                val sweep = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.06f), Color.Transparent),
                    startX = x * size.width - size.width,
                    endX = x * size.width
                )
                drawRect(sweep)
            }
    )
}
