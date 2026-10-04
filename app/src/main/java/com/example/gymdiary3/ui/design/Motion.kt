package com.example.gymdiary3.ui.design

import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import com.example.gymdiary3.ui.theme.LocalReducedMotion

/**
 * Insert/remove/reorder motion for list rows (a logged set appearing, a deleted
 * entry closing its gap). Short, token-timed, and nothing under reduced motion.
 */
@Composable
fun LazyItemScope.itemMotion(): Modifier =
    if (LocalReducedMotion.current) Modifier
    else Modifier.animateItem(
        fadeInSpec = tween(GdMotion.Base),
        placementSpec = tween<IntOffset>(GdMotion.Base, easing = GdMotion.Ease),
        fadeOutSpec = tween(GdMotion.Fast)
    )
