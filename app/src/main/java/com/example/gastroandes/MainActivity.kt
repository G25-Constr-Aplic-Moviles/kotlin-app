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

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SessionManager.init(this)

        enableEdgeToEdge()
        setContentView(R.layout.login_v1)
        val registerButton = findViewById<Button>(R.id.register_button)

        val fullText = "¿No tienes cuenta? Regístrate"
        val spannableString = SpannableString(fullText)

        spannableString.setSpan(
            ForegroundColorSpan(Color.BLUE),
            fullText.indexOf("Regístrate"),
            fullText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            UnderlineSpan(),
            fullText.indexOf("Regístrate"),
            fullText.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        registerButton.text = spannableString
        registerButton.setOnClickListener {
            viewModel.onRegisterClicked()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkUserAuthentication()

        val emailField = findViewById<EditText>(R.id.email_field)
        val passwordField = findViewById<EditText>(R.id.password_field)
        val loginButton = findViewById<Button>(R.id.login_button)

        viewModel.navigateToRegister.observe(this, Observer { shouldNavigate ->
            if (shouldNavigate) {
                val intent = Intent(this, RegisterActivity::class.java)
                startActivity(intent)
                viewModel.onNavigationComplete()
            }
        })

        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (!isValidEmail(email)) {
                Toast.makeText(this, "Por favor ingresa un email válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isNotEmpty() && password.isNotEmpty()) {
                if (isNetworkAvailable()) {
                    // Si hay conexión, procede con el login
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            viewModel.loginUser(email, password)
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Ocurrió un error desconocido. Por favor, intenta nuevamente.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                } else {
                    // Mostrar mensaje de error específico cuando no hay conexión a Internet
                    Toast.makeText(
                        this,
                        "No hay conexión a Internet. Intenta más tarde.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loginSuccess.observe(this, Observer { isSuccess ->
            if (isSuccess) {
                Toast.makeText(this, "Login exitoso", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, RestaurantListActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(
                    this,
                    "Error en el login: Verifica tus credenciales o intenta más tarde.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(Regex(emailRegex))
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun checkUserAuthentication() {
        val token = SessionManager.getAuthToken()

        if (token != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val user = RetrofitInstance.usersApi.getUserInfo("Bearer $token")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Usuario autenticado", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@MainActivity, RestaurantListActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Sesión expirada, por favor inicia sesión.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
