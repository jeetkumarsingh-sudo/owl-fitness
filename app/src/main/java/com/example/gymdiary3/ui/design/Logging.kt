package com.example.gymdiary3.ui.design

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.gymdiary3.ui.theme.GdType
import com.example.gymdiary3.ui.theme.LocalReducedMotion

/**
 * Rest timer pinned to the bottom of workout screens. The line above shrinks
 * as rest runs out (smoothly, not in 1-second jumps); ±15 s adjusts on the spot.
 */
@Composable
fun RestTimerBar(
    secondsLeft: Int,
    totalSeconds: Int,
    onAdjust: (Int) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reduced = LocalReducedMotion.current
    val fraction by animateFloatAsState(
        targetValue = if (totalSeconds <= 0) 0f else (secondsLeft.toFloat() / totalSeconds).coerceIn(0f, 1f),
        animationSpec = tween(if (reduced) 0 else 1000, easing = LinearEasing),
        label = "rest"
    )
    Column(
        modifier
            .fillMaxWidth()
            .background(Gd.Surface)
            .semantics { contentDescription = "Rest timer, ${secondsLeft / 60} minutes ${secondsLeft % 60} seconds left" }
    ) {
        Box(Modifier.fillMaxWidth().height(2.dp).background(Gd.Border)) {
            Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(Gd.Accent))
        }
        Row(
            Modifier.fillMaxWidth().height(64.dp).gutter(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Rest", style = GdType.meta, color = Gd.TextMuted)
                Text(
                    "%d:%02d".format(secondsLeft / 60, secondsLeft % 60),
                    style = GdType.metric, color = Gd.Text
                )
            }
            TextAction("−15", onClick = { onAdjust(-15) }, color = Gd.Text)
            TextAction("+15", onClick = { onAdjust(15) }, color = Gd.Text)
            TextAction("Skip", onClick = onSkip, color = Gd.TextMuted)
        }
    }
}

/**
 * Weight / reps input: big tabular value between − and +, inside an input
 * outline (the one place a boundary is functional). Tap the value to type it.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ValueStepper(
    label: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(label, style = GdType.meta, color = Gd.TextMuted)
        Spacer(Modifier.height(6.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(Gd.RadiusMd))
                .background(Gd.Surface)
                .border(BorderStroke(1.dp, Gd.BorderInput), RoundedCornerShape(Gd.RadiusMd)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMinus) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease $label", tint = Gd.TextMuted)
            }
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .combinedClickable(onClick = onEdit, onLongClick = onEdit),
                contentAlignment = Alignment.Center
            ) {
                Text(value, style = GdType.input, color = Gd.Text, textAlign = TextAlign.Center)
            }
            IconButton(onClick = onPlus) {
                Icon(Icons.Default.Add, contentDescription = "Increase $label", tint = Gd.Text)
            }
        }
    }
}

/** Numeric entry for an exact value. */
@Composable
fun NumberEntryDialog(
    title: String,
    initial: String,
    decimal: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Gd.SurfaceRaised,
        title = { Text(title, style = GdType.section, color = Gd.Text) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { v -> text = v.filter { it.isDigit() || (decimal && (it == '.' || it == ',')) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
                textStyle = GdType.input.copy(color = Gd.Text),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gd.Accent,
                    unfocusedBorderColor = Gd.BorderInput,
                    cursorColor = Gd.Accent
                )
            )
        },
        confirmButton = { TextAction("Set", onClick = { onConfirm(text.replace(',', '.')) }, color = Gd.AccentText) },
        dismissButton = { TextAction("Cancel", onClick = onDismiss) }
    )
}

/** Small RPE picker (6–10). Tap the selected value again to clear it. */
@Composable
fun RpePicker(selected: Float?, onSelect: (Float?) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Gd.s2)) {
        listOf(6f, 7f, 8f, 9f, 10f).forEach { v ->
            val on = selected == v
            Box(
                Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(Gd.RadiusSm))
                    .background(if (on) Gd.SurfaceRaised else Gd.Surface)
                    .border(BorderStroke(1.dp, if (on) Gd.Text else Gd.Border), RoundedCornerShape(Gd.RadiusSm))
                    .clickable { onSelect(if (on) null else v) },
                contentAlignment = Alignment.Center
            ) {
                Text(v.toInt().toString(), style = GdType.labelNum, color = if (on) Gd.Text else Gd.TextMuted)
            }
        }
    }
}
