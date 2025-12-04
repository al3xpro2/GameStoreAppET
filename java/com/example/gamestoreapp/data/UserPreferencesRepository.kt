package com.example.gamestoreapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extensión para crear una instancia de DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

// Clase que encapsula el acceso a DataStore
class UserPreferencesRepository(private val context: Context) {

    // Claves para guardar los datos
    private object PreferencesKeys {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_ROLE = stringPreferencesKey("user_role") // <-- Clave para el rol
    }

    // Flujo que emite los datos del usuario cada vez que cambian
    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val authToken = preferences[PreferencesKeys.AUTH_TOKEN]
        val userEmail = preferences[PreferencesKeys.USER_EMAIL]
        val userRole = preferences[PreferencesKeys.USER_ROLE] // <-- Leemos el rol
        UserPreferences(authToken, userEmail, userRole)
    }

    // Función para guardar los datos de sesión
    suspend fun saveUserSession(token: String, email: String, role: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTH_TOKEN] = token
            preferences[PreferencesKeys.USER_EMAIL] = email
            preferences[PreferencesKeys.USER_ROLE] = role // <-- Guardamos el rol
        }
    }

    // Función para limpiar los datos de sesión (logout)
    suspend fun clearUserSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

// Data class para representar los datos del usuario
data class UserPreferences(
    val authToken: String?,
    val userEmail: String?,
    val userRole: String? // <-- Campo para el rol
)
