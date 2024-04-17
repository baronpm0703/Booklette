package com.example.booklette

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.booklette.databinding.FragmentOrderDetailProcessingBinding
import com.example.booklette.databinding.FragmentOrderDetailReturnBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date

/**
 * A simple [Fragment] subclass.
 * Use the [OrderDetailCaseReturnFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
private const val ORDERID_PARAM = "param1"
class OrderDetailCaseReturnFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var orderID: String? = null
    private var _binding: FragmentOrderDetailReturnBinding? = null

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
        _binding = FragmentOrderDetailReturnBinding.inflate(inflater, container, false)
        val view = binding.root

        db = Firebase.firestore


        val orderItemLayout = binding.orderDetailProductsFragmentFrameLayout

        var tempTotalOrgMoney: Float = 0.0F
        orderID?.let {
            db.collection("orders")
                .document(it)
                .get()
                .addOnSuccessListener {document->
                        val orderData = document.data
                        val itemsMap = orderData?.get("items") as? Map<String, Any>
                        var totalQuantity: Long = 0
                        itemsMap?.forEach { (itemID, itemData) ->

                            val itemMap = itemData as? Map<String, Any>

                            //Log.d("number",itemMap.toString())
                            tempTotalOrgMoney += (itemMap?.get("totalSum") as Number).toFloat()
                            totalQuantity += itemMap?.get("quantity") as Long
                        }

                        // setup recycler view for books
                        val itemsFragment = OrderDetailItemListFragment.newInstance(1, itemsMap!!)
                        childFragmentManager.beginTransaction()
                            .replace(orderItemLayout.id,itemsFragment)
                            .commit()



                }
        }


        // back
        val backButton = binding.backButton
        backButton.setOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        // return button
        val returnButton = binding.returnButton
        returnButton.setOnClickListener {

            //implement later when document finished.
//            val dialogClickListener =
//                DialogInterface.OnClickListener { dialog, which ->
//                    when (which) {
//                        DialogInterface.BUTTON_POSITIVE -> {
//                            val docRef = orderID?.let { it1 -> db.collection("orders").document(it1) }
//                            docRef?.update("status","Bị huỷ")?.addOnSuccessListener {
//                                // will change to motion toast later
//                                Toast.makeText(
//                                    context,
//                                    R.string.orderDetailCancelArgument,
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                                requireActivity().onBackPressedDispatcher.onBackPressed()
//                            }?.addOnFailureListener{
//                                Toast.makeText(
//                                    context,
//                                    R.string.orderDetailFailedArgument,
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        }
//                        DialogInterface.BUTTON_NEGATIVE -> {}
//                    }
//                }
//
//            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
//
//            builder.setMessage(R.string.orderDetailProcessingCancelLabel).setPositiveButton(R.string.yes, dialogClickListener)
//                .setNegativeButton(R.string.no, dialogClickListener).show()
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
            OrderDetailCaseReturnFragment().apply {
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