import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.R
import com.example.booklette.model.CartObject
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class CartFragmentRecyclerViewAdapter(
    private val context: Context,
    private val cartList: ArrayList<CartObject>,
) : RecyclerView.Adapter<CartFragmentRecyclerViewAdapter.ViewHolder>() {

    private val itemQuantities = ArrayList<Int>()
    private val itemSelections = HashMap<Int, Boolean>()
    private val selectedItems = ArrayList<CartObject>() // ArrayList to store selected items

    interface OnSelectItemClickListener {
        fun onSelectItemClick(selectedItems: ArrayList<CartObject>)
    }
    interface TotalAmountListener {
        fun onTotalAmountCalculated(totalAmount: Float)
    }

    private fun updateTotalAmount() {
        onSelectItemClickListener?.onSelectItemClick(selectedItems)
    }

    fun calculateTotalAmount(): Float {
        var total = 0f
        for (item in selectedItems) {
            total += item.price * item.bookQuantity
        }
        return total
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

                    // Notify the fragment when an item is selected
                    onSelectItemClickListener?.onSelectItemClick(getSelectedItems())

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
        holder.btnSelectItem.isChecked = itemSelections[position] ?: false
    }

    private fun toggleSelection(position: Int) {
        itemSelections[position] = !(itemSelections[position] ?: false)
        // Update selected items list
        if (itemSelections[position] == true) {
            selectedItems.add(cartList[position])
        } else {
            selectedItems.remove(cartList[position])
        }
    }

    fun getSelectedItems(): ArrayList<CartObject> {
        return selectedItems
    }

    private fun increaseQuantity(position: Int) {
        val cartObject = cartList[position]
        cartObject.bookQuantity = cartObject.bookQuantity.toInt() + 1
        notifyItemChanged(position)
        updateTotalAmount()

    }

    private fun decreaseQuantity(position: Int) {
        val cartObject = cartList[position]
        if (cartObject.bookQuantity.toInt() > 0) {
            cartObject.bookQuantity = cartObject.bookQuantity.toInt() - 1
            notifyItemChanged(position)
            updateTotalAmount()
        }
    }


    override fun getItemCount(): Int {
        return cartList.size
    }

    fun removeItem(position: Int) {
        cartList.removeAt(position)
        notifyItemRemoved(position)
    }


    fun getItemInfo(position: Int): CartObject {
        return cartList[position]
    }
}