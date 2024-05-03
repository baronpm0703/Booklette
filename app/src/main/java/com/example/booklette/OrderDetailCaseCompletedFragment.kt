package com.example.booklette

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.booklette.databinding.FragmentOrderDetailCompletedBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date

private const val ORDERID_PARAM = "param1"

class OrderDetailCaseCompletedFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var orderID: String? = null
    private var _binding: FragmentOrderDetailCompletedBinding? = null

    private var itemsMap: Map<String, Map<String, Any>>? = null
    private lateinit var itemsFragment: OrderDetailItemListFragment

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
        _binding = FragmentOrderDetailCompletedBinding.inflate(inflater, container, false)
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
//                            tempTotalOrgMoney += (itemMap?.get("totalSum") as Number).toLong()
//                            totalQuantity += itemMap?.get("quantity") as Long
//                        }
                    val totalMoney = (orderData?.get("totalSum") as Number).toLong()
                    val status = orderData?.get("status") as String

                    val paymentMethod = orderData?.get("paymentMethod") as? Map<String, Any>
                    val paymentMethodType = paymentMethod?.get("Type")
                    val shippingAddress = orderData?.get("shippingAddress") as String
                    val beforeDiscount = (orderData?.get("beforeDiscount") as Number).toLong()
                    // setup recycler view for books
                    itemsFragment = OrderDetailItemListFragment.newInstance(
                        1,
                        itemsMap!!,
                        allowSelection = true,
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

                            totalQuantity += ((itemData as Map<*, *>)["quantity"] as? Long) ?: 0
                            tempTotalOrgMoney += (itemData["totalSum"] as? Long ?: 0)
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

        // review order
        val reviewButton = binding.orderDetailReviewButton
        reviewButton.setOnClickListener {

//            val returnFragment = orderID?.let { it1 -> OrderDetailCaseReturnFragment.newInstance(it1) }
//            if (context is homeActivity){
//                if (returnFragment != null) {
//                    (context as homeActivity).changeFragmentContainer(returnFragment, (context as homeActivity).smoothBottomBarStack[(context as homeActivity).smoothBottomBarStack.size - 1])
//                }
//            }
            var bdFragment = BookDetailFragment()

            var bundle = Bundle()
            var chosenBookID = ""
            val listBookID: List<String> = itemsFragment.getListClickedBookID()
            if (listBookID.isEmpty()) {
                val instruction = context?.getString(R.string.review_instruction)
                instruction?.let {
                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                }
            } else {
                bundle.putString("bookID", itemsFragment.getListClickedBookID()[0])
                bundle.putBoolean("openReview", true)
                Toast.makeText(context, itemsFragment.getListClickedBookID()[0], Toast.LENGTH_SHORT)
                    .show()
                //            bundle.putString("bookID", itemsFragment.getListClickedBookID()[0])

                bdFragment.arguments = bundle

                val homeAct = (context as homeActivity)
                homeAct.changeFragmentContainer(bdFragment, (context as homeActivity).smoothBottomBarStack[(context as homeActivity).smoothBottomBarStack.size - 1]) //Let the homePage handle changing fragment
            }

        }

        binding.orderDetailViewEInvoiceButton.setOnClickListener{
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
            OrderDetailCaseCompletedFragment().apply {
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