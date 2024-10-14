import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.gastroandes.R
import com.example.gastroandes.model.UserHistoryEntry

class HistoryAdapter(var entries: List<UserHistoryEntry>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTextView: TextView = view.findViewById(R.id.dateLabelText)
        val restaurantIdTextView: TextView = view.findViewById(R.id.historyEntryText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history_entry, parent, false)
        return ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]
        holder.dateTextView.text = entry.getFormattedDate()
        holder.restaurantIdTextView.text = entry.restaurantName ?: "Desconocido"
    }

    fun updateData(newEntries: List<UserHistoryEntry>) {
        entries = newEntries
        notifyDataSetChanged()
    }

    override fun getItemCount() = entries.size
}
