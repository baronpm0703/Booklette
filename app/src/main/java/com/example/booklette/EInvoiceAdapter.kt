import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.R
import DetailInvoiceItem

class EInvoiceAdapter(
    private val context: Context,
    private var itemList: ArrayList<DetailInvoiceItem> = ArrayList()
) : RecyclerView.Adapter<EInvoiceAdapter.ViewHolder>() {
    // ViewHolder để giữ các thành phần view trong mỗi mục của RecyclerView
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView = view.findViewById(R.id.orderItemName)
        val itemQuantity: TextView = view.findViewById(R.id.orderItemQuantity)
        val itemPrice: TextView = view.findViewById(R.id.orderItemPrice)
        val totalAmount: TextView = view.findViewById(R.id.orderItemAmount)
    }
    // onCreateViewHolder - Tạo ViewHolder mới khi RecyclerView cần một item view mới
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.einvoice_book_item, parent, false)
        return ViewHolder(view)
    }

    // onBindViewHolder - Gắn dữ liệu vào ViewHolder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.itemName.text = item.name
        holder.itemQuantity.text = item.quantity.toString()
        
        val priceFomart = String.format("%,.0f", item.price)
        holder.itemPrice.text = "$priceFomart VND"

        val totalAmountFormat = String.format("%,.0f", item.totalSum)
        holder.totalAmount.text = "$totalAmountFormat VND"
    }
    override fun getItemCount(): Int = itemList.size
    fun getItemInfo(position: Int): DetailInvoiceItem {
        // Trả về thông tin của item tại vị trí được chỉ định
        return itemList[position]
    }
}
