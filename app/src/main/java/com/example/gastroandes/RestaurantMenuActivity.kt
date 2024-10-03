package com.example.gastroandes

import RestaurantMenuAdapter
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView

import com.example.gastroandes.viewModel.RestaurantMenuViewModel

class RestaurantMenuActivity : AppCompatActivity() {


    private lateinit var viewModel: RestaurantMenuViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var menuAdapter: RestaurantMenuAdapter

    companion object {
        private const val EXTRA_RESTAURANT_NAME = "extra_restaurant_name"
        private const val EXTRA_RESTAURANT_IMAGE = "extra_restaurant_image"

        fun newIntent(context: Context, restaurantName: String, restaurantImageResId: Int): Intent {
            return Intent(context, RestaurantMenuActivity::class.java).apply {
                putExtra(EXTRA_RESTAURANT_NAME, restaurantName)
                putExtra(EXTRA_RESTAURANT_IMAGE, restaurantImageResId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.restaurant_menu_detail)

        val restaurantName = intent.getStringExtra(EXTRA_RESTAURANT_NAME) ?: "Restaurant Name"
        val restaurantImageResId = intent.getIntExtra(EXTRA_RESTAURANT_IMAGE, R.drawable.sushi_sample)

        findViewById<TextView>(R.id.restaurantNameText).text = restaurantName
        findViewById<ImageView>(R.id.restaurantHeaderImage).setImageResource(restaurantImageResId)

        viewModel = ViewModelProvider(this).get(RestaurantMenuViewModel::class.java)

        recyclerView = findViewById(R.id.menuRecyclerView)
        menuAdapter = RestaurantMenuAdapter()
        recyclerView.adapter = menuAdapter

        viewModel.menuItems.observe(this) { menuItems ->
            menuAdapter.submitList(menuItems)
        }

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            finish()
        }
    }
}