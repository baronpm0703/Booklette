import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.R
import com.example.booklette.bookDetailShopVoucherRVAdapter
import com.example.booklette.model.CartObject
import com.squareup.picasso.Picasso
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booklette.model.VoucherObject
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore


class CheckOutRecyclerViewAdapter(
    private val context: Context,
    private val selectedItems: ArrayList<CartObject>
) :
    RecyclerView.Adapter<CheckOutRecyclerViewAdapter.ViewHolder>() {

    interface VoucherItemClickListener {
        fun onVoucherItemClick(percentage: Float)
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var shopVoucherAdapter: bookDetailShopVoucherRVAdapter
    private var voucherPercentage: Float = 0f // Store the voucher percentage


    fun calculateTotalAmount(): Float {
        var total = 0f
        for (item in selectedItems) {
            total += item.price * item.bookQuantity
        }
        return total
    }

    private fun calculateVoucherShop(): Float {
        return calculateTotalAmount() * (voucherPercentage / 100)
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
        holder.quantityTextView.text = currentItem.bookQuantity.toString()

        val bookpricewithquantity = currentItem.price * currentItem.bookQuantity
        val formattedPriceWithQuantity = String.format("%,.0f", bookpricewithquantity)
        holder.BookPriceWithQuantity.text = "$formattedPriceWithQuantity VND"

        val shopVoucherList: ArrayList<VoucherObject> = ArrayList()
        val shopVoucherAdapter = bookDetailShopVoucherRVAdapter(context, shopVoucherList)
        holder.rvShopVoucherAdapter.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        holder.rvShopVoucherAdapter.adapter = shopVoucherAdapter


        val discountItem = calculateVoucherShop()
        val formattedDiscountItem = String.format("%,.0f", discountItem)

        holder.voucherShop.text = "$formattedDiscountItem VND"


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        db.collection("accounts").whereEqualTo("UID", currentItem.storeID).get()
            .addOnSuccessListener { accountDocument ->
                for (eachAccount in accountDocument) {
                    eachAccount.getDocumentReference("store")!!
                        .get()
                        .addOnSuccessListener { personalStoreDocument ->
                            val voucherList =
                                personalStoreDocument["shopVouchers"] as? ArrayList<String>
                            voucherList?.let { itemDisCountData ->
                                for (itemDiscount in itemDisCountData) {
                                    db.collection("discounts")
                                        .whereEqualTo("discountID", itemDiscount)
                                        .get()
                                        .addOnSuccessListener { discountDocument ->
                                            for (eachDiscountDocument in discountDocument) {
                                                shopVoucherList.add(
                                                    VoucherObject(
                                                        eachDiscountDocument.data.get("discountFilter")
                                                            .toString(),
                                                        eachDiscountDocument.data.get("discountID")
                                                            .toString(),
                                                        eachDiscountDocument.data.get("discountName")
                                                            .toString(),
                                                        eachDiscountDocument.data.get("discountType")
                                                            .toString(),
                                                        eachDiscountDocument.data.get("endDate") as Timestamp,
                                                        eachDiscountDocument.data.get("percent")
                                                            .toString().toFloat(),
                                                        eachDiscountDocument.data.get("startDate") as Timestamp
                                                    )
                                                )
                                                shopVoucherAdapter.notifyDataSetChanged()

                                            }
                                            currentItem.discountList = shopVoucherList
                                        }
                                }
                            }
                        }
                }
            }

        shopVoucherAdapter.itemClickListener = object : bookDetailShopVoucherRVAdapter.VoucherItemClickListener {
            override fun onVoucherItemClick(percentage: Float) {
                voucherPercentage = percentage
                notifyDataSetChanged() // Update the voucher shop value
            }
        }

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
        val rvShopVoucherAdapter: RecyclerView = itemView.findViewById(R.id.rvBookDetailShopVoucher)
        val voucherShop: TextView = itemView.findViewById(R.id.voucherShop)

    }
}

