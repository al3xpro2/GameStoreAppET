package com.example.gamestoreapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gamestoreapp.data.OrderRepository
import com.example.gamestoreapp.data.OrderResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminOrdersViewModel(private val orderRepository: OrderRepository) : ViewModel() {

    private val _orders = MutableStateFlow<List<OrderResponse>>(emptyList())
    val orders = _orders.asStateFlow()

    /**
     * Función de ordenamiento: Pendientes primero, luego por fecha más reciente.
     * Es clave para el perfil de administrador.
     */
    private fun sortOrders(list: List<OrderResponse>): List<OrderResponse> {
        val priority = mapOf(
            "pendiente" to 0,
            "enviado" to 1,
            "entregado" to 2, // Añadido 'entregado' a la prioridad
            "rechazado" to 3
        )
        return list.sortedWith(
            compareBy<OrderResponse> { priority[it.status.lowercase()] ?: 99 }
                .thenByDescending { it.createdAt } // Las más nuevas primero dentro de cada grupo
        )
    }

    /**
     * Obtiene la lista de órdenes desde Xano y las ordena.
     */
    fun fetchOrders(authToken: String) {
        viewModelScope.launch {
            try {
                // Asumiendo que getOrders retorna List<OrderResponse> o null/vacío si falla
                val fetched = orderRepository.getOrders(authToken) ?: emptyList()
                _orders.value = sortOrders(fetched)
            } catch (e: Exception) {
                e.printStackTrace()
                _orders.value = emptyList()
            }
        }
    }

    /**
     * Actualiza el estado de una orden en Xano y refresca la lista local.
     */
    fun updateOrderStatus(authToken: String, orderId: Int, newStatus: String) {
        viewModelScope.launch {
            try {
                // Asumiendo que updateOrderStatus retorna Boolean indicando éxito
                val ok = orderRepository.updateOrderStatus(authToken, orderId, newStatus)
                if (ok) {
                    // Actualizamos la lista localmente para reflejar el cambio sin recargar todo
                    val updated = _orders.value.map { order ->
                        if (order.id == orderId) order.copy(status = newStatus) else order
                    }
                    _orders.value = sortOrders(updated)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class AdminOrdersViewModelFactory(private val orderRepository: OrderRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminOrdersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminOrdersViewModel(orderRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}