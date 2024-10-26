package com.example.gastroandes

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.example.gastroandes.model.SessionManager
import com.example.gastroandes.network.RetrofitInstance
import com.example.gastroandes.viewModel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    // Instanciar el ViewModel usando delegación de propiedades
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar el SessionManager
        SessionManager.init(this)

        enableEdgeToEdge()
        setContentView(R.layout.login_v1)
        val registerButton = findViewById<Button>(R.id.register_button)

        // Crear el texto con formato
        val fullText = "¿No tienes cuenta? Regístrate"
        val spannableString = SpannableString(fullText)

        // Aplicar color azul y subrayado solo a "Regístrate"
        spannableString.setSpan(
            ForegroundColorSpan(Color.BLUE), // Color azul
            fullText.indexOf("Regístrate"), // Inicio de "Regístrate"
            fullText.length, // Fin de "Regístrate"
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            UnderlineSpan(), // Subrayado
            fullText.indexOf("Regístrate"), // Inicio de "Regístrate"
            fullText.length, // Fin de "Regístrate"
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Configurar el texto formateado en el botón
        registerButton.text = spannableString

        // Configurar el clic del botón para navegar al registro
        registerButton.setOnClickListener {
            viewModel.onRegisterClicked()
        }

        // Ajusta los padding para evitar la superposición con las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Verificar si el usuario ya está autenticado
        checkUserAuthentication()

        // Obtener los campos del formulario de login
        val emailField = findViewById<EditText>(R.id.email_field)
        val passwordField = findViewById<EditText>(R.id.password_field)
        val loginButton = findViewById<Button>(R.id.login_button)

        // Observar el LiveData de navegación al registro
        viewModel.navigateToRegister.observe(this, Observer { shouldNavigate ->
            if (shouldNavigate) {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
                viewModel.onNavigationComplete()
            }
        })

        // Configurar el botón de login
        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            // Validar el formato del email
            if (!isValidEmail(email)) {
                Toast.makeText(this, "Por favor ingresa un email válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.loginUser(email, password)
            } else {
                Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Observar el resultado del login
        viewModel.loginSuccess.observe(this, Observer { isSuccess ->
            if (isSuccess) {
                Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show()
                // Redirigir a la actividad RestaurantListActivity
                val intent = Intent(this, RestaurantListActivity::class.java)
                startActivity(intent)
                finish()  // Cierra la actividad de login
            } else {
                Toast.makeText(this, "Error en el login. Verifica tus credenciales.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(Regex(emailRegex))
    }

    // Función para verificar si el usuario ya está autenticado
    private fun checkUserAuthentication() {
        // Verificar si hay un token guardado
        val token = SessionManager.getAuthToken()

        if (token != null) {
            // Hacer la petición para obtener la información del usuario con el token
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val user = RetrofitInstance.usersApi.getUserInfo("Bearer $token")
                    withContext(Dispatchers.Main) {
                        // Si el token es válido y el usuario es autenticado
                        Toast.makeText(this@MainActivity, "Usuario autenticado", Toast.LENGTH_SHORT).show()
                        // Redirigir a RestaurantListActivity
                        val intent = Intent(this@MainActivity, RestaurantListActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } catch (e: Exception) {
                    // Si el token no es válido o está vencido, se atrapa la excepción
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Sesión expirada, por favor inicia sesión.", Toast.LENGTH_SHORT).show()
                        // Continuar con el flujo normal de login (no hacer nada)
                    }
                }
            }
        }
    }
}
