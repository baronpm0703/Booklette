package com.example.booklette

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.booklette.databinding.FragmentOrderDetailReviewBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date

private const val ORDERID_PARAM = "param1"
class OrderDetailCaseReviewFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var orderID: String? = null
    private var _binding: FragmentOrderDetailReviewBinding? = null


    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    private lateinit var db : FirebaseFirestore
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
        _binding = FragmentOrderDetailReviewBinding.inflate(inflater, container, false)
        val view = binding.root

        db = Firebase.firestore
        val numberField: TextView = binding.orderDetailNumberField
        val dateField = binding.orderDetailDateField
        val trackingNumberField = binding.orderDetailTrackingNumberField
        val statusField = binding.orderDetailStatusField
        val shippingAddressField = binding.orderDetailShippingAddressField
        val paymentMethodField = binding.orderDetailPaymentMethodField
        val deliveryMethodField = binding.orderDetailDeliveryMethodField
        val discountField = binding.orderDetailDiscountField
        val totalField = binding.orderDetailTotalField

        val orderItemLayout = binding.orderDetailProductsFragmentFrameLayout

        var tempTotalOrgMoney: Float = 0.0F
        orderID?.let {
            db.collection("orders")
                .document(it)
                .get()
                .addOnSuccessListener {document->
                        val orderData = document.data
                        val timeStamp = orderData?.get("creationDate") as Timestamp

                        val date: Date? = timeStamp?.toDate()
                        val itemsMap = orderData?.get("items") as? Map<String, Any>
                        var totalQuantity: Long = 0
                        itemsMap?.forEach { (itemID, itemData) ->

                            val itemMap = itemData as? Map<String, Any>

                            //Log.d("number",itemMap.toString())
                            tempTotalOrgMoney += (itemMap?.get("totalSum") as Number).toFloat()
                            totalQuantity += itemMap?.get("quantity") as Long
                        }
                        val totalMoney = (orderData?.get("totalSum") as Number).toFloat()
                        val status = orderData?.get("status") as String

                        val paymentMethod = orderData?.get("paymentMethod") as? Map<String, Any>
                        val paymentMethodType = paymentMethod?.get("Type")
                        val shippingAddress = orderData?.get("shippingAddress") as String
                        // setup recycler view for books
                        val itemsFragment = OrderDetailItemListFragment.newInstance(1, itemsMap!!)
                        childFragmentManager.beginTransaction()
                            .replace(orderItemLayout.id,itemsFragment)
                            .commit()
                        // assign to field in view
                        var orderName = ""
                        val fetchBookNamesTasks = itemsMap?.map { (itemID, _) ->
                            db.collection("books")
                                .whereEqualTo("bookID", itemID)
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
                                statusField.text = status
                                shippingAddressField.text = shippingAddress
                                paymentMethodField.text = paymentMethodType.toString()
                                deliveryMethodField.text = "Giao Hàng Nhanh (test)"

                                discountField.text = "30% (test)"
                                totalField.text = totalMoney.toString()
                            }

                }
        }


        // back
        val backButton = binding.backButton
        backButton.setOnClickListener{
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
            //waiting for judgment
//            bundle.putString("bookID", this.bookID)
//
//            bdFragment.arguments = bundle
//
//            val homeAct = (context as homeActivity)
//            homeAct.changeFragmentContainer(bdFragment, context.smoothBottomBarStack[context.smoothBottomBarStack.size - 1]) //Let the homePage handle changing fragment
        }
        return view
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
            OrderDetailCaseReviewFragment().apply {
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