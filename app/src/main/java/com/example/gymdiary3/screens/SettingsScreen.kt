package com.example.gymdiary3.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.launch
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

import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    nav: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel(),
    onExportClick: () -> Unit
) {
    val settings by viewModel.userSettings.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                scope.launch {
                    val result = viewModel.importJson(context, it)
                    if (result.isSuccess) {
                        Toast.makeText(context, "Backup imported successfully", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Import failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    )

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
                                    text = "$seconds seconds",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = OwlColors.TextPrimary
                                )
                            }
                            if (index < (timerOptions.size - 1)) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = OwlColors.BorderSubtle
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "BAR WEIGHT",
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
                    val barOptions = listOf(10.0, 15.0, 20.0)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        barOptions.forEach { weight ->
                            UnitButton(
                                label = "${weight.toInt()}kg",
                                isSelected = settings.barWeight == weight,
                                modifier = Modifier.weight(1f),
                                onClick = { viewModel.updateBarWeight(weight) }
                            )
                        }
                    }
                }
            }

            item {
                Text("DATA", color = OwlColors.PurpleSoft, style = MaterialTheme.typography.labelMedium, letterSpacing = 1.2.sp)
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { onExportClick() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, OwlColors.BorderSubtle),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OwlColors.TextSecondary)
                    ) {
                        Text("EXPORT ALL DATA (CSV)", style = MaterialTheme.typography.labelLarge)
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val uri = viewModel.exportJson(context)
                                if (uri != null) {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/json"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Export JSON Backup"))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, OwlColors.BorderSubtle),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OwlColors.TextSecondary)
                    ) {
                        Text("BACKUP DATA (JSON)", style = MaterialTheme.typography.labelLarge)
                    }

                    OutlinedButton(
                        onClick = {
                            importLauncher.launch(arrayOf("application/json"))
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, OwlColors.BorderSubtle),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OwlColors.TextSecondary)
                    ) {
                        Text("RESTORE FROM BACKUP", style = MaterialTheme.typography.labelLarge)
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
