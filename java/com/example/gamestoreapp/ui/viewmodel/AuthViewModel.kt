package com.example.gamestoreapp.ui.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamestoreapp.data.LoginRequest
import com.example.gamestoreapp.data.LoginResponse
import com.example.gamestoreapp.data.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _loginResult = MutableStateFlow<LoginResponse?>(null)
    val loginResult: StateFlow<LoginResponse?> = _loginResult.asStateFlow()

    fun onEmailChange(newEmail: String) { _email.value = newEmail }
    fun onPasswordChange(newPassword: String) { _password.value = newPassword }

    private fun validateFields(): Boolean {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _errorMessage.value = "Correo y contraseña no pueden estar vacíos."
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(_email.value).matches()) {
            _errorMessage.value = "Formato de correo electrónico inválido."
            return false
        }
        return true
    }

    fun login() {
        if (validateFields()) {
            performAuthAction {
                val request = LoginRequest(email = _email.value, password = _password.value)
                RetrofitClient.api.login(request)
            }
        }
    }

    fun signUp() {
        if (validateFields()) {
            performAuthAction {
                val request = LoginRequest(email = _email.value, password = _password.value)
                RetrofitClient.api.signup(request)
            }
        }
    }

    private fun performAuthAction(apiCall: suspend () -> LoginResponse) {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _loginResult.value = apiCall()
            } catch (e: Exception) {
                _errorMessage.value = "Error: Verifica tus datos o conexión."
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetLoginState() {
        _loginResult.value = null
        _errorMessage.value = null
        _email.value = ""
        _password.value = ""
    }
}