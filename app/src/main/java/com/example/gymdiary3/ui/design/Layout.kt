package com.example.gymdiary3.ui.design

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gymdiary3.ui.theme.GdType

/** Horizontal page gutter. */
fun Modifier.gutter(): Modifier = this.padding(horizontal = Gd.Gutter)

/**
 * Large screen title for tab roots. Typography carries it — no bar, no container.
 */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .gutter()
            .padding(top = Gd.s3, bottom = Gd.s2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            if (subtitle != null) {
                Text(subtitle, style = GdType.label, color = Gd.TextMuted)
                Spacer(Modifier.height(2.dp))
            }
            Text(title, style = GdType.title, color = Gd.Text)
        }
        trailing?.invoke(this)
    }
}

/** Compact bar for pushed screens: back, title, optional actions. */
@Composable
fun DetailTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: (@Composable RowScope.() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(Gd.s14)
            .padding(horizontal = Gd.s1),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Gd.Text)
        }
        Text(
            title,
            style = GdType.section,
            color = Gd.Text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        actions?.invoke(this)
    }
}

/**
 * Section title in sentence case with an optional quiet "View all"-style link.
 * Grouping comes from the space above it, not from a container.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null,
    top: Dp = Gd.s8
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .gutter()
            .padding(top = top, bottom = Gd.s2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = GdType.section, color = Gd.Text, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) {
            Row(
                modifier = Modifier
                    .clickable(onClick = onAction)
                    .padding(vertical = Gd.s1),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(action, style = GdType.label, color = Gd.TextMuted)
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Gd.TextFaint,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/** 1px divider. [inset] indents it to align with row text. */
@Composable
fun Hairline(modifier: Modifier = Modifier, inset: Dp = Gd.Gutter, color: Color = Gd.Border) {
    HorizontalDivider(
        modifier = modifier.padding(start = inset),
        thickness = Dp.Hairline,
        color = color
    )
}

/**
 * The standard list row. Replaces "one card per item": rows sit on the page and
 * are separated by [Hairline]s. Optional overline carries a status label.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    overline: String? = null,
    overlineColor: Color = Gd.TextMuted,
    titleStrong: Boolean = false,
    /** Optional third line, e.g. the next action for a lift that needs one. */
    detail: String? = null,
    detailColor: Color = Gd.Text,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    val clickModifier = if (onClick != null || onLongClick != null) {
        Modifier.combinedClickable(onClick = { onClick?.invoke() }, onLongClick = onLongClick)
    } else Modifier

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(clickModifier)
            .defaultMinSize(minHeight = Gd.RowMin)
            .gutter()
            .padding(vertical = Gd.s3),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(Gd.s4))
        }
        Column(Modifier.weight(1f)) {
            if (overline != null) {
                Text(overline.uppercase(), style = GdType.overline, color = overlineColor)
                Spacer(Modifier.height(3.dp))
            }
            Text(
                title,
                style = if (titleStrong) GdType.bodyStrong else GdType.body,
                color = Gd.Text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, style = GdType.label, color = Gd.TextMuted)
            }
            if (detail != null) {
                Spacer(Modifier.height(2.dp))
                Text(detail, style = GdType.label, color = detailColor)
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(Gd.s3))
            trailing()
        }
    }
}

/** Trailing chevron for navigable rows. */
@Composable
fun Chevron() {
    Icon(
        Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = Gd.TextFaint,
        modifier = Modifier.size(20.dp)
    )
}

/**
 * One line of figures: "15 workouts · 63,922 kg · 6 PRs". Values in primary
 * text, labels muted. For summary numbers that do not each deserve a container.
 */
@Composable
fun InlineStats(items: List<Pair<String, String>>, modifier: Modifier = Modifier) {
    val text = buildAnnotatedString {
        items.forEachIndexed { i, (value, label) ->
            if (i > 0) withStyle(SpanStyle(color = Gd.TextFaint)) { append("  ·  ") }
            withStyle(SpanStyle(color = Gd.Text, fontWeight = FontWeight.SemiBold)) { append(value) }
            withStyle(SpanStyle(color = Gd.TextMuted)) { append(" $label") }
        }
    }
    Text(text, style = GdType.labelNum, modifier = modifier)
}

/** A figure with its label underneath. Lays out in rows with no container. */
@Composable
fun Metric(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Gd.Text,
    detail: String? = null,
    detailColor: Color = Gd.TextMuted
) {
    Column(modifier) {
        Text(value, style = GdType.metric, color = valueColor, maxLines = 1)
        Spacer(Modifier.height(2.dp))
        Text(label, style = GdType.meta, color = Gd.TextMuted, maxLines = 1)
        if (detail != null) {
            Spacer(Modifier.height(2.dp))
            Text(detail, style = GdType.metaNum, color = detailColor, maxLines = 1)
        }
    }
}

/** Evenly spaced row of [Metric]s inside the gutter. */
@Composable
fun MetricRow(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier.fillMaxWidth().gutter(),
        horizontalArrangement = Arrangement.spacedBy(Gd.s4),
        content = content
    )
}

/** Uppercase status label in a semantic colour, e.g. "STALLING". */
@Composable
fun StatusLabel(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(text.uppercase(), style = GdType.overline, color = color, modifier = modifier)
}

/** Quiet empty state: left-aligned, one line of explanation, optional action. */
@Composable
fun EmptyMessage(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(modifier.fillMaxWidth().gutter().padding(vertical = Gd.s6)) {
        Text(title, style = GdType.bodyStrong, color = Gd.Text)
        Spacer(Modifier.height(Gd.s1))
        Text(body, style = GdType.label, color = Gd.TextMuted)
        if (action != null) {
            Spacer(Modifier.height(Gd.s4))
            Box { action() }
        }
    }
}
