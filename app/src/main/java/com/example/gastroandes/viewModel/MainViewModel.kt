package com.example.gastroandes.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastroandes.model.AuthCredentials
import com.example.gastroandes.model.AuthResponse
import com.example.gastroandes.network.RetrofitInstance
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    // LiveData para manejar la respuesta del login
    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> get() = _loginSuccess

    // LiveData para manejar la navegación al registro
    private val _navigateToRegister = MutableLiveData<Boolean>()
    val navigateToRegister: LiveData<Boolean> get() = _navigateToRegister

    // Función para manejar el login del usuario
    fun loginUser(email: String, password: String) {
        val credentials = AuthCredentials(email, password)

        // Llamada al backend usando Retrofit
        viewModelScope.launch {
            try {
                val response: AuthResponse = RetrofitInstance.usersApi.authenticateUser(credentials)
                // Si el login es exitoso
                _loginSuccess.value = true
            } catch (e: Exception) {
                // Si el login falla
                _loginSuccess.value = false
            }
        }
    }

    // Función para navegar a la vista de registro
    fun onRegisterClicked() {
        _navigateToRegister.value = true
    }

    // Resetear la navegación después de realizarla
    fun onNavigationComplete() {
        _navigateToRegister.value = false
    }
}
