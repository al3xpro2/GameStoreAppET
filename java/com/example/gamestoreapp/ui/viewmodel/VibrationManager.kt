package com.example.gamestoreapp.ui.viewmodel

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Interfaz que abstrae la funcionalidad de vibración para que
 * los ViewModels no dependan directamente del sistema Android
 * y así poder testearlos.
 */
interface VibrationManager {
    fun vibrate(context: Context)
}

/**
 * Implementación real que usa los servicios de Android para vibrar el dispositivo.
 * Esta se usará en la aplicación en producción.
 */
class AndroidVibrationManager : VibrationManager {
    override fun vibrate(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        // Se usa VibrationEffect para controlar la vibración (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    100,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }
}

/**
 * Una implementación "falsa" o nula que no hace nada.
 * La usaremos en las previsualizaciones de Compose y en los tests
 * para evitar errores.
 */
class NoOpVibrationManager : VibrationManager {
    override fun vibrate(context: Context) {
        // No hace absolutamente nada
    }
}