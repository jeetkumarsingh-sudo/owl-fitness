package com.example.gymdiary3.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                color = Color(0xFF2A2000),
                shape = RoundedCornerShape(50.dp) // full pill
            )
            .border(1.dp, Color(0xFFF0A500), RoundedCornerShape(50.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("★", color = Color(0xFFF0A500), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("PR", color = Color(0xFFFFD166), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        }
    }
}
