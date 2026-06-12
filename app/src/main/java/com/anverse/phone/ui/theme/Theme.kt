package com.anverse.phone.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WhiteColorScheme = lightColorScheme(
    primary = Color(0xFF3B82F6),
    secondary = Color(0xFF4ADE80),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF111827),
    error = Color(0xFFEF4444)
)

@Composable
fun AnverseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = WhiteColorScheme,
        content = content
    )
}
