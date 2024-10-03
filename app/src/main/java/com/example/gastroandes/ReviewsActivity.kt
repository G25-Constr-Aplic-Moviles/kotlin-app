package com.example.gastroandes

import ReviewsAdapter
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.gastroandes.viewModel.ReviewViewModel

class ReviewsActivity : AppCompatActivity() {

    private lateinit var viewModel: ReviewViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var backButton: ImageButton
    private lateinit var addReviewButton: ImageButton
    private val adapter = ReviewsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.restaurant_review)

        setupViews()
        setupViewModel()
        observeReviews()
        setupButtons()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.reviewsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        backButton = findViewById(R.id.backButton)
        addReviewButton = findViewById(R.id.addReviewButton)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this).get(ReviewViewModel::class.java)
    }

    private fun observeReviews() {
        viewModel.reviews.observe(this) { reviews ->
            adapter.submitList(reviews)
        }
    }

    private fun setupButtons() {
        backButton.setOnClickListener {
            finish()
        }

        addReviewButton.setOnClickListener {
            // TODO: Implement add review functionality
        }
    }
}