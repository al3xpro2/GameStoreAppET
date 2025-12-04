package com.example.gamestoreapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Definimos el esquema OSCURO como el principal (Estilo Azul/Tech)
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = Color.White,

    secondary = AccentCyan, // Cyan brillante como acento principal
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1A759F), // Cyan oscuro

    background = BackgroundDark, // Azul muy oscuro
    onBackground = TextWhite,

    surface = SurfaceDark, // Gris azulado oscuro
    onSurface = TextWhite,

    surfaceVariant = SurfaceLight,
    onSurfaceVariant = TextGray,

    error = ErrorRed,
    onError = Color.White
)

// Esquema CLARO (Solo si se fuerza)
private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryDark,

    background = Color(0xFFF2F4F7), // Gris muy claro
    onBackground = Color.Black,

    surface = Color.White,
    onSurface = Color.Black,

    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color.Black
)

@Composable
fun GameStoreAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Pintamos la barra de estado del color del fondo (BackgroundDark)
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false // Forzamos iconos claros
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}