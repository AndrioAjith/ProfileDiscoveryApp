package com.example.profilediscoveryapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ProfileDiscoveryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = PrimaryLight,
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onPrimary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White,
            error = ErrorRed
        )
    } else {
        lightColorScheme(
            primary = PrimaryColor,
            background = BackgroundWhite,
            surface = BackgroundWhite,
            onPrimary = Color.White,
            onBackground = TextBlack,
            onSurface = TextBlack,
            error = ErrorRed
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}