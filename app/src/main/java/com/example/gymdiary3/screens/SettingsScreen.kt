package com.example.gymdiary3.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.domain.settings.UserSettings
import com.example.gymdiary3.ui.design.*
import com.example.gymdiary3.ui.theme.GdType
import com.example.gymdiary3.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

data class SettingsActions(
    val onBack: () -> Unit = {},
    val onUnit: (String) -> Unit = {},
    val onRest: (Int) -> Unit = {},
    val onBar: (Double) -> Unit = {},
    val onExportCsv: () -> Unit = {},
    val onBackup: () -> Unit = {},
    val onRestore: () -> Unit = {}
)

private val UNITS = listOf("kg", "lbs")
private val RESTS = listOf(30, 60, 90, 120, 180)
private val BARS = listOf(10.0, 15.0, 20.0)

@Composable
fun SettingsScreen(nav: NavHostController, vm: SettingsViewModel = hiltViewModel()) {
    val settings by vm.userSettings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun share(uri: Uri?, type: String, title: String, emptyMessage: String) {
        if (uri == null) {
            Toast.makeText(context, emptyMessage, Toast.LENGTH_SHORT).show(); return
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            this.type = type
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    // Accept JSON backups even when shared without a .json extension (e.g. via a messaging app).
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val result = vm.importJson(context, uri)
            Toast.makeText(
                context,
                if (result.isSuccess) "Backup restored" else "Couldn't restore: ${result.exceptionOrNull()?.message ?: "unreadable file"}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    SettingsContent(
        settings,
        SettingsActions(
            onBack = { nav.popBackStack() },
            onUnit = vm::updateWeightUnit,
            onRest = vm::updateDefaultRestSeconds,
            onBar = vm::updateBarWeight,
            onExportCsv = { scope.launch { share(vm.exportCsv(context), "text/csv", "Export CSV", "No workouts to export yet") } },
            onBackup = { scope.launch { share(vm.exportJson(context), "application/json", "Back up Gym Diary", "Couldn't create the backup") } },
            onRestore = { restoreLauncher.launch(arrayOf("application/json", "application/octet-stream", "text/plain", "*/*")) }
        )
    )
}

@Composable
fun SettingsContent(settings: UserSettings, actions: SettingsActions, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        DetailTopBar("Settings", onBack = actions.onBack)
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = Gd.s8)) {
            item(key = "units") {
                SectionHeader("Units", top = Gd.s3)
                SegmentedControl(UNITS, UNITS.indexOf(settings.weightUnit).coerceAtLeast(0), { actions.onUnit(UNITS[it]) }, Modifier.gutter())
            }
            item(key = "rest") {
                SectionHeader("Default rest")
                SegmentedControl(
                    RESTS.map { if (it < 120) "${it}s" else "${it / 60} min" },
                    RESTS.indexOf(settings.defaultRestSeconds).coerceAtLeast(0),
                    { actions.onRest(RESTS[it]) },
                    Modifier.gutter()
                )
                Text("Starts after every logged set", style = GdType.meta, color = Gd.TextMuted, modifier = Modifier.gutter().padding(top = Gd.s2))
            }
            item(key = "bar") {
                SectionHeader("Bar weight")
                SegmentedControl(
                    BARS.map { "${it.toInt()} kg" },
                    BARS.indexOf(settings.barWeight).coerceAtLeast(0),
                    { actions.onBar(BARS[it]) },
                    Modifier.gutter()
                )
                Text("Used by the plate calculator", style = GdType.meta, color = Gd.TextMuted, modifier = Modifier.gutter().padding(top = Gd.s2))
            }
            item(key = "data") {
                SectionHeader("Data")
                ListRow("Export CSV", subtitle = "Workouts and body weight as a spreadsheet", trailing = { Chevron() }, onClick = actions.onExportCsv)
                Hairline()
                ListRow("Back up", subtitle = "Workouts, body weight and exercises as a JSON file", trailing = { Chevron() }, onClick = actions.onBackup)
                Hairline()
                ListRow("Restore from backup", subtitle = "Import a JSON backup; existing data is kept", trailing = { Chevron() }, onClick = actions.onRestore)
            }
        }
    }
}
