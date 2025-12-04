package com.example.gamestoreapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gamestoreapp.ui.screens.*
import com.example.gamestoreapp.ui.viewmodel.*

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    gamesViewModel: GamesViewModel,
    cartViewModel: CartViewModel,
    productEditViewModel: ProductEditViewModel,
    adminOrdersViewModel: AdminOrdersViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppScreen.SplashScreen.route
    ) {
        composable(AppScreen.SplashScreen.route) {
            SplashScreen(navController, authViewModel)
        }
        composable(AppScreen.LoginScreen.route) {
            LoginScreen(navController, authViewModel)
        }
        composable(AppScreen.SignUpScreen.route) {
            SignUpScreen(navController, authViewModel)
        }
        composable(AppScreen.CatalogScreen.route) {
            CatalogScreen(navController, gamesViewModel, cartViewModel, authViewModel)
        }
        composable(
            route = "detail_screen/{gameId}",
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")
            DetailScreen(navController, gameId, gamesViewModel, cartViewModel, authViewModel)
        }
        composable(AppScreen.CartScreen.route) {
            CartScreen(navController, cartViewModel, gamesViewModel, authViewModel)
        }
        composable(AppScreen.AdminDashboardScreen.route) {
            AdminDashboardScreen(navController, authViewModel)
        }
        composable(AppScreen.AdminProductListScreen.route) {
            AdminProductListScreen(navController, gamesViewModel, productEditViewModel, authViewModel)
        }
        composable(
            route = AppScreen.ProductEditScreen.route,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            ProductEditScreen(navController, productId, gamesViewModel, productEditViewModel, authViewModel)
        }
        composable(AppScreen.AdminOrdersScreen.route) {
            AdminOrdersScreen(navController, authViewModel, adminOrdersViewModel)
        }
    }
}