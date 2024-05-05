package com.example.booklette

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.booklette.databinding.FragmentOrderDetailDeliveredBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date

/**
 * A simple [Fragment] subclass.
 * Use the [OrderDetailCaseDeliveredFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val ORDERID_PARAM = "param1"

class OrderDetailCaseDeliveredFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var orderID: String? = null
    private var _binding: FragmentOrderDetailDeliveredBinding? = null


    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            orderID = it.getString(ORDERID_PARAM)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOrderDetailDeliveredBinding.inflate(inflater, container, false)
        val view = binding.root

        db = Firebase.firestore
        val numberField: TextView = binding.orderDetailNumberField
        val dateField = binding.orderDetailDateField
        val trackingNumberField = binding.orderDetailTrackingNumberField
        val statusField = binding.orderDetailStatusField
        val shippingAddressField = binding.orderDetailShippingAddressField
        val paymentMethodField = binding.orderDetailPaymentMethodField
        val beforeDiscountField = binding.orderDetailBeforeDiscountField
        val discountField = binding.orderDetailDiscountField
        val totalField = binding.orderDetailTotalField

        val orderItemLayout = binding.orderDetailProductsFragmentFrameLayout

        var tempTotalOrgMoney: Long = 0
        var bookSoldQuantity = mutableMapOf<String,Int>()
        var shopIDlist = arrayListOf<String>()
        var itemsMap : Map<String, Map<String, Any>> = emptyMap()
        orderID?.let {
            db.collection("orders")
                .document(it)
                .get()
                .addOnSuccessListener { document ->
                    val orderData = document.data
                    val timeStamp = orderData?.get("creationDate") as Timestamp

                    val date: Date? = timeStamp?.toDate()
                    itemsMap = (orderData?.get("items") as? Map<String, Map<String, Any>>)!!

                    val totalMoney = (orderData?.get("totalSum") as Number).toLong()
                    val status = orderData?.get("status") as String

                    val paymentMethod = orderData?.get("paymentMethod") as? Map<String, Any>
                    val paymentMethodType = paymentMethod?.get("Type")
                    val shippingAddress = orderData?.get("shippingAddress") as String

                    val beforeDiscount = (orderData?.get("beforeDiscount") as Number).toLong()

                    // setup recycler view for books
                    val itemsFragment = OrderDetailItemListFragment.newInstance(
                        1,
                        itemsMap!!,
                        allowSelection = false,
                        allowMultipleSelection = false
                    )
                    childFragmentManager.beginTransaction()
                        .replace(orderItemLayout.id, itemsFragment)
                        .commit()
                    // assign to field in view
                    var orderName = ""
                    val fetchBookNamesTasks = itemsMap?.flatMap { (shopID, itemMap) ->
                        itemMap.map { (itemId, itemData) ->
                            Log.d("shopID", shopID)
                            Log.d("itemId", itemId)
                            Log.d("itemData", itemData.toString())
                            shopIDlist.add(shopID)
                            val bookQuantity = ((itemData as Map<*, *>)["quantity"] as? Int) ?: 0
                            bookSoldQuantity[itemId] = bookQuantity

                            db.collection("books")
                                .whereEqualTo("bookID", itemId)
                                .get()
                                .addOnSuccessListener { bookSnapshot ->
                                    for (book in bookSnapshot.documents) {
                                        val bookData = book.data
                                        val bookName = bookData?.get("name") as? String
                                        if (!bookName.isNullOrEmpty()) {
                                            orderName += "$bookName, "
                                        }
                                    }
                                }

                        }
                    }
                    Tasks.whenAllComplete(fetchBookNamesTasks!!)
                        .addOnSuccessListener {
                            if (orderName.length >= 2) {
                                orderName = orderName.dropLast(2)
                            }
                            if (orderName.length > 20) {
                                orderName = orderName.substring(0, 20) + "..."
                            }
                            numberField.text = orderName
                            val sdf = SimpleDateFormat("dd-MM-yyyy")
                            dateField.text = sdf.format(date)

                            trackingNumberField.text = orderID
                            statusField.text = changeStatusText(status)
                            shippingAddressField.text = shippingAddress
                            paymentMethodField.text = paymentMethodType.toString()
                            beforeDiscountField.text = formatMoney(beforeDiscount)
                            discountField.text = "-" + formatMoney(beforeDiscount - totalMoney)
                            totalField.text = formatMoney(totalMoney)
                        }

                }
        }


        // back
        val backButton = binding.backButton
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        // confirm order
        val confirmButton = binding.orderDetailConfirmButton
        confirmButton.setOnClickListener {
            val dialogClickListener =
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        // !!will change to motion toast later!!
                        DialogInterface.BUTTON_POSITIVE -> { // Chỗ này bị cấn ở UI vì cái status chỉ có hoàn thành, thiếu giao hàng, làm sao để xác nhận là hoàn thành?
                            val docRef =
                                orderID?.let { it1 -> db.collection("orders").document(it1) }
                            docRef?.update("status", "Thành công")?.addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    R.string.orderDetailConfirmSuccessfulArgument,
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d("itemsMap", itemsMap.toString())
                                val updateBookRemain = itemsMap?.flatMap { (shopID, itemMap) ->
                                    itemMap.map { (itemId, itemData) ->
                                        Log.d("shopID", shopID)
                                        Log.d("itemId", itemId)
                                        Log.d("itemData", itemData.toString())

                                        val bookQuantity =
                                            (((itemData as Map<*, *>)["quantity"] as? Number)?.toInt()) ?: 0
                                        val shopAccount = db.collection("accounts").whereEqualTo("UID", shopID).limit(1).get()
                                        shopAccount.addOnSuccessListener { accountSnapshots ->
                                            for ( accountSnapshot in accountSnapshots){
                                                val shopPersonalStoreRef = accountSnapshot["store"] as DocumentReference
                                                shopPersonalStoreRef.get()
                                                    .addOnSuccessListener { storeSnapshot ->
                                                        val shopItems =
                                                            storeSnapshot["items"] as Map<String, Map<String, Any>>
                                                        val soldBookID = shopItems[itemId]
                                                        var bookRemain =
                                                            (soldBookID?.get("remain") as Number).toInt()
                                                        var bookSold =
                                                            (soldBookID?.get("sold") as Number).toInt()
                                                        // Update bookRemain and bookSold based on bookQuantity
                                                        Log.d("BookRemainSold", bookRemain.toString() + " " + bookSold.toString())
                                                        Log.d("BookRemainSoldQuantity",
                                                            bookQuantity.toString()
                                                        )
                                                        bookRemain -= bookQuantity
                                                        bookSold += bookQuantity
                                                        Log.d("BookRemainSold", bookRemain.toString() + " " + bookSold.toString())
                                                        // Construct the update data
                                                        val updateData = hashMapOf<String, Any>(
                                                            "items.$itemId.remain" to bookRemain,
                                                            "items.$itemId.sold" to bookSold
                                                        )
                                                        // Update the document in Firestore
                                                        shopPersonalStoreRef.update(updateData)
                                                            .addOnSuccessListener {
                                                                Log.d("success", "Update book thanh cong")
                                                            }
                                                            .addOnFailureListener { e ->
                                                                Log.e("error", e.toString())
                                                            }
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.e("error", e.toString())
                                                    }
                                            }
                                        }

                                    }
                                }
                                Tasks.whenAllComplete(updateBookRemain)
                                    .addOnSuccessListener {
                                        requireActivity().onBackPressedDispatcher.onBackPressed()
                                    }
                            }?.addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    R.string.orderDetailConfirmFailedArgument,
                                    Toast.LENGTH_SHORT
                                ).show()

                            }

                        }
                        DialogInterface.BUTTON_NEGATIVE -> {

                        }
                    }
                }

            val builder: AlertDialog.Builder = AlertDialog.Builder(context)

            builder.setMessage(R.string.orderDetailConfirmlabel)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show()
        }
        // return order
        val returnButton = binding.orderDetailReturnButton
        returnButton.setOnClickListener {
            val returnFragment =
                orderID?.let { it1 -> OrderDetailCaseReturnFragment.newInstance(it1) }
            if (context is homeActivity) {
                if (returnFragment != null) {
                    (context as homeActivity).changeFragmentContainer(
                        returnFragment,
                        (context as homeActivity).smoothBottomBarStack[(context as homeActivity).smoothBottomBarStack.size - 1]
                    )
                }
            }
        }
        return view
    }
    fun changeStatusText(status: String): String {
        return when {
            status.contains("xử lý", true) -> getString(R.string.my_order_processing_button)
            status.contains("huỷ", true) -> getString(R.string.my_order_cancelled_button)
            status.contains("trả đang duyệt", true) -> getString(R.string.my_order_detail_item_return_in_process)
            status.contains("trả thành công", true) -> getString(R.string.my_order_detail_item_return_success)
            status.contains("trả bị từ chối", true) -> getString(R.string.my_order_detail_item_return_failed)
            status.contains("thành công", true) -> getString(R.string.my_order_completed_button)
            status.contains("đã giao", true) -> getString(R.string.my_order_delivered_button)
            else -> ""
        }
    }

    fun formatMoney(number: Long): String {
        val numberString = number.toString()
        val regex = "(\\d)(?=(\\d{3})+$)".toRegex()
        return numberString.replace(regex, "$1.") + " VND"
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param orderID Parameter 1.
         * @param orderFullName Parameter 2.
         * @return A new instance of fragment OrderDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(orderID: String) =
            OrderDetailCaseDeliveredFragment().apply {
                arguments = Bundle().apply {
                    putString(ORDERID_PARAM, orderID)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}