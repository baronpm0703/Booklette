package com.example.booklette

import android.app.ActionBar.LayoutParams
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.booklette.databinding.FragmentShopOrderDetailReturnBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso
import java.io.File

private const val ORDERID_PARAM = "param1"

class OrderDetailCaseShopReturnFragment : Fragment() {
    private var selectedImageUri: Uri? = null

    // TODO: Rename and change types of parameters
    private var orderID: String? = null
    private var _binding: FragmentShopOrderDetailReturnBinding? = null
    private lateinit var imagePicker: ImageView
    private lateinit var imagePicker2: ImageView

    private lateinit var itemsFragment: OrderDetailItemListFragment
    private var URI1: String = ""
    private var URI2: String = ""

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var cloudStorage: FirebaseStorage
    private lateinit var documentID: String
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
        _binding = FragmentShopOrderDetailReturnBinding.inflate(inflater, container, false)
        val view = binding.root

        db = Firebase.firestore
        auth = Firebase.auth
        cloudStorage = Firebase.storage
        val orderItemLayout = binding.orderDetailProductsFragmentFrameLayout
        val reasonField = binding.returnReasonField
        val shopReasonField = binding.shopReasonField
        val returnRef = db.collection("returnNExchange").whereEqualTo("orderID", orderID)

        imagePicker = binding.imagePicker
        imagePicker2 = binding.imagePicker2
        val returnReasonLabel = binding.returnReasonLabel
        val statusField = binding.statusField
        val shopReasonFrameLayout = binding.orderDetailOrderShopReasonFrameLayout
        val cancelRequestButton = binding.orderDetailRejectRequestButton
        val contactBuyerButton = binding.orderDetailContactBuyerButton
        val acceptRequestButton = binding.orderDetailAcceptRequestButton
        returnRef.get()
            .addOnSuccessListener { QuerySnapshot ->
                for (document in QuerySnapshot) {
                    documentID = document.id
                    val returnData = document.data
                    val imageList = returnData["image"] as? List<String>
                    val itemList = returnData["itemID"] as? List<String>
                    val shopReason = returnData["shopReason"] as? String
                    val status = returnData["status"] as String
                    if (!status.contains("trả đang duyệt", true)) {
                        shopReasonField.setText(
                            shopReason ?: getString(R.string.no_reason_provided)
                        )
                        shopReasonField.isEnabled = false
                        contactBuyerButton.layoutParams.width = LayoutParams.MATCH_PARENT
                        cancelRequestButton.visibility = View.GONE
                        acceptRequestButton.visibility = View.GONE
                    }
                    statusField.text = changeStatusText(status)
                    val reason = returnData["reason"] as? String
                    reasonField.setText(reason ?: getString(R.string.no_reason_provided))
                    if (imageList != null) {
                        if (imageList[0].isNotEmpty() && imageList[1].isNotEmpty()) {
                            Picasso.get().load(imageList[0]).into(imagePicker)
                            Picasso.get().load(imageList[1]).into(imagePicker2)
                        } else if (imageList[0].isNotEmpty() || imageList[1].isNotEmpty()) {
                            if (imageList[0].isNotEmpty()) {
                                Picasso.get().load(imageList[0]).into(imagePicker)
                            } else {
                                Picasso.get().load(imageList[1]).into(imagePicker)
                            }
                            imagePicker2.visibility = View.GONE
                        } else {
                            returnReasonLabel.visibility = View.GONE
                            imagePicker.visibility = View.GONE
                            imagePicker2.visibility = View.GONE
                        }
                    }
                    orderID?.let {
                        db.collection("orders")
                            .document(it)
                            .get()
                            .addOnSuccessListener { document ->
                                val orderData = document.data
                                val itemsMap =
                                    orderData?.get("items") as? Map<String, Map<String, Any>>


                                var itemsToDisplay = mutableMapOf<String, Map<String, Any>>()
//                                itemsMap?.forEach { (itemID, itemData) ->
//                                    val bookMap : Map<String,Map<String,Any>> = itemData as Map<String,Map<String,Any>>
//                                    if (itemList != null) {
//                                        if (itemList.contains(itemID)){
//                                            itemsToDisplay[itemID] = itemData
//                                            Log.d("itemsToDisplay",itemsToDisplay.toString())
//                                        }
//                                    }
//                                }
                                itemsMap?.flatMap { (shopID, itemMap) ->
                                    itemMap.map { (itemId, itemData) ->
//                                        Log.d("shopID", shopID)
//                                        Log.d("itemId", itemId)
//                                        Log.d("itemData", itemData.toString())
                                        if (itemList != null) {
                                            if (itemList.contains(itemId)) {
                                                itemsToDisplay[shopID] = itemMap
                                                Log.d("itemsToDisplay", itemsToDisplay.toString())
                                            }
                                        }
                                    }
                                }
                                // setup recycler view for books
                                itemsFragment = OrderDetailItemListFragment.newInstance(
                                    1,
                                    itemsToDisplay,
                                    allowSelection = false,
                                    allowMultipleSelection = false
                                )
                                childFragmentManager.beginTransaction()
                                    .replace(orderItemLayout.id, itemsFragment)
                                    .commit()

                            }
                    }


                }
            }
            .addOnFailureListener {
                Log.e("Loi", it.toString())
            }


        // back
        val backButton = binding.backButton
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        cancelRequestButton.setOnClickListener {
            val dialogClickListener =
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            val returnDocumentRef =
                                db.collection("returnNExchange").document(documentID)
                            val updates = hashMapOf<String, Any>(
                                "shopReason" to shopReasonField.text.toString(),
                                "status" to "Yêu cầu hoàn trả bị từ chối",
                            )
                            returnDocumentRef
                                .update(updates)
                                .addOnSuccessListener {
                                    Log.d(
                                        "returnDocument",
                                        "DocumentSnapshot successfully updated!"
                                    )
                                    val orderDocumentRef = orderID?.let { it1 ->
                                        db.collection("orders").document(it1)
                                    }
                                    orderDocumentRef?.update(
                                        "status",
                                        "Yêu cầu hoàn trả bị từ chối"
                                    )
                                        ?.addOnSuccessListener {
                                            Log.d(
                                                "orderDocument",
                                                "DocumentSnapshot successfully updated!"
                                            )
                                            Toast.makeText(
                                                context,
                                                R.string.orderShopDetailReturnReject,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            requireActivity().onBackPressedDispatcher.onBackPressed()
                                        }
                                        ?.addOnFailureListener { e ->
                                            Log.e("orderDocument", "Error updating document", e)
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.w("returnDocument", "Error updating document", e)
                                }
                        }

                        DialogInterface.BUTTON_NEGATIVE -> {}
                    }
                }

            val builder: AlertDialog.Builder = AlertDialog.Builder(context)

            builder.setMessage(R.string.orderDetailReturnRejectLabel)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show()

        }

        contactBuyerButton.setOnClickListener {
            //later
        }

        acceptRequestButton.setOnClickListener {
            val dialogClickListener =
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            val returnDocumentRef =
                                db.collection("returnNExchange").document(documentID)
                            val updates = hashMapOf<String, Any>(
                                "shopReason" to shopReasonField.text.toString(),
                                "status" to "Yêu cầu hoàn trả thành công",
                            )
                            returnDocumentRef
                                .update(updates)
                                .addOnSuccessListener {
                                    Log.d(
                                        "returnDocument",
                                        "DocumentSnapshot successfully updated!"
                                    )
                                    val orderDocumentRef = orderID?.let { it1 ->
                                        db.collection("orders").document(it1)
                                    }
                                    orderDocumentRef?.update(
                                        "status",
                                        "Yêu cầu hoàn trả thành công"
                                    )
                                        ?.addOnSuccessListener {
                                            Log.d(
                                                "orderDocument",
                                                "DocumentSnapshot successfully updated!"
                                            )
                                            Toast.makeText(
                                                context,
                                                R.string.orderDetailReturnAcceptLabel,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            requireActivity().onBackPressedDispatcher.onBackPressed()
                                        }
                                        ?.addOnFailureListener { e ->
                                            Log.e("orderDocument", "Error updating document", e)
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.w("returnDocument", "Error updating document", e)
                                }
                        }

                        DialogInterface.BUTTON_NEGATIVE -> {
                        }
                    }
                }

            val builder: AlertDialog.Builder = AlertDialog.Builder(context)

            builder.setMessage(R.string.orderDetailReturnAcceptLabel)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show()
        }

        return view
    }

    fun changeStatusText(status: String): String {
        return when {
            status.contains("xử lý", true) -> getString(R.string.my_order_processing_button)
            status.contains("huỷ", true) -> getString(R.string.my_order_cancelled_button)
            status.contains(
                "trả đang duyệt",
                true
            ) -> getString(R.string.my_order_detail_item_return_in_process)

            status.contains(
                "trả thành công",
                true
            ) -> getString(R.string.my_order_detail_item_return_success)

            status.contains(
                "trả thất bại",
                true
            ) -> getString(R.string.my_order_detail_item_return_failed)

            status.contains("thành công", true) -> getString(R.string.my_order_completed_button)
            status.contains("đã giao", true) -> getString(R.string.my_order_delivered_button)
            else -> ""
        }
    }

    fun getFileNameFromUri(uri: Uri): String {
        val file = File(uri.path)
        return file.name
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
            OrderDetailCaseShopReturnFragment().apply {
                arguments = Bundle().apply {
                    putString(ORDERID_PARAM, orderID)
                }
            }
    }

    fun dpToPx(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
    private fun setImageToImageView2() {
        selectedImageUri?.let { uri ->
            Picasso.get().load(uri).into(imagePicker2)

        }

    }
    // Lấy ảnh từ user (Hai?)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}