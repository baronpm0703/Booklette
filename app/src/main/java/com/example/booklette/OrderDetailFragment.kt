package com.example.booklette

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.booklette.databinding.FragmentMyOrderBinding
import com.example.booklette.databinding.FragmentOrderDetailBinding
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ORDERID_PARAM = "param1"

/**
 * A simple [Fragment] subclass.
 * Use the [OrderDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OrderDetailFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var orderID: String? = null
    private var _binding: FragmentOrderDetailBinding? = null

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
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
        _binding = FragmentOrderDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        val db = Firebase.firestore
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

        var tempTotalOrgMoney: Long = 0
        db.collection("orders")
            .whereEqualTo("orderID",orderID)
            .get()
            .addOnSuccessListener {querySnapshot->
                for (document in querySnapshot){
                    val orderData = document.data
                    val timeStamp = orderData?.get("creationDate") as Timestamp

                    val date: Date? = timeStamp?.toDate()
                    val itemsMap = orderData?.get("items") as? Map<String, Any>
                    var totalQuantity: Long = 0
                    itemsMap?.forEach { (itemID, itemData) ->

                        val itemMap = itemData as? Map<String, Any>

                        //Log.d("number",itemMap.toString())
                        tempTotalOrgMoney += itemMap?.get("totalSum") as Long
                        totalQuantity += itemMap?.get("quantity") as Long
                    }
                    val totalMoney = (orderData?.get("totalSum") as Long).toFloat()
                    val status = orderData?.get("status") as String

                    val paymentMethod = orderData?.get("paymentMethod") as? Map<String, Any>
                    val paymentMethodType = paymentMethod?.get("Type")

                    // setup recycler view for books
                    val itemsFragment = OrderDetailItemListFragment.newInstance(1,itemsMap!!)
                    childFragmentManager.beginTransaction()
                        .replace(orderItemLayout.id,itemsFragment)
                        .commit()
                    // assign to field in view
                    numberField.text = orderID
                    val sdf = SimpleDateFormat("dd-MM-yyyy")
                    dateField.text = sdf.format(date)

                    trackingNumberField.text = orderID
                    statusField.text = status
                    shippingAddressField.text = "227 Nguyễn Văn Cừ, P4, Quận 5, Tp HCM."
                    paymentMethodField.text = paymentMethodType.toString()
                    deliveryMethodField.text = "Giao Hàng Nhanh"

                    discountField.text = "30%"
                    totalField.text = totalMoney.toString()


                }
            }


        // back
        val backButton = binding.backButton
        backButton.setOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param orderID Parameter 1.
         * @return A new instance of fragment OrderDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(orderID: String) =
            OrderDetailFragment().apply {
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