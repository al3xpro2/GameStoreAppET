package com.example.gamestoreapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    // Títulos grandes (Nombres de juegos en detalle)
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        letterSpacing = 0.5.sp
    ),
    // Títulos medianos (Nombres en AppBar y Dashboard Admin)
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        letterSpacing = 0.5.sp
    ),
    // Títulos pequeños (Títulos de tarjeta, mensajes de diálogo)
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    // Títulos muy pequeños (Nombres en tarjetas de catálogo)
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        letterSpacing = 0.15.sp
    ),
    // Cuerpo de texto principal (Descripciones, párrafos)
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // Etiquetas grandes (Precios, Botones de acción)
    labelLarge = TextStyle(
        fontFamily = FontFamily.Monospace, // Toque técnico/digital para precios
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        letterSpacing = 1.sp
    ),
    // Etiquetas pequeñas
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)