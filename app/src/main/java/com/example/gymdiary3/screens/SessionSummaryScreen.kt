package com.example.gymdiary3.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.presentation.history.SummaryUiState
import com.example.gymdiary3.system.export.ShareUtils
import com.example.gymdiary3.ui.design.*
import com.example.gymdiary3.ui.theme.GdType
import com.example.gymdiary3.ui.theme.LocalReducedMotion
import com.example.gymdiary3.viewmodel.SummaryViewModel

data class SummaryActions(
    val onBack: () -> Unit = {},
    val onShareText: () -> Unit = {},
    val onShareImage: () -> Unit = {},
    val onOpenExercise: (String) -> Unit = {}
)

@Suppress("UNUSED_PARAMETER")
@Composable
fun SessionSummaryScreen(nav: NavHostController, sessionId: Int, vm: SummaryViewModel = hiltViewModel()) {
    val data by vm.data.collectAsStateWithLifecycle()
    val unit by vm.unit.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var captureView by remember { mutableStateOf<android.view.View?>(null) }
    val (session, state) = data ?: return

    Box {
        SummaryScreen(
            state,
            SummaryActions(
                onBack = { nav.popBackStack() },
                onShareText = { ShareUtils.shareText(context, ShareUtils.buildShareText(session, unit)) },
                onShareImage = { captureView?.let { v -> v.post { ShareUtils.shareImage(context, ShareUtils.captureView(v)) } } },
                onOpenExercise = { nav.navigate("analytics/${android.net.Uri.encode(it)}") }
            )
        )
        // Off-screen surface rendered for the image share.
        AndroidView(
            factory = { ctx -> ComposeView(ctx).apply { setContent { ShareCard(state) } } },
            modifier = Modifier.size(0.dp),
            update = { captureView = it }
        )
    }
}

@Composable
fun SummaryScreen(state: SummaryUiState, actions: SummaryActions, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    var celebrated by rememberSaveable(state.sessionId) { mutableStateOf(false) }
    LaunchedEffect(state.sessionId, state.prs.size) {
        if (state.prs.isNotEmpty() && !celebrated) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            celebrated = true
        }
    }

    Column(modifier.fillMaxSize()) {
        DetailTopBar(
            title = state.title,
            onBack = actions.onBack,
            actions = {
                IconButton(onClick = actions.onShareText) {
                    Icon(Icons.Outlined.Share, contentDescription = "Share as text", tint = Gd.TextMuted)
                }
                IconButton(onClick = actions.onShareImage) {
                    Icon(Icons.Outlined.Image, contentDescription = "Share as image", tint = Gd.TextMuted)
                }
            }
        )
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = Gd.s8)) {
            item(key = "head") {
                Text(state.dateLine, style = GdType.label, color = Gd.TextMuted, modifier = Modifier.gutter())
                Spacer(Modifier.height(Gd.s4))
                MetricRow {
                    Metric("${state.sets}", "sets", Modifier.weight(1f))
                    Metric(state.duration, "duration", Modifier.weight(1f))
                    Metric(state.volume, "volume", Modifier.weight(1.3f))
                }
            }

            if (state.prs.isNotEmpty()) {
                item(key = "prHeader") {
                    SectionHeader(if (state.prs.size == 1) "New record" else "${state.prs.size} new records")
                }
                itemsIndexed(state.prs, key = { _, p -> "pr_${p.exercise}" }) { i, pr ->
                    PrRow(pr.exercise, pr.set, pr.detail, index = i) { actions.onOpenExercise(pr.exercise) }
                }
            }

            item(key = "exHeader") { SectionHeader("Exercises") }
            itemsIndexed(state.exercises, key = { _, e -> "ex_${e.name}" }) { i, ex ->
                ListRow(
                    title = ex.name,
                    subtitle = listOfNotNull(ex.setsLine, ex.extra).joinToString("\n"),
                    overline = if (ex.isPr) "PR" else null,
                    overlineColor = Gd.AccentText,
                    titleStrong = true,
                    trailing = { Chevron() },
                    onClick = { actions.onOpenExercise(ex.name) }
                )
                if (i < state.exercises.lastIndex) Hairline()
            }

            if (state.muscles.isNotEmpty()) {
                item(key = "muscles") {
                    SectionHeader("Volume by muscle")
                    Column(Modifier.gutter(), verticalArrangement = Arrangement.spacedBy(Gd.s3)) {
                        state.muscles.forEach { MuscleBar(it.muscle, it.volume, it.fraction) }
                    }
                }
            }

            state.notes?.let { n ->
                item(key = "notes") {
                    SectionHeader("Notes")
                    Text(n, style = GdType.body, color = Gd.TextMuted, modifier = Modifier.gutter())
                }
            }
        }
    }
}

/** A new record: settles from a light accent wash to neutral once, staggered slightly. */
@Composable
private fun PrRow(exercise: String, set: String, detail: String, index: Int, onClick: () -> Unit) {
    val reduced = LocalReducedMotion.current
    val wash = remember { Animatable(if (reduced) 0f else 1f) }
    LaunchedEffect(Unit) {
        if (!reduced) {
            kotlinx.coroutines.delay(150L * index)
            wash.animateTo(0f, tween(GdMotion.Highlight))
        }
    }
    Box(Modifier.background(Gd.AccentWash.copy(alpha = Gd.AccentWash.alpha * wash.value))) {
        ListRow(
            title = exercise,
            subtitle = detail,
            titleStrong = true,
            trailing = { Text(set, style = GdType.bodyStrong.copy(fontFeatureSettings = "tnum"), color = Gd.AccentText) },
            onClick = onClick
        )
    }
}

@Composable
private fun MuscleBar(muscle: String, volume: String, fraction: Float) =
    LabeledBar(muscle, fraction, volume)

/** The image a user shares: dark, on-brand, just the facts. */
@Composable
private fun ShareCard(state: SummaryUiState) {
    Column(Modifier.width(420.dp).background(Gd.Bg).padding(28.dp)) {
        Text("GYM DIARY", fontSize = 12.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.SemiBold, color = Gd.AccentText)
        Spacer(Modifier.height(10.dp))
        Text(state.title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(state.dateLine, fontSize = 13.sp, color = Gd.TextMuted)
        Spacer(Modifier.height(18.dp))
        Text("${state.sets} sets · ${state.duration} · ${state.volume}", fontSize = 14.sp, color = Gd.Text)
        Spacer(Modifier.height(18.dp))
        state.exercises.forEach {
            Text(it.name + if (it.isPr) "  · PR" else "", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(it.setsLine, fontSize = 13.sp, color = Gd.TextMuted)
            Spacer(Modifier.height(10.dp))
        }
    }
}
