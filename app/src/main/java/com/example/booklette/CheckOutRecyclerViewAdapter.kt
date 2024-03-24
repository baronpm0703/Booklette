import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.CheckoutItem
import com.example.booklette.R

class CheckOutRecyclerViewAdapter(private val context: Context, private val data: List<CheckoutItem>) :
    RecyclerView.Adapter<CheckOutRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_check_out_list_view_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = data[position]

        holder.shopNameTextView.text = currentItem.shopName
        holder.bookTitleTextView.text = currentItem.bookTitle
        holder.bookOwnerTextView.text = currentItem.bookOwner
        holder.bookPriceTextView.text = currentItem.bookPrice
        holder.quantityTextView.text = currentItem.tvCartItemCount

    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shopNameTextView: TextView = itemView.findViewById(R.id.shopName)
        val bookTitleTextView: TextView = itemView.findViewById(R.id.bookTitle)
        val bookOwnerTextView: TextView = itemView.findViewById(R.id.bookOwner)
        val bookPriceTextView: TextView = itemView.findViewById(R.id.bookPrice)
        val quantityTextView: TextView = itemView.findViewById(R.id.tvCartItemCount)

        // Initialize other views here
    }
}