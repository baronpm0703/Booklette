import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.R
import com.example.booklette.bookDetailShopVoucherRVAdapter
import com.example.booklette.model.CartObject
import com.squareup.picasso.Picasso
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booklette.model.VoucherObject
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
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
    private var totalVoucherSum: Float = 0f

    // Define the interface for voucher sum calculation
    interface VoucherSumListener {
        fun onVoucherSumCalculated(sum: Float)
    }

    private var voucherSumListener: VoucherSumListener? = null

    fun setVoucherSumListener(listener: VoucherSumListener) {
        voucherSumListener = listener
    }
    // Method to calculate the total voucher shop discount
    fun calculateTotalVoucherShopDiscount(): Float {
        var totalVoucherShopDiscount = 0f
        for (item in selectedItems) {
            totalVoucherShopDiscount += item.voucherShopDiscount
        }
        return totalVoucherShopDiscount
    }


    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedChip: String = ""


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
        holder.quantityTextView.text = currentItem.bookQuantity.toString()

        val bookpricewithquantity = currentItem.price * currentItem.bookQuantity
        val formattedPriceWithQuantity = String.format("%,.0f", bookpricewithquantity)
        holder.BookPriceWithQuantity.text = "$formattedPriceWithQuantity VND"


        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        holder.voucherShop.text = "0 VND"
        holder.priceAfterUseVoucher.text = "$formattedPriceWithQuantity VND"

        val chipSelectionMap: MutableMap<Chip, Boolean> = mutableMapOf()

        db.collection("accounts").whereEqualTo("UID", currentItem.storeID).get()
            .addOnSuccessListener { accountDocument ->
                for (eachAccount in accountDocument) {
                    eachAccount.getDocumentReference("store")!!
                        .get()
                        .addOnSuccessListener { personalStoreDocument ->
                            val voucherList =
                                personalStoreDocument["shopVouchers"] as? ArrayList<String>
                            voucherList?.let { itemDiscountData ->
                                for (voucherItem in itemDiscountData) {
                                    db.collection("discounts")
                                        .whereEqualTo("discountID", voucherItem)
                                        .get()
                                        .addOnSuccessListener { discountDocument ->
                                            for (eachDiscount in discountDocument) {
                                                val percentVoucher = eachDiscount.get("percent") as Number
                                                val percentFloat = percentVoucher.toFloat()

                                                val chip = LayoutInflater.from(context).inflate(
                                                    R.layout.home_fragment_chip_top_book,
                                                    holder.homeFragmentCGTopBook,
                                                    false
                                                ) as Chip
                                                chip.text =
                                                    "$percentVoucher% Off"
                                                holder.homeFragmentCGTopBook.addView(chip)
                                            }
                                        }

                                }
                            }
                        }
                }
            }

//        handle chip selected
        for (i in 0 until holder.homeFragmentCGTopBook.childCount) {
            val view = holder.homeFragmentCGTopBook.getChildAt(i)
            if (view is Chip) {
                val chip = view as Chip
                if (chip.text == selectedChip) {
                    chip.isChecked = true
                }
            }
        }

        var discountVoucher = 0f

        holder.homeFragmentCGTopBook.setOnCheckedStateChangeListener { group, checkedIds ->
            Log.d("CheckedIds", "CheckedIds size: ${checkedIds.size}")
            if (checkedIds.size > 0) {
                val chip: Chip? = group.findViewById(checkedIds[0])
                if (chip != null && chip.isChecked) {
                    Log.d("chip", chip.text.toString())
                    selectedChip = chip.text.toString()
                    val percentValue = chip.text.split("%")[0].trim().toIntOrNull()
                    if (percentValue != null) {
                        discountVoucher = bookpricewithquantity * (percentValue.toFloat() / 100f)
                        val afterFormatDiscountVoucher = String.format("%,.0f", discountVoucher)
                        holder.voucherShop.text = "$afterFormatDiscountVoucher VND"
                    } else {
                        Log.e("Error", "Failed to convert percent value to Int.")
                    }
                } else {
                    discountVoucher = 0f
                    holder.voucherShop.text = "0 VND"
                }
                currentItem.voucherShopDiscount = discountVoucher
                val afterUseVoucher = bookpricewithquantity - discountVoucher
                val formattedAfterUserVoucher = String.format("%,.0f", afterUseVoucher)
                holder.priceAfterUseVoucher.text = "$formattedAfterUserVoucher VND"

                calculateTotalVoucherSum()
            } else {
                Log.d("unchip", "unchip")
                discountVoucher = 0f
                holder.voucherShop.text = "0 VND"
                currentItem.voucherShopDiscount = discountVoucher
                val afterUseVoucher = bookpricewithquantity - discountVoucher
                val formattedAfterUserVoucher = String.format("%,.0f", afterUseVoucher)
                holder.priceAfterUseVoucher.text = "$formattedAfterUserVoucher VND"
                calculateTotalVoucherSum()
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
        val homeFragmentCGTopBook: ChipGroup = itemView.findViewById(R.id.homeFragmentCGTopBook)
        val voucherShop: TextView = itemView.findViewById(R.id.voucherShop)
        val priceAfterUseVoucher: TextView = itemView.findViewById(R.id.priceAfterUseVoucher)
    }

    private fun calculateTotalVoucherSum() {
        totalVoucherSum = 0f
        for (item in selectedItems) {
            totalVoucherSum += item.voucherShopDiscount
        }
        // Pass the total voucher sum back to the fragment using the interface
        voucherSumListener?.onVoucherSumCalculated(totalVoucherSum)
    }
}

