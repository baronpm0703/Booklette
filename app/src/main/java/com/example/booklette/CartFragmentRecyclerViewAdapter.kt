import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.R

class CartFragmentRecyclerViewAdapter(
    private val context: Context,
    private val bookInCartList: ArrayList<String>
) : RecyclerView.Adapter<CartFragmentRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookCover: ImageView = itemView.findViewById(R.id.bookCover)
        val bookTitle: TextView = itemView.findViewById(R.id.bookTitle)
        val bookOwner: TextView = itemView.findViewById(R.id.bookOwner)
        val bookPrice: TextView = itemView.findViewById(R.id.bookPrice)
        val btnCartItemMinus: AppCompatButton = itemView.findViewById(R.id.btnCartItemMinus)
        val tvCartItemCount: TextView = itemView.findViewById(R.id.tvCartItemCount)
        val btnCartItemAdd: AppCompatButton  = itemView.findViewById(R.id.btnCartItemAdd)
    }

    interface OnQuantityChangeListener {
        fun onQuantityDecreased(position: Int)
        fun onQuantityIncreased(position: Int)
    }

    var quantityChangeListener: OnQuantityChangeListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_cart_recycle_view_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookInfo = bookInCartList[position]

        if (bookInfo == "2") {
            holder.bookCover.setImageResource(R.drawable.image_book_3)
            holder.bookTitle.text = "The Catcher in the Rye"
            holder.bookOwner.text = "Phan Thai Khang"
            holder.bookPrice.text = "150.000 VND"
        } else if (bookInfo == "1") {
            holder.bookCover.setImageResource(R.drawable.image_book_2)
            holder.bookTitle.text = "The Secret Society of Aunts & Uncles"
            holder.bookOwner.text = "Jake Gyllenhaal, Greta Caruso"
            holder.bookPrice.text = "250.000 VND"
        }
        holder.btnCartItemMinus.setOnClickListener {
            quantityChangeListener?.onQuantityDecreased(position)
            // Update tvCartItemCount
            var newCount = holder.tvCartItemCount.text.toString().toInt() - 1
            if (newCount < 0) {
                newCount = 0
            }
            holder.tvCartItemCount.text= newCount.toString()

        }

        // Increase item quantity when add button is clicked
        holder.btnCartItemAdd.setOnClickListener {
            quantityChangeListener?.onQuantityIncreased(position)
            // Update tvCartItemCount
            var newCount = holder.tvCartItemCount.text.toString().toInt() + 1
            holder.tvCartItemCount.text = newCount.toString()
        }
    }

    override fun getItemCount(): Int {
        return bookInCartList.size
    }

    fun removeItem(position: Int) {

    }
}