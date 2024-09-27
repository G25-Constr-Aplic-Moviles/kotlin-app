package com.example.gastroandes.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastroandes.model.User
import com.example.gastroandes.network.RetrofitInstance
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    // LiveData para manejar la respuesta del registro
    private val _registerSuccess = MutableLiveData<Boolean>()
    val registerSuccess: LiveData<Boolean> get() = _registerSuccess

    // LiveData para manejar la navegación
    private val _navigateToLogin = MutableLiveData<Boolean>()
    val navigateToLogin: LiveData<Boolean> get() = _navigateToLogin

    // Función para manejar el registro del usuario
    fun registerUser(firstName: String, lastName: String, email: String, password: String) {
        val user = User(firstName, lastName, email, password)

        // Llamada al backend usando Retrofit
        viewModelScope.launch {
            try {
                RetrofitInstance.usersApi.createUser(user)
                _registerSuccess.value = true
            } catch (e: Exception) {
                _registerSuccess.value = false
            }
        }
    }

    // Función para navegar a la vista de login
    fun onLoginClicked() {
        _navigateToLogin.value = true
    }

    // Resetear la navegación después de realizarla
    fun onNavigationComplete() {
        _navigateToLogin.value = false
    }
}
