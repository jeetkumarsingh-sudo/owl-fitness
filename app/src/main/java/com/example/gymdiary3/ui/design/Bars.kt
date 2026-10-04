package com.example.gymdiary3.ui.design

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gymdiary3.ui.theme.GdType
import com.example.gymdiary3.ui.theme.LocalReducedMotion

private val LabelColumn = 84.dp
private val BarShape = RoundedCornerShape(3.dp)

/**
 * A labelled proportion bar: "Back  ▓▓▓▓░░  48 sets". Fixed label and value
 * columns keep the bars aligned down a list. At large font scales those columns
 * would wrap ("Ready in 36 / h"), so the row stacks instead: label and value on
 * one line, the bar full width beneath. Fills once on entry (Chart timing).
 */
@Composable
fun LabeledBar(
    label: String,
    fraction: Float,
    value: String,
    modifier: Modifier = Modifier,
    fill: Color = Gd.DataNeutral,
    valueColor: Color = Gd.TextMuted,
    valueWidth: Dp = 92.dp
) {
    val reduced = LocalReducedMotion.current
    var shown by remember { mutableStateOf(reduced) }
    LaunchedEffect(Unit) { shown = true }
    val f by animateFloatAsState(
        if (shown) fraction.coerceIn(0f, 1f) else 0f,
        tween(if (reduced) 0 else GdMotion.Chart, easing = GdMotion.Ease),
        label = "bar"
    )
    val bar: @Composable (Modifier) -> Unit = { m ->
        Box(m.height(6.dp).clip(BarShape).background(Gd.Surface)) {
            Box(Modifier.fillMaxWidth(f).fillMaxHeight().clip(BarShape).background(fill))
        }
    }
    val merged = modifier.semantics(mergeDescendants = true) {}

    if (LocalDensity.current.fontScale > 1.15f) {
        Column(merged) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label, style = GdType.label, color = Gd.Text, modifier = Modifier.weight(1f))
                Text(value, style = GdType.labelNum, color = valueColor)
            }
            Spacer(Modifier.height(Gd.s1))
            bar(Modifier.fillMaxWidth())
        }
    } else {
        Row(merged, verticalAlignment = Alignment.CenterVertically) {
            Text(
                label, style = GdType.label, color = Gd.Text, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(LabelColumn)
            )
            bar(Modifier.weight(1f))
            Text(
                value, style = GdType.labelNum, color = valueColor, maxLines = 1,
                modifier = Modifier.width(valueWidth).padding(start = Gd.s3)
            )
        }
    }
}
