package com.example.gastroandes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.example.gastroandes.viewModel.RegisterViewModel

class RegisterActivity : AppCompatActivity() {

    // Instanciar el ViewModel usando delegación de propiedades
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_v2)

        // Ajusta los padding para evitar la superposición con las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Obtener los campos del formulario
        val nameField = findViewById<EditText>(R.id.name_field)
        val lastNameField = findViewById<EditText>(R.id.lastname_field)
        val emailField = findViewById<EditText>(R.id.email_field)
        val passwordField = findViewById<EditText>(R.id.password_field)
        val registerButton = findViewById<Button>(R.id.register_button)

        // Configurar el botón de "¿Ya tienes cuenta? Login"
        val loginButton = findViewById<Button>(R.id.login_button)
        loginButton.setOnClickListener {
            // Llamar al ViewModel para iniciar la navegación
            viewModel.onLoginClicked()
        }

        // Observar el LiveData de navegación
        viewModel.navigateToLogin.observe(this, Observer { shouldNavigate ->
            if (shouldNavigate) {
                // Navegar a la pantalla de Login
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                viewModel.onNavigationComplete()
            }
        })

        // Configurar el botón de registro
        registerButton.setOnClickListener {
            val firstName = nameField.text.toString().trim()
            val lastName = lastNameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (firstName.isNotEmpty() && lastName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.registerUser(firstName, lastName, email, password)
            } else {
                Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Observar el resultado del registro
        viewModel.registerSuccess.observe(this, Observer { isSuccess ->
            if (isSuccess) {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()

                // Redirigir al login cuando el registro sea exitoso
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()  // Cierra la actividad actual para que el usuario no pueda regresar a ella
            } else {
                Toast.makeText(this, "Error en el registro", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
