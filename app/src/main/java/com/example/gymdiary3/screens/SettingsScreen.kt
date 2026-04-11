package com.example.gymdiary3.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.ui.theme.OwlColors
import com.example.gymdiary3.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(nav: NavHostController, viewModel: SettingsViewModel) {
    val settings by viewModel.userSettings.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = OwlColors.DeepBg,
        topBar = {
            TopAppBar(
                title = { Text("SETTINGS", fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 1.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OwlColors.DeepBg,
                    titleContentColor = OwlColors.TextPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = OwlColors.TextPrimary)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            item {
                Text(
                    text = "UNITS",
                    color = OwlColors.PurpleSoft,
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.height(16.dp))
                Surface(
                    color = OwlColors.CardBg,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OwlColors.BorderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        UnitButton(
                            label = "kg",
                            isSelected = settings.weightUnit == "kg",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.updateWeightUnit("kg") }
                        )
                        UnitButton(
                            label = "lbs",
                            isSelected = settings.weightUnit == "lbs",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.updateWeightUnit("lbs") }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "REST TIMER DEFAULTS",
                    color = OwlColors.PurpleSoft,
                    style = MaterialTheme.typography.labelMedium,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.height(16.dp))
                Surface(
                    color = OwlColors.CardBg,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OwlColors.BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        val timerOptions = listOf(30, 60, 90, 120, 180)
                        timerOptions.forEachIndexed { index, seconds ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.updateDefaultRestSeconds(seconds) }
                                    .padding(horizontal = 8.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.defaultRestSeconds == seconds,
                                    onClick = { viewModel.updateDefaultRestSeconds(seconds) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = OwlColors.Purple,
                                        unselectedColor = OwlColors.TextMuted
                                    )
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "${seconds} seconds",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = OwlColors.TextPrimary
                                )
                            }
                            if (index < timerOptions.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = OwlColors.BorderSubtle
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UnitButton(label: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) OwlColors.Purple else Color.Transparent,
            contentColor = if (isSelected) Color.White else OwlColors.TextSecondary
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}
