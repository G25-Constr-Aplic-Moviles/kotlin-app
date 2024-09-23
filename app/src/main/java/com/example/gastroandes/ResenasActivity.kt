package com.example.gastroandes
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity



class ResenasActivity : AppCompatActivity() {

    private lateinit var stars: Array<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.resenas_v11)

        // Obtener referencias de las estrellas
        val star1: ImageView = findViewById(R.id.star1)
        val star2: ImageView = findViewById(R.id.star2)
        val star3: ImageView = findViewById(R.id.star3)
        val star4: ImageView = findViewById(R.id.star4)
        val star5: ImageView = findViewById(R.id.star5)

        // Poner las estrellas en un array para facilitar su manipulación
        stars = arrayOf(star1, star2, star3, star4, star5)

        // Añadir listeners a cada estrella
        star1.setOnClickListener { updateStars(1) }
        star2.setOnClickListener { updateStars(2) }
        star3.setOnClickListener { updateStars(3) }
        star4.setOnClickListener { updateStars(4) }
        star5.setOnClickListener { updateStars(5) }
    }

    // Función para actualizar el color de las estrellas
    private fun updateStars(selectedStars: Int) {
        for (i in stars.indices) {
            if (i < selectedStars) {
                stars[i].setImageResource(R.drawable.ic_star_filled) // Colorea la estrella
            } else {
                stars[i].setImageResource(R.drawable.ic_star_outline) // Mantiene las estrellas vacías
            }
        }
    }
}
