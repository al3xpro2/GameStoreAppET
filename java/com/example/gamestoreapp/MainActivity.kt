package com.example.gamestoreapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gamestoreapp.data.CartRepository
import com.example.gamestoreapp.data.GameRepository
import com.example.gamestoreapp.data.OrderRepository
import com.example.gamestoreapp.navigation.AppNavigation
import com.example.gamestoreapp.ui.theme.GameStoreAppTheme
import com.example.gamestoreapp.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gameRepository = GameRepository()
        val orderRepository = OrderRepository()
        val cartRepository = CartRepository()

        // Gestores de recursos nativos
        val vibrationManager = AndroidVibrationManager()
        val uiMessageManager = AndroidUiMessageManager() // 👈 Instancia real del Toast Manager

        setContent {
            GameStoreAppTheme {
                val authViewModel: AuthViewModel = viewModel()
                val gamesViewModel: GamesViewModel = viewModel(
                    factory = GamesViewModelFactory(gameRepository)
                )

                val cartViewModel: CartViewModel = viewModel(
                    factory = CartViewModelFactory(
                        vibrationManager,
                        uiMessageManager, // 👈 Pasamos el manager a la factory
                        gameRepository,
                        cartRepository
                    )
                )

                val productEditViewModel: ProductEditViewModel = viewModel(
                    factory = ProductEditViewModelFactory(gameRepository)
                )

                val adminOrdersViewModel: AdminOrdersViewModel = viewModel(
                    factory = AdminOrdersViewModelFactory(orderRepository)
                )

                AppNavigation(
                    authViewModel = authViewModel,
                    gamesViewModel = gamesViewModel,
                    cartViewModel = cartViewModel,
                    productEditViewModel = productEditViewModel,
                    adminOrdersViewModel = adminOrdersViewModel
                )
            }
        }
    }
}