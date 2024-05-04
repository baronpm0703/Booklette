package com.example.booklette

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.booklette.databinding.FragmentShopOrderDetailDeliveredBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date
private const val ORDERID_PARAM = "param1"
class OrderDetailCaseShopDeliveredFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var orderID: String? = null
    private var _binding: FragmentShopOrderDetailDeliveredBinding? = null


    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    private var storeUID : String = ""
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
        _binding = FragmentShopOrderDetailDeliveredBinding.inflate(inflater, container, false)
        val view = binding.root

        db = Firebase.firestore
        val numberField: TextView = binding.orderDetailNumberField
        val dateField = binding.orderDetailDateField
        val trackingNumberField = binding.orderDetailTrackingNumberField
        val buyerNameField = binding.orderDetailBuyerNameField
        val statusField = binding.orderDetailStatusField
        val shippingAddressField = binding.orderDetailShippingAddressField
        val paymentMethodField = binding.orderDetailPaymentMethodField
        val beforeDiscountField = binding.orderDetailBeforeDiscountField
        val discountField = binding.orderDetailDiscountField
        val totalField = binding.orderDetailTotalField

        val orderItemLayout = binding.orderDetailProductsFragmentFrameLayout

        var tempTotalOrgMoney: Long = 0
        orderID?.let {
            db.collection("orders")
                .document(it)
                .get()
                .addOnSuccessListener { document ->
                    val orderData = document.data
                    val timeStamp = orderData?.get("creationDate") as Timestamp

                    val date: Date? = timeStamp?.toDate()
                    val itemsMap = orderData?.get("items") as? Map<String, Map<String, Any>>

                    val totalMoney = (orderData?.get("totalSum") as Number).toLong()
                    val status = orderData?.get("status") as String

                    val paymentMethod = orderData?.get("paymentMethod") as? Map<String, Any>
                    val paymentMethodType = paymentMethod?.get("Type")
                    val customerID = orderData?.get("customerID")
                    storeUID = customerID.toString()
                    val customerRef = db.collection("accounts").whereEqualTo("UID",customerID)
                    var customerName = "Minh Bảo"
                    customerRef.get()
                        .addOnSuccessListener { QuerySnapshot ->
                            for ( userDocument in QuerySnapshot){
                                val userData = userDocument.data
                                customerName = userData?.get("fullname").toString()
                            }
                        }
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
                            buyerNameField.text = customerName
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

        // return order
        val contactButton = binding.orderDetailContactBuyerButton
        contactButton.setOnClickListener {
            val intent = Intent(context, ChannelChatActivity::class.java)
            intent.putExtra("storeUID", storeUID)

            startActivity(intent)
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
            OrderDetailCaseShopDeliveredFragment().apply {
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