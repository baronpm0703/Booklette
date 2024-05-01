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
import com.example.booklette.databinding.FragmentOrderDetailProcessingBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ORDERID_PARAM = "param1"
private const val ORDERNAME_PARAM = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [OrderDetailCaseProcessingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OrderDetailCaseProcessingFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var orderID: String? = null
    private var _binding: FragmentOrderDetailProcessingBinding? = null
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
        _binding = FragmentOrderDetailProcessingBinding.inflate(inflater, container, false)
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
        val viewInvoice = binding.viewInvoice


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

                            trackingNumberField.text = orderID
                            //statusField.text = status
                            if (status.contains("xử lý", true)) {
                                statusField.text =
                                    requireContext().getString(R.string.my_order_processing_button)
                            } else if (status.contains("huỷ", true)) {
                                statusField.text =
                                    requireContext().getString(R.string.my_order_cancelled_button)
                            } else if (status.contains("trả thành công", true)) {
                                statusField.text =
                                    requireContext().getString(R.string.my_order_detail_item_return_success)
                            } else if (status.contains("trả thất bại", true)) {
                                statusField.text =
                                    requireContext().getString(R.string.my_order_detail_item_return_failed)
                            } else if (status.contains("thành công", true)) {
                                statusField.text =
                                    requireContext().getString(R.string.my_order_completed_button)
                            } else if (status.contains("đã giao", true)) {
                                statusField.text =
                                    requireContext().getString(R.string.my_order_delivered_button)
                            }
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


        viewInvoice.setOnClickListener {
            // Tạo một instance mới của EInvoiceFragment
            val eInvoiceFragment = EInvoiceFragment()
            // Tạo Bundle để chứa orderId
            val args = Bundle()
            // Đặt orderId vào Bundle
            args.putString(ORDERID_PARAM, orderID)
            // Đặt Bundle vào fragment
            eInvoiceFragment.arguments = args
            // Lấy instance của homeActivity
            val homeAct = (activity as homeActivity)
            // Chuyển đổi sang EInvoiceFragment và truyền stack hiện tại
            homeAct.changeFragmentContainer(
                eInvoiceFragment,
                homeAct.smoothBottomBarStack[homeAct.smoothBottomBarStack.size - 1]
            )
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
        return view
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
            OrderDetailCaseProcessingFragment().apply {
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