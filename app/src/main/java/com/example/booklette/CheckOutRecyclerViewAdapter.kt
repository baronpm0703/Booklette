import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.CheckoutItem
import com.example.booklette.R
import com.example.booklette.model.CartObject
import com.squareup.picasso.Picasso

class CheckOutRecyclerViewAdapter(
    private val context: Context,
    private val selectedItems: ArrayList<CartObject>,

) :
    RecyclerView.Adapter<CheckOutRecyclerViewAdapter.ViewHolder>() {

    fun calculateTotalAmount(): Float {
        var total = 0f
        for (item in selectedItems) {
            total += item.price * item.bookQuantity
        }
        return total
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_check_out_list_view_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = selectedItems[position]

        holder.shopNameTextView.text = currentItem.storeName
        holder.bookTitleTextView.text = currentItem.bookName
        holder.bookOwnerTextView.text = currentItem.author
        val formattedPrice = String.format("%,.0f", currentItem.price) 
        holder.bookPriceTextView.text = "$formattedPrice VND"
        Picasso.get()
            .load(currentItem.bookCover)
            .into(holder.bookCover)
        holder.quantityTextView.text= currentItem.bookQuantity.toString()

        val bookpricewithquantity = currentItem.price * currentItem.bookQuantity
        val formattedPriceWithQuantity = String.format("%,.0f", bookpricewithquantity)
        holder.BookPriceWithQuantity.text = "$formattedPriceWithQuantity VND"

    }



    override fun getItemCount(): Int {
        return selectedItems.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shopNameTextView: TextView = itemView.findViewById(R.id.shopName)
        val bookTitleTextView: TextView = itemView.findViewById(R.id.bookTitle)
        val bookOwnerTextView: TextView = itemView.findViewById(R.id.bookOwner)
        val bookPriceTextView: TextView = itemView.findViewById(R.id.bookPrice)
        val quantityTextView: TextView = itemView.findViewById(R.id.quantity)
        val bookCover: ImageView = itemView.findViewById(R.id.bookCover)
        val BookPriceWithQuantity: TextView = itemView.findViewById(R.id.BookPriceWithQuantity)

    }
}