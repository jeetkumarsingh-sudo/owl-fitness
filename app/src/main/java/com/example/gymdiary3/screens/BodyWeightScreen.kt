package com.example.gymdiary3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.gymdiary3.viewmodel.BodyWeightViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gymdiary3.domain.BodyWeightAnalyzer
import com.example.gymdiary3.domain.model.BodyWeight
import com.example.gymdiary3.domain.settings.WeightFormatter
import com.example.gymdiary3.ui.components.*
import com.example.gymdiary3.ui.theme.Apex
import java.text.SimpleDateFormat
import java.util.*
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyWeightScreen(
    nav: NavHostController,
    viewModel: BodyWeightViewModel = hiltViewModel()
) {
    var weightInput by remember { mutableStateOf("") }
    val weights by viewModel.allWeights.collectAsStateWithLifecycle()
    val userSettings by viewModel.settingsRepository.userSettingsFlow
        .collectAsStateWithLifecycle(com.example.gymdiary3.domain.settings.UserSettings())
    val weightUnit = userSettings.weightUnit
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    ApexScaffold(title = "Body Weight", onBack = { nav.popBackStack() }) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ApexPanel(radius = Apex.radiusMd) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Current weight (${WeightFormatter.label(weightUnit)})", color = Apex.TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineSmall.copy(color = Apex.TextPrimary),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Apex.TextPrimary,
                            unfocusedTextColor = Apex.TextPrimary,
                            focusedLabelColor = Apex.AccentSoft,
                            unfocusedLabelColor = Apex.TextMuted,
                            focusedBorderColor = Apex.Accent,
                            unfocusedBorderColor = Apex.Hairline,
                            focusedContainerColor = Apex.Surface3,
                            unfocusedContainerColor = Apex.Surface3
                        )
                    )
                    ApexCta(text = "LOG WEIGHT", onClick = {
                        val w = weightInput.toDoubleOrNull() ?: return@ApexCta
                        viewModel.insertWeight(WeightFormatter.toKilograms(w, weightUnit))
                        weightInput = ""
                    })
                }
            }

            BodyWeightChart(weights, weightUnit)

            SectionLabel("History", accent = true)

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(weights, key = { it.id }) { item ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.deleteWeight(item)
                                true
                            } else false
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            Box(
                                Modifier.fillMaxSize()
                                    .background(Apex.AccentDeep, RoundedCornerShape(Apex.radiusMd))
                                    .padding(end = 16.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text("DELETE", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    ) {
                        WeightCard(item, sdf, weightUnit)
                    }
                }
            }
        }
    }
}

@Composable
private fun BodyWeightChart(weights: List<BodyWeight>, unit: String) {
    val unitLabel = WeightFormatter.label(unit)
    val displayWeights = remember(weights, unit) {
        weights.map { it.copy(weight = WeightFormatter.fromKilograms(it.weight, unit)) }
    }

    if (displayWeights.size < 2) {
        Box(
            Modifier.fillMaxWidth().height(180.dp)
                .background(Apex.Surface2, RoundedCornerShape(Apex.radiusMd)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Log 2+ entries to see your trend",
                color = Apex.TextMuted,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    val stats = BodyWeightAnalyzer.getStats(displayWeights) ?: return
    val sorted = remember(displayWeights) { displayWeights.sortedBy { it.timestamp } }
    val latestWeight = sorted.lastOrNull()
    val previousWeight = sorted.getOrNull(sorted.size - 2)
    val weightChange = (latestWeight?.weight ?: 0.0) - (previousWeight?.weight ?: 0.0)

    val recentAvg = remember(displayWeights) {
        val cutoff = System.currentTimeMillis() - 14L * 24 * 60 * 60 * 1000
        val recent = displayWeights.filter { it.timestamp >= cutoff }
        if (recent.isEmpty()) null else recent.map { it.weight }.average()
    }

    val trendColor = when {
        weightChange > 0.1 -> Apex.Positive
        weightChange < -0.1 -> Apex.Negative
        else -> Apex.TextMuted
    }
    val trendText = (if (weightChange > 0) "+" else "") + "${WeightFormatter.formatNumber(weightChange, 2)} $unitLabel"

    ApexPanel(radius = Apex.radiusMd) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatMini("CURRENT", "${WeightFormatter.formatNumber(stats.latestWeight, 1)} $unitLabel", Apex.TextPrimary)
                StatMini("CHANGE", trendText, trendColor, Alignment.CenterHorizontally)
                StatMini(
                    "AVG (14D)",
                    if (recentAvg != null) "${WeightFormatter.formatNumber(recentAvg, 1)} $unitLabel" else "--",
                    Apex.TextPrimary,
                    Alignment.End
                )
            }
            Spacer(Modifier.height(18.dp))
            ApexLineChart(values = sorted.map { it.weight.toFloat() }, height = 190.dp)
        }
    }
}

@Composable
private fun StatMini(label: String, value: String, valueColor: Color, align: Alignment.Horizontal = Alignment.Start) {
    Column(horizontalAlignment = align) {
        Text(label, color = Apex.TextMuted, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(2.dp))
        Text(value, color = valueColor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun WeightCard(item: BodyWeight, sdf: SimpleDateFormat, unit: String) {
    ApexPanel(radius = Apex.radiusMd) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(sdf.format(Date(item.timestamp)), color = Apex.TextMuted, style = MaterialTheme.typography.bodyMedium)
            Text(
                WeightFormatter.formatFromKilograms(item.weight, unit),
                color = Apex.TextPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
