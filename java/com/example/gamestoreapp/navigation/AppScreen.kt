package com.example.gamestoreapp.navigation

sealed class AppScreen(val route: String) {
    object SplashScreen : AppScreen("splash_screen")
    object LoginScreen : AppScreen("login_screen")
    object SignUpScreen : AppScreen("sign_up_screen")
    object CatalogScreen : AppScreen("catalog_screen")
    object DetailScreen : AppScreen("detail_screen/{gameId}")
    object CartScreen : AppScreen("cart_screen")
    object AdminDashboardScreen : AppScreen("admin_dashboard_screen")
    object AdminProductListScreen : AppScreen("admin_product_list_screen")

    object ProductEditScreen : AppScreen("product_edit_screen?productId={productId}") {
        fun createRoute(productId: String?) = if (productId != null) "product_edit_screen?productId=$productId" else "product_edit_screen"
    }

    // --- ¡NUEVA RUTA AÑADIDA! ---
    object AdminOrdersScreen : AppScreen("admin_orders_screen")
    // ----------------------------
}