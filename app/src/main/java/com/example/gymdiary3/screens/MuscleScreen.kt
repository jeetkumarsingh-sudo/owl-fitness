package com.example.gymdiary3.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.gymdiary3.ui.components.ApexPanel
import com.example.gymdiary3.ui.components.ApexScaffold
import com.example.gymdiary3.ui.components.appear
import com.example.gymdiary3.ui.theme.Apex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuscleScreen(nav: NavHostController) {
    val muscles = remember {
        listOf("Chest", "Back", "Legs", "Shoulders", "Biceps", "Triceps", "Abs")
    }

    ApexScaffold(title = "Select Muscle", onBack = { nav.popBackStack() }) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(muscles, key = { _, m -> m }) { index, muscle ->
                MuscleCard(muscle, Modifier.appear(index)) { nav.navigate("exercise/$muscle") }
            }
        }
    }
}

@Composable
private fun MuscleCard(muscle: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    ApexPanel(modifier = modifier.height(130.dp), onClick = onClick) {
        Column(
            Modifier.fillMaxSize().padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                Modifier.size(38.dp).background(Apex.AccentWash, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = Apex.AccentSoft,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                muscle.uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = Apex.TextPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
