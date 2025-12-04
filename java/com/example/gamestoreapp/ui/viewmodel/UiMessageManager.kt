package com.example.gamestoreapp.ui.viewmodel

import android.content.Context
import android.widget.Toast

/**
 * Interfaz para abstraer los mensajes de UI (Toasts).
 * Permite hacer pruebas unitarias sin depender de la clase Toast de Android.
 */
interface UiMessageManager {
    fun showToast(context: Context, message: String)
}

/**
 * Implementación real para la App (usa Toast de Android).
 */
class AndroidUiMessageManager : UiMessageManager {
    override fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Implementación "falsa" para Tests (no hace nada).
 * Evita el error "Method makeText not mocked".
 */
class NoOpUiMessageManager : UiMessageManager {
    override fun showToast(context: Context, message: String) {
        // No hace nada, ideal para tests
    }
}