import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.model.CartObject
import com.example.booklette.R
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class CartFragmentRecyclerViewAdapter(
    private val context: Context,
    private val cartList: ArrayList<CartObject>,
    ) : RecyclerView.Adapter<CartFragmentRecyclerViewAdapter.ViewHolder>() {

    private val itemQuantities = ArrayList<Int>()
    private val itemSelections = HashMap<Int, Boolean>()

    interface OnSelectItemClickListener {
        fun onSelectItemClick(position: Int)
    }

    var onSelectItemClickListener: OnSelectItemClickListener? = null


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookCover: ImageView = itemView.findViewById(R.id.bookCover)
        val bookTitle: TextView = itemView.findViewById(R.id.bookTitle)
        val bookOwner: TextView = itemView.findViewById(R.id.bookOwner)
        val bookPrice: TextView = itemView.findViewById(R.id.bookPrice)
        val storeName: TextView = itemView.findViewById(R.id.shopName)
        val btnCartItemMinus: AppCompatButton = itemView.findViewById(R.id.btnCartItemMinus)
        val quantity: TextView = itemView.findViewById(R.id.quantity)
        val btnCartItemAdd: AppCompatButton  = itemView.findViewById(R.id.btnCartItemAdd)
        val btnSelectItem: RadioButton = itemView.findViewById(R.id.btnSelectItem)

        init {
            btnSelectItem.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    toggleSelection(position)
                    notifyItemChanged(position)
                }
            }

            btnCartItemAdd.setOnClickListener {
                increaseQuantity(adapterPosition)
            }

            btnCartItemMinus.setOnClickListener {
                decreaseQuantity(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_cart_recycle_view_item, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cartInfo = cartList[position]
        holder.storeName.text=cartInfo.storeName
        holder.bookTitle.text=cartInfo.bookName
        holder.bookOwner.text=cartInfo.author
        holder.quantity.text= cartInfo.bookQuantity.toString()
        val formattedPrice = String.format("%,.0f", cartInfo.price) // Format as integer with thousand separator
        holder.bookPrice.text = "$formattedPrice VND"
        Picasso.get()
            .load(cartInfo.bookCover)
            .into(holder.bookCover)
//        holder.tvCartItemCount.text = itemQuantities[position].toString()
        holder.btnSelectItem.isChecked = itemSelections[position] ?: false

//
//        holder.btnCartItemMinus.setOnClickListener {
//            // Decrease quantity and notify data change
//            decreaseQuantity(position)
//            notifyDataSetChanged() // Notify data change to update views
//        }
//
//        holder.btnCartItemAdd.setOnClickListener {
//            // Increase quantity and notify data change
//            increaseQuantity(position)
//            notifyDataSetChanged() // Notify data change to update views
//        }
    }


    private fun toggleSelection(position: Int) {
        itemSelections[position] = !(itemSelections[position] ?: false)
    }

    fun getSelectedItems(): List<CartObject> {
        val selectedItems = mutableListOf<CartObject>()
        itemSelections.forEach { (position, isSelected) ->
            if (isSelected) {
                selectedItems.add(cartList[position])
            }
        }
        return selectedItems
    }

    private fun increaseQuantity(position: Int) {
        val cartObject = cartList[position]
        cartObject.bookQuantity = cartObject.bookQuantity.toInt() + 1
        notifyItemChanged(position)
    }

    private fun decreaseQuantity(position: Int) {
        val cartObject = cartList[position]
        if (cartObject.bookQuantity.toInt() > 0) {
            cartObject.bookQuantity = cartObject.bookQuantity.toInt() - 1
            notifyItemChanged(position)
        }
    }


    override fun getItemCount(): Int {
        return cartList.size
    }

    fun removeItem(position: Int) {
        // Xóa mục khỏi danh sách
        cartList.removeAt(position)
        // Thông báo cho RecyclerView biết rằng một mục đã bị xóa ở vị trí được chỉ định
        notifyItemRemoved(position)
    }


    fun getItemInfo(position: Int): CartObject {
        // Trả về thông tin của item tại vị trí được chỉ định
        return cartList[position]
    }
}