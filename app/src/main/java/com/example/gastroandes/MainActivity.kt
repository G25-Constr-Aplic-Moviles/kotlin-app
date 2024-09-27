package com.example.gastroandes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.example.gastroandes.viewModel.MainViewModel

class MainActivity : AppCompatActivity() {

    // Instanciar el ViewModel usando delegación de propiedades
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_v1)

        // Ajusta los padding para evitar la superposición con las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener los campos del formulario de login
        val emailField = findViewById<EditText>(R.id.email_field)
        val passwordField = findViewById<EditText>(R.id.password_field)
        val loginButton = findViewById<Button>(R.id.login_button)

        // Configurar el botón de "¿No tienes cuenta? Regístrate"
        val registerButton = findViewById<Button>(R.id.register_button)
        registerButton.setOnClickListener {
            viewModel.onRegisterClicked()
        }

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
}
