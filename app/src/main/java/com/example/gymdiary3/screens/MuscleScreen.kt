package com.example.gymdiary3.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import com.example.gymdiary3.ui.theme.BackgroundDark
import com.example.gymdiary3.ui.theme.CardDark
import com.example.gymdiary3.ui.theme.PrimaryText
import com.example.gymdiary3.ui.theme.Accent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuscleScreen(nav: NavHostController) {

    val muscles = remember {
        listOf("Chest", "Back", "Legs", "Shoulders", "Biceps", "Triceps", "Abs")
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(BackgroundDark),
        topBar = { 
            TopAppBar(
                title = { Text("SELECT MUSCLE", fontWeight = FontWeight.ExtraBold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark,
                    titleContentColor = PrimaryText
                )
            ) 
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(BackgroundDark),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(muscles, key = { it }) { muscle ->
                Card(
                    onClick = { nav.navigate("exercise/$muscle") },
                    modifier = Modifier.height(110.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CardDark
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text(
                            muscle.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Accent
                        )
                    }
                }
            }
        }
    }
}
