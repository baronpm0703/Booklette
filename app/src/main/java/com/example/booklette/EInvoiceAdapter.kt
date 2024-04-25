import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.R

class EInvoiceAdapter(private val itemsMap: Map<String, Any>?) :
    RecyclerView.Adapter<EInvoiceAdapter.ViewHolder>() {
    // ViewHolder để giữ các thành phần view trong mỗi mục của RecyclerView
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.orderDetailItemName)
        val itemAmount: TextView = view.findViewById(R.id.orderDetailItemAmount)
        val itemPrice: TextView = view.findViewById(R.id.orderDetailItemPrice)
        val tax: TextView = view.findViewById(R.id.tax)
        val totalAmount: TextView = view.findViewById(R.id.totalAmount)
    }
    // onCreateViewHolder - Tạo ViewHolder mới khi RecyclerView cần một item view mới
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.einvoice_book_item, parent, false)
        return ViewHolder(view)
    }

    // onBindViewHolder - Gắn dữ liệu vào ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        itemsMap?.let { map ->
            val itemID = map.keys.elementAt(position)
            val itemData = map[itemID] as? Map<String, Any>

            holder.itemName.text = itemData?.get("name").toString()
            holder.itemAmount.text = itemData?.get("quantity").toString()
            holder.itemPrice.text = itemData?.get("price").toString()
            // Tùy chỉnh cho các TextView khác nếu cần
        }
    }
    override fun getItemCount(): Int {
        return itemsMap?.size ?: 0
    }
}
