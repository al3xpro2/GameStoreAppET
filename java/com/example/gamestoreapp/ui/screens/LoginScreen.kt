package com.example.gamestoreapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gamestoreapp.R
import com.example.gamestoreapp.navigation.AppScreen
import com.example.gamestoreapp.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val email by authViewModel.email.collectAsState()
    val password by authViewModel.password.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val loginResult by authViewModel.loginResult.collectAsState()
    val context = LocalContext.current

    // Lógica de Redirección (Completada previamente)
    LaunchedEffect(loginResult) {
        loginResult?.let { result ->
            Toast.makeText(context, "¡Bienvenido/a!", Toast.LENGTH_SHORT).show()

            val isAdmin = result.role?.trim()?.equals("admin", ignoreCase = true) ?: false
            val destination = if (isAdmin) {
                AppScreen.AdminDashboardScreen.route
            } else {
                AppScreen.CatalogScreen.route
            }

            navController.navigate(destination) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Fondo Azul Oscuro
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- Logo de la app ---
        // Asume que R.drawable.logo_gamestore es tu logo azul metálico
        Image(
            painter = painterResource(id = R.drawable.logo_gamestore),
            contentDescription = "Logo GameStore",
            modifier = Modifier
                .size(160.dp) // Grande para impacto
                .padding(bottom = 32.dp)
        )

        // --- Título ---
        Text(
            text = "INICIAR SESIÓN",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary, // Azul Profundo
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- Campo correo ---
        OutlinedTextField(
            value = email,
            onValueChange = { authViewModel.onEmailChange(it) },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(imageVector = Icons.Default.Email, contentDescription = null)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.secondary, // Borde Cyan al enfocar
                focusedLabelColor = MaterialTheme.colorScheme.secondary,
                cursorColor = MaterialTheme.colorScheme.secondary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Campo contraseña ---
        OutlinedTextField(
            value = password,
            onValueChange = { authViewModel.onPasswordChange(it) },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null)
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.secondary, // Borde Cyan al enfocar
                focusedLabelColor = MaterialTheme.colorScheme.secondary,
                cursorColor = MaterialTheme.colorScheme.secondary
            )
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Botón ingresar ---
        Button(
            onClick = { authViewModel.login() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp), // Botón alto
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary // Azul Profundo
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "INGRESAR",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Ir a registro ---
        TextButton(onClick = { navController.navigate(AppScreen.SignUpScreen.route) }) {
            Text(
                text = "¿No tienes cuenta? Regístrate aquí",
                color = MaterialTheme.colorScheme.secondary, // Cyan Brillante
                fontSize = 16.sp
            )
        }
    }
}