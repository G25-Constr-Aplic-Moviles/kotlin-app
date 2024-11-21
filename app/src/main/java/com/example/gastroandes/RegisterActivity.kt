package com.example.gastroandes

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.example.gastroandes.viewModel.RegisterViewModel
import com.google.android.material.snackbar.Snackbar

class RegisterActivity : AppCompatActivity() {

    // Instanciar el ViewModel usando delegación de propiedades
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_v2)

        val loginButton = findViewById<Button>(R.id.login_button)

        // Crear el texto con formato
        val fullText = "¿Ya tienes una cuenta? Login"
        val spannableString = SpannableString(fullText)

        // Aplicar color azul y subrayado solo a "Login"
        spannableString.setSpan(
            ForegroundColorSpan(Color.BLUE), // Color azul
            fullText.indexOf("Login"), // Inicio de "Login"
            fullText.length, // Fin de "Login"
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            UnderlineSpan(), // Subrayado
            fullText.indexOf("Login"), // Inicio de "Login"
            fullText.length, // Fin de "Login"
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Configurar el texto formateado en el botón
        loginButton.text = spannableString

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

        // Configurar el botón de registro con validaciones
        registerButton.setOnClickListener {
            val firstName = nameField.text.toString().trim()
            val lastName = lastNameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            when {
                !isValidName(firstName) -> showToast("Por favor ingresa un nombre válido.")
                !isValidName(lastName) -> showToast("Por favor ingresa un apellido válido.")
                !isValidEmail(email) -> showToast("Por favor ingresa un email válido.")
                !isValidPassword(password) -> showSnackbar("La contraseña debe tener al menos una mayúscula, un número y un carácter especial.")
                else -> if (isNetworkAvailable()) {viewModel.registerUser(firstName, lastName, email, password)} else {
                    Toast.makeText(
                        this,
                        "No hay conexión a Internet. Intenta más tarde.",
                        Toast.LENGTH_LONG
                    ).show()
                }
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

    // Validación del nombre
    private fun isValidName(name: String): Boolean {
        val nameRegex = "^[A-Za-záéíóúÁÉÍÓÚñÑ]+( [A-Za-záéíóúÁÉÍÓÚñÑ]+)*$"
        return name.matches(Regex(nameRegex)) && name.isNotBlank()
    }

    // Validación del email
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(Regex(emailRegex))
    }

    // Validación de la contraseña
    private fun isValidPassword(password: String): Boolean {
        val passwordRegex = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#\$%^&+=!]).{6,}$"
        return password.matches(Regex(passwordRegex))
    }

    // Función auxiliar para mostrar un mensaje Toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}
