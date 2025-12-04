package com.example.gamestoreapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gamestoreapp.data.GameRepository
import com.example.gamestoreapp.data.ProductResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface GamesUiState {
    data class Success(val games: List<ProductResponse>) : GamesUiState
    data object Error : GamesUiState
    data object Loading : GamesUiState
}

class GamesViewModel(private val gameRepository: GameRepository) : ViewModel() {

    private val _gamesState = MutableStateFlow<GamesUiState>(GamesUiState.Loading)
    val gamesState: StateFlow<GamesUiState> = _gamesState.asStateFlow()

    // 🔹 PROPIEDAD AÑADIDA: 'games'
    // Expone la lista de juegos directamente como StateFlow<List<ProductResponse>>.
    // Esto facilita su uso en pantallas que no requieren manejar estados de carga complejos
    // o para búsquedas rápidas (como en ProductEditScreen).
    val games: StateFlow<List<ProductResponse>> = _gamesState
        .map { state ->
            if (state is GamesUiState.Success) state.games else emptyList()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedGame = MutableStateFlow<ProductResponse?>(null)
    val selectedGame: StateFlow<ProductResponse?> = _selectedGame.asStateFlow()

    init {
        fetchGames()
    }

    fun fetchGames() {
        // Solo emitimos Loading si no tenemos datos previos cargados exitosamente para evitar parpadeos
        if (_gamesState.value !is GamesUiState.Success) {
            _gamesState.value = GamesUiState.Loading
        }

        viewModelScope.launch {
            try {
                val gamesList = gameRepository.getGames()
                _gamesState.value = GamesUiState.Success(gamesList)
            } catch (e: Exception) {
                // Si falla y ya teníamos datos, podríamos decidir mantenerlos o mostrar error
                _gamesState.value = GamesUiState.Error
            }
        }
    }

    fun findGameById(gameId: String?) {
        val id = gameId?.toIntOrNull()
        if (id != null) {
            // Buscamos en la lista actual expuesta por 'games'
            val currentGames = games.value
            _selectedGame.value = currentGames.find { it.id == id }

            // Si la lista estaba vacía (quizás no cargó aun), intentamos buscar en el estado
            if (_selectedGame.value == null && _gamesState.value is GamesUiState.Success) {
                _selectedGame.value = (_gamesState.value as GamesUiState.Success).games.find { it.id == id }
            }
        } else {
            _selectedGame.value = null
        }
    }
}

class GamesViewModelFactory(
    private val gameRepository: GameRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GamesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GamesViewModel(gameRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}