package com.example.gymdiary3.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.gymdiary3.domain.model.EquipmentType
import com.example.gymdiary3.domain.model.Exercise
import com.example.gymdiary3.domain.model.MovementPattern
import com.example.gymdiary3.domain.model.TrackingType
import com.example.gymdiary3.presentation.workout.PickerRow
import com.example.gymdiary3.presentation.workout.PickerUiState
import com.example.gymdiary3.ui.design.*
import com.example.gymdiary3.ui.theme.GdType
import com.example.gymdiary3.viewmodel.PickerViewModel

data class PickerActions(
    val onBack: () -> Unit = {},
    val onQuery: (String) -> Unit = {},
    val onMuscle: (String?) -> Unit = {},
    val onPick: (PickerRow) -> Unit = {},
    val onCreate: (String, String, EquipmentType, MovementPattern, TrackingType) -> Unit = { _, _, _, _, _ -> },
    val onDelete: (Exercise) -> Unit = {}
)

@Composable
fun ExercisePickerRoute(nav: NavHostController, vm: PickerViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val muscle by vm.muscle.collectAsStateWithLifecycle()
    val s = state ?: return
    ExercisePickerScreen(
        state = s, query = query, muscle = muscle,
        actions = PickerActions(
            onBack = { nav.popBackStack() },
            onQuery = { vm.query.value = it },
            onMuscle = { vm.muscle.value = it },
            onPick = { row -> nav.navigate(loggerRoute(row.muscle, row.name)) { popUpTo("picker") { inclusive = true } } },
            onCreate = vm::add,
            onDelete = vm::delete
        )
    )
}

@Composable
fun ExercisePickerScreen(
    state: PickerUiState,
    query: String,
    muscle: String?,
    actions: PickerActions,
    modifier: Modifier = Modifier
) {
    var creating by remember { mutableStateOf(false) }

    Column(modifier.fillMaxSize()) {
        DetailTopBar(
            title = "Add exercise",
            onBack = actions.onBack,
            actions = {
                IconButton(onClick = { creating = true }) {
                    Icon(Icons.Default.Add, contentDescription = "New exercise", tint = Gd.Text)
                }
            }
        )

        OutlinedTextField(
            value = query,
            onValueChange = actions.onQuery,
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = Gd.TextMuted) },
            placeholder = { Text("Search exercises", style = GdType.body, color = Gd.TextFaint) },
            textStyle = GdType.body.copy(color = Gd.Text),
            modifier = Modifier.fillMaxWidth().gutter(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gd.Accent, unfocusedBorderColor = Gd.BorderInput, cursorColor = Gd.Accent,
                focusedContainerColor = Gd.Surface, unfocusedContainerColor = Gd.Surface
            )
        )

        MuscleFilter(state.muscles, muscle, actions.onMuscle)

        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = Gd.s8)) {
            if (state.recent.isNotEmpty()) {
                item(key = "recentHeader") { SectionHeader("Recent", top = Gd.s3) }
                itemsIndexed(state.recent, key = { _, r -> "recent_${r.name}" }) { i, row ->
                    PickerItem(row, actions)
                    if (i < state.recent.lastIndex) Hairline()
                }
            }
            item(key = "allHeader") { SectionHeader(muscle ?: if (query.isBlank()) "All exercises" else "Results") }
            if (state.all.isEmpty()) {
                item(key = "empty") {
                    EmptyMessage(
                        title = "No match",
                        body = if (query.isBlank()) "No exercises for this muscle yet." else "Nothing called \"$query\" yet.",
                        action = { SecondaryButton("Create exercise", onClick = { creating = true }, compact = true) }
                    )
                }
            }
            itemsIndexed(state.all, key = { _, r -> "all_${r.name}" }) { i, row ->
                PickerItem(row, actions)
                if (i < state.all.lastIndex) Hairline()
            }
        }
    }

    if (creating) {
        CreateExerciseDialog(
            initialName = query.trim(),
            initialMuscle = muscle ?: state.muscles.first(),
            muscles = state.muscles,
            onCreate = { n, m, e, p, t -> actions.onCreate(n, m, e, p, t); creating = false },
            onDismiss = { creating = false }
        )
    }
}

@Composable
private fun MuscleFilter(muscles: List<String>, selected: String?, onSelect: (String?) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Gd.s3, vertical = Gd.s2)
    ) {
        (listOf<String?>(null) + muscles).forEach { m ->
            val on = m == selected
            Column(
                Modifier
                    .clickable { onSelect(m) }
                    .padding(horizontal = Gd.s2, vertical = Gd.s2),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    m ?: "All",
                    style = GdType.label.copy(fontWeight = if (on) FontWeight.SemiBold else FontWeight.Medium),
                    color = if (on) Gd.Text else Gd.TextMuted
                )
                Spacer(Modifier.height(4.dp))
                Box(Modifier.width(16.dp).height(2.dp).background(if (on) Gd.Accent else androidx.compose.ui.graphics.Color.Transparent))
            }
        }
    }
}

@Composable
private fun PickerItem(row: PickerRow, actions: PickerActions) {
    var menu by remember { mutableStateOf(false) }
    Box {
        ListRow(
            title = row.name,
            subtitle = row.detail,
            onClick = { actions.onPick(row) },
            onLongClick = if (row.exercise != null) ({ menu = true }) else null
        )
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }, containerColor = Gd.SurfaceRaised) {
            DropdownMenuItem(
                text = { Text("Delete exercise", style = GdType.label, color = Gd.Danger) },
                onClick = { row.exercise?.let(actions.onDelete); menu = false }
            )
        }
    }
}

@Composable
private fun CreateExerciseDialog(
    initialName: String,
    initialMuscle: String,
    muscles: List<String>,
    onCreate: (String, String, EquipmentType, MovementPattern, TrackingType) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var muscle by remember { mutableStateOf(initialMuscle) }
    var equipment by remember { mutableStateOf(EquipmentType.OTHER) }
    var pattern by remember { mutableStateOf(MovementPattern.ISOLATION) }
    var tracking by remember { mutableStateOf(TrackingType.WEIGHT_REPS) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Gd.SurfaceRaised,
        title = { Text("New exercise", style = GdType.section, color = Gd.Text) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Gd.s3)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it }, singleLine = true,
                    label = { Text("Name") },
                    textStyle = GdType.body.copy(color = Gd.Text),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Gd.Accent, unfocusedBorderColor = Gd.BorderInput, cursorColor = Gd.Accent,
                        focusedLabelColor = Gd.TextMuted, unfocusedLabelColor = Gd.TextMuted
                    )
                )
                Choice("Muscle", muscles, muscle, { it }) { muscle = it }
                Choice("Equipment", EquipmentType.entries, equipment, ::enumLabel) { equipment = it }
                Choice("Movement", MovementPattern.entries, pattern, ::enumLabel) { pattern = it }
                Choice("Tracking", TrackingType.entries, tracking, ::enumLabel) { tracking = it }
            }
        },
        confirmButton = {
            TextAction(
                "Create", color = Gd.AccentText,
                onClick = { if (name.isNotBlank()) onCreate(name, muscle, equipment, pattern, tracking) }
            )
        },
        dismissButton = { TextAction("Cancel", onClick = onDismiss) }
    )
}

private fun enumLabel(e: Enum<*>): String =
    e.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> Choice(label: String, options: List<T>, selected: T, display: (T) -> String, onSelect: (T) -> Unit) {
    var open by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = open, onExpandedChange = { open = it }) {
        OutlinedTextField(
            value = display(selected), onValueChange = {}, readOnly = true, singleLine = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = open) },
            textStyle = GdType.body.copy(color = Gd.Text),
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Gd.Accent, unfocusedBorderColor = Gd.BorderInput,
                focusedLabelColor = Gd.TextMuted, unfocusedLabelColor = Gd.TextMuted
            )
        )
        ExposedDropdownMenu(expanded = open, onDismissRequest = { open = false }, containerColor = Gd.SurfaceRaised) {
            options.forEach { o ->
                DropdownMenuItem(
                    text = { Text(display(o), style = GdType.body, color = Gd.Text) },
                    onClick = { onSelect(o); open = false }
                )
            }
        }
    }
}
