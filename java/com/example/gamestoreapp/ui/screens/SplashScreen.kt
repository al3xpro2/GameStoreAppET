package com.example.gamestoreapp.ui.screens

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.gamestoreapp.R
import com.example.gamestoreapp.navigation.AppScreen
import com.example.gamestoreapp.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val loginResult by authViewModel.loginResult.collectAsState()

    // Estado para la animación de escala
    val scale = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // 1. Animación de entrada con efecto rebote
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 800,
                easing = {
                    OvershootInterpolator(2f).getInterpolation(it)
                }
            )
        )

        // 2. Espera para completar el tiempo de splash (2 segundos total)
        delay(1200)

        // 3. Decisión de navegación
        if (loginResult != null) {
            navController.navigate(AppScreen.CatalogScreen.route) {
                popUpTo(AppScreen.SplashScreen.route) { inclusive = true }
            }
        } else {
            navController.navigate(AppScreen.LoginScreen.route) {
                popUpTo(AppScreen.SplashScreen.route) { inclusive = true }
            }
        }
    }

    // --- DISEÑO GAMER ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background), // Fondo Azul Oscuro Profundo
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo animado
            Image(
                painter = painterResource(id = R.drawable.logo_gamestore),
                contentDescription = "Logo GameStore",
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Texto de Marca estilo Cyberpunk
            Text(
                text = "GAME STORE",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary, // Azul Profundo
                fontWeight = FontWeight.Black,
                modifier = Modifier.scale(scale.value)
            )
        }
    }
}