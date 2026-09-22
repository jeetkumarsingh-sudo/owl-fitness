package com.example.gymdiary3.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymdiary3.ui.theme.OwlColors

/** Rounded surface card with a subtle top-lit gradient and hairline border. */
@Composable
fun ApexCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val border = BorderStroke(1.dp, OwlColors.BorderSubtle)
    val gradient = Brush.verticalGradient(
        listOf(OwlColors.CardGradTop, OwlColors.CardGradBot)
    )
    val fill = Modifier
        .clip(shape)
        .background(gradient)

    if (onClick != null) {
        Surface(
            modifier = modifier,
            onClick = onClick,
            shape = shape,
            color = Color.Transparent,
            border = border
        ) {
            Box(fill) { content() }
        }
    } else {
        Surface(
            modifier = modifier,
            shape = shape,
            color = Color.Transparent,
            border = border
        ) {
            Box(fill) { content() }
        }
    }
}

/** Full-width gradient crimson pill — the primary call to action. */
@Composable
fun ApexPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(100)
    val fill: Brush = if (enabled) {
        Brush.verticalGradient(listOf(OwlColors.CrimsonGradTop, OwlColors.CrimsonGradBot))
    } else {
        Brush.verticalGradient(listOf(OwlColors.CrimsonDim, OwlColors.CrimsonDim))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(shape)
            .alpha(if (enabled) 1f else 0.55f)
            .background(fill)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = Color.White
        )
    }
}

/** Full-width outlined secondary action. */
@Composable
fun ApexOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        enabled = enabled,
        shape = RoundedCornerShape(100),
        border = BorderStroke(1.dp, OwlColors.BorderActive),
        contentPadding = PaddingValues(horizontal = 24.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = OwlColors.TextSecondary)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Tracked-out section label with a leading accent tick. */
@Composable
fun ApexSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    accent: Color = OwlColors.Crimson
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(width = 3.dp, height = 14.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accent)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = OwlColors.TextSecondary,
            fontWeight = FontWeight.Bold
        )
    }
}
