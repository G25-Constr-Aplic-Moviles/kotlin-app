package com.example.gastroandes
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.gastroandes.R
import com.example.gastroandes.model.Review

class ReviewAdapter(private var reviews: List<Review>) : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTextView: TextView = view.findViewById(R.id.reviewDate)
        val ratingBar: RatingBar = view.findViewById(R.id.reviewRating)
        val contentTextView: TextView = view.findViewById(R.id.reviewContent)
        val usernameTextView: TextView = view.findViewById(R.id.reviewUsername)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = reviews[position]
        holder.dateTextView.text = review.getFormattedDateReview()
        holder.ratingBar.rating = review.rating
        holder.contentTextView.text = review.content
        holder.usernameTextView.text = "Usuario ${review.user_id}" // Puedes ajustar esto según los datos del usuario
    }

    override fun getItemCount() = reviews.size

    // Método para actualizar los datos en el adaptador
    fun updateData(newReviews: List<Review>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}
