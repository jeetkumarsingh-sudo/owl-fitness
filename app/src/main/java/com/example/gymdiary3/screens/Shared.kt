package com.example.gymdiary3.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.gymdiary3.domain.progression.ProgressionStatus
import com.example.gymdiary3.presentation.insight.InsightRow
import com.example.gymdiary3.presentation.insight.Tone
import com.example.gymdiary3.ui.design.Chevron
import com.example.gymdiary3.ui.design.Gd
import com.example.gymdiary3.ui.design.StatusLabel
import com.example.gymdiary3.ui.design.gutter
import com.example.gymdiary3.ui.theme.GdType

fun Tone.color(): Color = when (this) {
    Tone.POSITIVE -> Gd.Positive
    Tone.WARNING -> Gd.Warning
    Tone.INFO -> Gd.Info
    Tone.DANGER -> Gd.Danger
    Tone.NEUTRAL -> Gd.TextMuted
}

fun ProgressionStatus.label(): String = when (this) {
    ProgressionStatus.PROGRESSING -> "Progressing"
    ProgressionStatus.STABLE -> "Stable"
    ProgressionStatus.STALLING -> "Stalling"
    ProgressionStatus.REGRESSING -> "Regressing"
    ProgressionStatus.NEW -> "New"
}

fun ProgressionStatus.color(): Color = when (this) {
    ProgressionStatus.PROGRESSING -> Gd.Positive
    ProgressionStatus.STABLE -> Gd.Info
    ProgressionStatus.STALLING -> Gd.Warning
    ProgressionStatus.REGRESSING -> Gd.Danger
    ProgressionStatus.NEW -> Gd.TextMuted
}

/**
 * Insight as a row, readable in two seconds:
 *   STALLING          ← status in its semantic colour
 *   Lat Pulldown      ← subject
 *   50 kg · 3 sessions
 *   Try +1 rep before adding weight
 */
@Composable
fun InsightItem(row: InsightRow, onClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    val click = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Row(
        modifier.fillMaxWidth().then(click).gutter().padding(vertical = Gd.s3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            StatusLabel(row.tag, row.tone.color())
            Spacer(Modifier.height(4.dp))
            Text(row.title, style = GdType.bodyStrong, color = Gd.Text)
            Text(row.state, style = GdType.labelNum, color = Gd.TextMuted)
            if (row.action != null) {
                Spacer(Modifier.height(6.dp))
                Text(row.action, style = GdType.label, color = Gd.Text)
            }
        }
        if (onClick != null) {
            Spacer(Modifier.width(Gd.s3))
            Chevron()
        }
    }
}
