package com.example.gymdiary3.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymdiary3.ui.theme.OwlColors

@Composable
fun LoadingOverlay(
    message: String = "PROCESSING..."
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OwlColors.DeepBg.copy(alpha = 0.82f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = OwlColors.Crimson,
                trackColor = OwlColors.BorderSubtle,
                strokeWidth = 4.dp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                color = OwlColors.TextSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                fontSize = 12.sp
            )
        }
    }
}
