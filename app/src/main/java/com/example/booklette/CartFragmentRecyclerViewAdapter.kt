import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.R

class CartFragmentRecyclerViewAdapter(
    private val context: Context,
    private val bookInCartList: ArrayList<String>,

) : RecyclerView.Adapter<CartFragmentRecyclerViewAdapter.ViewHolder>() {
    private val itemQuantities = ArrayList<Int>()
    private val itemSelections = ArrayList<Boolean>()


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookCover: ImageView = itemView.findViewById(R.id.bookCover)
        val bookTitle: TextView = itemView.findViewById(R.id.bookTitle)
        val bookOwner: TextView = itemView.findViewById(R.id.bookOwner)
        val bookPrice: TextView = itemView.findViewById(R.id.bookPrice)
        val btnCartItemMinus: AppCompatButton = itemView.findViewById(R.id.btnCartItemMinus)
        val tvCartItemCount: TextView = itemView.findViewById(R.id.tvCartItemCount)
        val btnCartItemAdd: AppCompatButton  = itemView.findViewById(R.id.btnCartItemAdd)
        val btnSelectItem: RadioButton = itemView.findViewById(R.id.btnSelectItem)
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

    init {
        // Initialize itemQuantities with default quantities
        for (i in bookInCartList.indices) {
            itemQuantities.add(1) // Initialize with default quantity 1
            itemSelections.add(false) // Initialize with not selected

        }
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
        else if (bookInfo == "3") {
            holder.bookCover.setImageResource(R.drawable.image_book_2)
            holder.bookTitle.text = "The Secret Society of Aunts & Uncles"
            holder.bookOwner.text = "Jake Gyllenhaal, Greta Caruso"
            holder.bookPrice.text = "100.000 VND"
        }
        else if (bookInfo == "4") {
            holder.bookCover.setImageResource(R.drawable.image_book_2)
            holder.bookTitle.text = "TestBook"
            holder.bookOwner.text = "Khong coa ten"
            holder.bookPrice.text = "120.000 VND"
        }
        holder.tvCartItemCount.text = itemQuantities[position].toString()
        holder.btnSelectItem.isChecked = itemSelections[position]
        holder.btnSelectItem.setOnClickListener {
            toggleSelection(position)
            notifyDataSetChanged() // Notify data change to update views
        }

        holder.btnCartItemMinus.setOnClickListener {
            // Decrease quantity and notify data change
            decreaseQuantity(position)
            notifyDataSetChanged() // Notify data change to update views
        }

        holder.btnCartItemAdd.setOnClickListener {
            // Increase quantity and notify data change
            increaseQuantity(position)
            notifyDataSetChanged() // Notify data change to update views
        }
    }
    private fun toggleSelection(position: Int) {
        itemSelections[position] = !itemSelections[position]
    }

    private fun increaseQuantity(position: Int) {
        itemQuantities[position]++
    }

    // Decrease quantity for an item, ensuring it doesn't go below 0
    private fun decreaseQuantity(position: Int) {
        if (itemQuantities[position] > 0) {
            itemQuantities[position]--
        }
    }
    override fun getItemCount(): Int {
        return bookInCartList.size
    }

    fun removeItem(position: Int) {
        // Xóa mục khỏi danh sách
        bookInCartList.removeAt(position)
        // Thông báo cho RecyclerView biết rằng một mục đã bị xóa ở vị trí được chỉ định
        notifyItemRemoved(position)
    }


    fun getItemInfo(position: Int): String {
        // Trả về thông tin của item tại vị trí được chỉ định
        return bookInCartList[position]
    }
}