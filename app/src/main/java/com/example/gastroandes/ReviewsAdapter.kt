import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.example.gastroandes.model.Review
import com.example.gastroandes.R

class ReviewsAdapter : ListAdapter<Review, ReviewsAdapter.ViewHolder>(ReviewDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.reviewTitle)
        private val contentTextView: TextView = itemView.findViewById(R.id.reviewContent)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.reviewRating)
        private val usernameTextView: TextView = itemView.findViewById(R.id.reviewUsername)
        private val dateTextView: TextView = itemView.findViewById(R.id.reviewDate)

        fun bind(review: Review) {
            titleTextView.text = review.title
            contentTextView.text = review.content
            ratingBar.rating = review.rating
            usernameTextView.text = review.username
            dateTextView.text = review.date
        }
    }
}

class ReviewDiffCallback : DiffUtil.ItemCallback<Review>() {
    override fun areItemsTheSame(oldItem: Review, newItem: Review): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Review, newItem: Review): Boolean {
        return oldItem == newItem
    }
}