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

import com.example.booklette.databinding.FragmentShopOrderDetailToShipBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date

/**
 * A simple [Fragment] subclass.
 * Use the [OrderDetailCaseShopToShipFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val ORDERID_PARAM = "param1"
class OrderDetailCaseShopToShipFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var orderID: String? = null
    private var _binding: FragmentShopOrderDetailToShipBinding? = null
    private var itemsMap: Map<String, Map<String, Any>>? = null


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
        _binding = FragmentShopOrderDetailToShipBinding.inflate(inflater, container, false)
        val view = binding.root

        db = Firebase.firestore
        val numberField: TextView = binding.orderDetailNumberField
        val dateField = binding.orderDetailDateField
        val trackingNumberField = binding.orderDetailTrackingNumberField
        val statusField = binding.orderDetailStatusField
        val buyerNameField = binding.orderDetailBuyerNameField
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
                    val customerID = orderData?.get("customerID")
                    val customerRef = db.collection("accounts").whereEqualTo("UID",customerID)
                    var customerName = "Minh Bảo"
                    customerRef.get()
                        .addOnSuccessListener { QuerySnapshot ->
                            for ( userDocument in QuerySnapshot){
                                val userData = userDocument.data
                                customerName = userData?.get("fullname").toString()
                            }
                        }
                    val date: Date? = timeStamp?.toDate()
                    itemsMap = orderData?.get("items") as? Map<String, Map<String, Any>>
                    var totalQuantity: Long = 0
//                        itemsMap?.forEach { (itemID, itemData) ->
//
//                            val itemMap = itemData as? Map<String, Any>
//
//                            //Log.d("number",itemMap.toString())
//                            tempTotalOrgMoney += (itemMap?.get("totalSum") as Number).toFloat()
//                            totalQuantity += itemMap?.get("quantity") as Long
//                        }
                    val totalMoney = (orderData?.get("totalSum") as Number).toLong()
                    val status = orderData?.get("status") as String

                    val paymentMethod = orderData?.get("paymentMethod") as? Map<String, Any>
                    val paymentMethodType = paymentMethod?.get("Type")
                    val shippingAddress = orderData?.get("shippingAddress") as String
                    val beforeDiscount = (orderData?.get("beforeDiscount") as Number).toLong()
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

                            buyerNameField.text = customerName
                            trackingNumberField.text = orderID
                            statusField.text = changeStatusText(status)
                            shippingAddressField.text = shippingAddress
                            paymentMethodField.text = paymentMethodType.toString()
                            beforeDiscountField.text = formatMoney(beforeDiscount)
                            discountField.text = "-" + formatMoney(beforeDiscount - totalMoney)
                            val formattedMoney = formatMoney(totalMoney)
                            totalField.text = formattedMoney
                        }

                }
        }


        // back
        val backButton = binding.backButton
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }


        // cancel order
        val cancelButton = binding.orderDetailCancelOrderButton
        cancelButton.setOnClickListener {
            val dialogClickListener =
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            val docRef =
                                orderID?.let { it1 -> db.collection("orders").document(it1) }
                            docRef?.update("status", "Bị huỷ")?.addOnSuccessListener {
                                // will change to motion toast later
                                Toast.makeText(
                                    context,
                                    R.string.orderDetailCancelArgument,
                                    Toast.LENGTH_SHORT
                                ).show()
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }?.addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    R.string.orderDetailFailedArgument,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        DialogInterface.BUTTON_NEGATIVE -> {}
                    }
                }

            val builder: AlertDialog.Builder = AlertDialog.Builder(context)

            builder.setMessage(R.string.orderDetailProcessingCancelLabel)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show()
        }
        // accept order
        val acceptButton = binding.orderDetailAcceptOrderButton
        acceptButton.setOnClickListener {
            val dialogClickListener =
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            val docRef =
                                orderID?.let { it1 -> db.collection("orders").document(it1) }
                            docRef?.update("status", "Đã giao")?.addOnSuccessListener {
                                // will change to motion toast later
                                Toast.makeText(
                                    context,
                                    R.string.orderDetailAcceptSuccessfulArgument,
                                    Toast.LENGTH_SHORT
                                ).show()
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }?.addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    R.string.orderDetailAcceptFailedArgument,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        DialogInterface.BUTTON_NEGATIVE -> {}
                    }
                }

            val builder: AlertDialog.Builder = AlertDialog.Builder(context)

            builder.setMessage(R.string.orderDetailToShipAcceptLabel)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show()
        }
        return view
    }

    fun changeStatusText(status: String): String {
        return when {
            status.contains("xử lý", true) -> getString(R.string.my_shop_order_to_ship_button)
            status.contains("huỷ", true) -> getString(R.string.my_order_cancelled_button)
            status.contains("trả đang duyệt", true) -> getString(R.string.my_order_detail_item_return_in_process)
            status.contains("trả thành công", true) -> getString(R.string.my_order_detail_item_return_success)
            status.contains("trả thất bại", true) -> getString(R.string.my_order_detail_item_return_failed)
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
            OrderDetailCaseShopToShipFragment().apply {
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