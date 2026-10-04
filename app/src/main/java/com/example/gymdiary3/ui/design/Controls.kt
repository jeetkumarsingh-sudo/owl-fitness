package com.example.gymdiary3.ui.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gymdiary3.ui.theme.GdType
import com.example.gymdiary3.ui.theme.LocalReducedMotion

/* ------------------------------------------------------------------ buttons */

/** The one primary action on a screen. Solid accent, modest radius, no gradient. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(Gd.ButtonHeight),
        shape = RoundedCornerShape(Gd.RadiusMd),
        colors = ButtonDefaults.buttonColors(
            containerColor = Gd.Accent,
            contentColor = Gd.OnAccent,
            disabledContainerColor = Gd.SurfaceRaised,
            disabledContentColor = Gd.TextFaint
        ),
        elevation = null,
        contentPadding = PaddingValues(horizontal = Gd.s5)
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(Gd.s2))
        }
        Text(text, style = GdType.bodyStrong)
    }
}

/** Neutral secondary action. */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    compact: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(if (compact) 40.dp else Gd.ButtonHeight),
        shape = RoundedCornerShape(Gd.RadiusMd),
        border = BorderStroke(1.dp, Gd.BorderStrong),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Gd.Text,
            disabledContentColor = Gd.TextFaint
        ),
        contentPadding = PaddingValues(horizontal = Gd.s4)
    ) {
        Text(text, style = if (compact) GdType.label.copy(fontWeight = FontWeight.SemiBold) else GdType.bodyStrong)
    }
}

/** Text-only action. Muted by default; pass [color] = AccentText for a primary text action. */
@Composable
fun TextAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Gd.TextMuted,
    contentPadding: PaddingValues = PaddingValues(horizontal = Gd.s3)
) {
    TextButton(onClick = onClick, modifier = modifier, contentPadding = contentPadding) {
        Text(text, style = GdType.label.copy(fontWeight = FontWeight.SemiBold), color = color)
    }
}

/* --------------------------------------------------------- segmented control */

/**
 * Compact single-choice selector (chart ranges: 4W 8W 3M …). The selection
 * slides between options rather than snapping.
 */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val reduced = LocalReducedMotion.current
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Gd.Surface)
            .padding(3.dp)
    ) {
        val segment = maxWidth / options.size
        val indicatorX by animateDpAsState(
            targetValue = segment * selectedIndex,
            animationSpec = tween(if (reduced) 0 else GdMotion.Base, easing = GdMotion.Ease),
            label = "segment"
        )
        Box(
            Modifier
                .offset(x = indicatorX)
                .width(segment)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(Gd.SurfaceRaised)
        )
        Row(Modifier.fillMaxWidth().fillMaxHeight()) {
            options.forEachIndexed { i, label ->
                val selected = i == selectedIndex
                val color by animateColorAsState(
                    if (selected) Gd.Text else Gd.TextMuted,
                    tween(if (reduced) 0 else GdMotion.Base),
                    label = "segColor"
                )
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(role = Role.Tab) { onSelect(i) }
                        .semantics { this.selected = selected },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        style = GdType.label.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium),
                        color = color
                    )
                }
            }
        }
    }
}

/* ------------------------------------------------------------ underline tabs */

/** Text tabs with an accent underline that slides to the selection. */
@Composable
fun UnderlineTabs(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val reduced = LocalReducedMotion.current
    Column(modifier.fillMaxWidth()) {
        BoxWithConstraints(Modifier.fillMaxWidth().gutter()) {
            val tabWidth = maxWidth / tabs.size
            val x by animateDpAsState(
                targetValue = tabWidth * selectedIndex,
                animationSpec = tween(if (reduced) 0 else GdMotion.Base, easing = GdMotion.Ease),
                label = "tabX"
            )
            Row(Modifier.fillMaxWidth()) {
                tabs.forEachIndexed { i, label ->
                    val selected = i == selectedIndex
                    Box(
                        Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable(role = Role.Tab) { onSelect(i) }
                            .semantics { this.selected = selected },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            style = GdType.label.copy(fontWeight = FontWeight.SemiBold),
                            color = if (selected) Gd.Text else Gd.TextMuted
                        )
                    }
                }
            }
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = x)
                    .width(tabWidth)
                    .height(2.dp)
                    .padding(horizontal = Gd.s5)
                    .background(Gd.Accent)
            )
        }
        Hairline(inset = 0.dp)
    }
}

/* ---------------------------------------------------------------- week strip */

data class WeekDay(
    val initial: String,
    val done: Boolean,
    val isToday: Boolean,
    val isFuture: Boolean
)

/**
 * Compact weekly activity: S M T W T F S over filled/hollow dots. A day fills
 * with a short scale-in when it becomes done (honours reduced motion).
 */
@Composable
fun WeekStrip(days: List<WeekDay>, modifier: Modifier = Modifier) {
    val reduced = LocalReducedMotion.current
    val doneCount = days.count { it.done }
    Row(
        modifier
            .fillMaxWidth()
            .semantics { contentDescription = "$doneCount of ${days.size} days trained this week" },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { day ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(28.dp)) {
                Text(
                    day.initial,
                    style = GdType.meta.copy(fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium),
                    color = if (day.isToday) Gd.Text else Gd.TextFaint
                )
                Spacer(Modifier.height(Gd.s2))
                val fill by animateFloatAsState(
                    targetValue = if (day.done) 1f else 0f,
                    animationSpec = tween(if (reduced) 0 else GdMotion.Slow, easing = GdMotion.Ease),
                    label = "dayFill"
                )
                Box(Modifier.size(10.dp), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .border(
                                width = 1.5.dp,
                                color = when {
                                    day.isToday -> Gd.TextMuted
                                    day.isFuture -> Gd.Border
                                    else -> Gd.BorderStrong
                                },
                                shape = CircleShape
                            )
                    )
                    Box(
                        Modifier
                            .size(10.dp)
                            .scale(fill)
                            .background(Gd.Text, CircleShape)
                    )
                }
            }
        }
    }
}

/* ------------------------------------------------------------ animated number */

/**
 * Number that tweens when its value changes (e.g. volume after logging a set).
 * The first frame shows the real value — nothing counts up on every screen visit.
 */
@Composable
fun AnimatedNumber(
    value: Double,
    format: (Double) -> String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null
) {
    val reduced = LocalReducedMotion.current
    val animated by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = tween(if (reduced) 0 else GdMotion.Slow, easing = GdMotion.Ease),
        label = "number"
    )
    Text(format(animated.toDouble()), style = style, color = color, modifier = modifier, textAlign = textAlign)
}
