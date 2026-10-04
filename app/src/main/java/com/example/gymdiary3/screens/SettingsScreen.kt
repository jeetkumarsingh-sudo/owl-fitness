package com.example.gymdiary3.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.ui.components.ApexPanel
import com.example.gymdiary3.ui.components.ApexScaffold
import com.example.gymdiary3.ui.components.SectionLabel
import com.example.gymdiary3.ui.theme.Apex
import com.example.gymdiary3.viewmodel.SettingsViewModel
import androidx.hilt.navigation.compose.hiltViewModel

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

    ApexScaffold(title = "Settings", onBack = { nav.popBackStack() }) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            item {
                SectionLabel("Units")
                Spacer(Modifier.height(14.dp))
                ApexPanel(radius = Apex.radiusSm) {
                    Row(Modifier.fillMaxWidth().padding(6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        UnitButton("kg", settings.weightUnit == "kg", Modifier.weight(1f)) { viewModel.updateWeightUnit("kg") }
                        UnitButton("lbs", settings.weightUnit == "lbs", Modifier.weight(1f)) { viewModel.updateWeightUnit("lbs") }
                    }
                }
            }

            item {
                SectionLabel("Rest timer default")
                Spacer(Modifier.height(14.dp))
                ApexPanel(radius = Apex.radiusSm) {
                    Column(Modifier.padding(vertical = 8.dp)) {
                        val timerOptions = listOf(30, 60, 90, 120, 180)
                        timerOptions.forEachIndexed { index, seconds ->
                            Row(
                                Modifier.fillMaxWidth()
                                    .clickable { viewModel.updateDefaultRestSeconds(seconds) }
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = settings.defaultRestSeconds == seconds,
                                    onClick = { viewModel.updateDefaultRestSeconds(seconds) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = Apex.Accent,
                                        unselectedColor = Apex.TextMuted
                                    )
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("$seconds seconds", style = MaterialTheme.typography.bodyLarge, color = Apex.TextPrimary)
                            }
                            if (index < timerOptions.size - 1) {
                                HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Apex.Hairline)
                            }
                        }
                    }
                }
            }

            item {
                SectionLabel("Bar weight")
                Spacer(Modifier.height(14.dp))
                ApexPanel(radius = Apex.radiusSm) {
                    Row(Modifier.fillMaxWidth().padding(6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(10.0, 15.0, 20.0).forEach { weight ->
                            UnitButton("${weight.toInt()}kg", settings.barWeight == weight, Modifier.weight(1f)) {
                                viewModel.updateBarWeight(weight)
                            }
                        }
                    }
                }
            }

            item {
                SectionLabel("Data")
                Spacer(Modifier.height(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DataButton("EXPORT ALL DATA (CSV)") { onExportClick() }
                    DataButton("BACKUP DATA (JSON)") {
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
                    }
                    DataButton("RESTORE FROM BACKUP") {
                        importLauncher.launch(arrayOf("application/json"))
                    }
                }
            }

            item {
                Spacer(Modifier.height(4.dp))
                Text(
                    "APEX FITNESS",
                    style = MaterialTheme.typography.labelSmall,
                    color = Apex.TextFaint,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun DataButton(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(Apex.radiusSm),
        border = BorderStroke(1.dp, Apex.Hairline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Apex.TextSecondary)
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun UnitButton(label: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Apex.Accent else Color.Transparent,
            contentColor = if (isSelected) Color.White else Apex.TextSecondary
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}
