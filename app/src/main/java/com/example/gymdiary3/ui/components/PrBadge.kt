package com.example.gymdiary3.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymdiary3.ui.theme.OwlColors

@Composable
fun PrBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(OwlColors.CrimsonGlow, RoundedCornerShape(50))
            .border(1.dp, OwlColors.Crimson, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            Icons.Filled.Bolt,
            contentDescription = null,
            tint = OwlColors.CrimsonSoft,
            modifier = Modifier.size(12.dp)
        )
        Text(
            "PR",
            color = OwlColors.CrimsonSoft,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.8.sp
        )
    }
}
