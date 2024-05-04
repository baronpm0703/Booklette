package com.example.booklette

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.booklette.databinding.MyOrderItemListBinding
import com.google.android.gms.tasks.Tasks

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import java.util.Date

class MyOrderItemFragment : Fragment() {

    private var columnCount = 1
    private lateinit var adapter: MyOrderItemRecyclerViewAdapter
    private var userOrders = arrayListOf<OrderDataClass>()
    private var originalValues = arrayListOf<OrderDataClass>()
    private lateinit var db: FirebaseFirestore
    private var _binding: MyOrderItemListBinding? = null
    private var fullItemNames = arrayListOf<String>()
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = MyOrderItemListBinding.inflate(inflater, container, false)

        val view = binding.root

        // Authentication and database
        val auth = Firebase.auth
        // Get the current user
        val userID = auth.uid

        db = Firebase.firestore

        val ordersRef = db.collection("orders")
        adapter = MyOrderItemRecyclerViewAdapter(requireContext(), userOrders)
        view.adapter = adapter
        view.layoutManager = LinearLayoutManager(context)
        // Fetch data from Firestore
        ordersRef.whereEqualTo("customerID", userID)
            .orderBy("creationDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val orderID = document.id
                    val orderData = document.data
                    val timeStamp = orderData?.get("creationDate") as Timestamp
                    val date: Date? = timeStamp?.toDate()
                    val trackingNumber: String = orderID
                    var orderName = ""
                    var totalQuantity: Long = 0
                    val itemsMap = orderData?.get("items") as? Map<String, Map<String, Any>>
                    Log.d("map", itemsMap.toString())
                    val fetchBookNamesTasks = itemsMap?.flatMap { (shopID, itemMap) ->
                        itemMap.map { (itemId, itemData) ->
                            Log.d("shopID", shopID)
                            Log.d("itemId", itemId)
                            Log.d("itemData", itemData.toString())

                            totalQuantity += ((((itemData as Map<*, *>)["quantity"]) as? Number)?.toLong()) ?: 0

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
                            // Remove the last comma and space from orderName
                            if (orderName.length >= 2) {
                                orderName = orderName.dropLast(2)
                                val copiedName = orderName
                                fullItemNames.add(copiedName)
                            }

                            // Truncate orderName if it exceeds 15 characters
                            if (orderName.length > 15) {
                                orderName = orderName.substring(0, 15) + "..."
                            }

                            // Create OrderDataClass instance
                            val totalMoney = (orderData?.get("totalSum") as? Number)?.toLong() ?: 0
                            val status = orderData?.get("status") as String
                            val newOrder = date?.let {
                                OrderDataClass(
                                    orderName,
                                    it,
                                    trackingNumber,
                                    totalQuantity,
                                    totalMoney,
                                    status
                                )
                            }

                            // Add newOrder to userOrders list
                            if (newOrder != null) {
                                userOrders.add(newOrder)
                                originalValues.add(newOrder)
                            }
                            // Update adapter and layout manager
                            afterQuery()
                        }

                }

            }
            .addOnFailureListener { exception ->
                Log.e("CanhBao", "Error getting documents.", exception)
            }


        return view
    }

    private var orderItemClickListener: ((OrderDataClass) -> Unit)? = null
    private fun afterQuery(){
        Log.d("dataSetUserOrder", userOrders.toString())
        Log.d("dataSetOG", originalValues.toString())
        adapter.notifyDataSetChanged()
        if (adapter.onButtonClick == null) {
            adapter.onButtonClick = orderItemClickListener
        }
        processingButton()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is homeActivity) {
            orderItemClickListener = { orderItem ->
                // đang xử lý => huỷ đơn,
                if (orderItem.status.contains("xử lý", ignoreCase = true)) {
                    val detailFragment =
                        OrderDetailCaseProcessingFragment.newInstance(orderItem.trackingNumber)
                    (context).changeFragmentContainer(
                        detailFragment,
                        (context).smoothBottomBarStack[(context).smoothBottomBarStack.size - 1]
                    )
                }
                // đã giao => trả hoặc xác nhận nhận hàng (không cho huỷ)
                else if (orderItem.status.contains("đã giao", true)) {
                    val detailFragment =
                        OrderDetailCaseDeliveredFragment.newInstance(orderItem.trackingNumber)
                    (context).changeFragmentContainer(
                        detailFragment,
                        (context).smoothBottomBarStack[(context).smoothBottomBarStack.size - 1]
                    )
                }

                // thành công => viết review
                else if (orderItem.status.contains("Thành công") && !orderItem.status.contains("trả thành công", true)){
                    val detailFragment = OrderDetailCaseCompletedFragment.newInstance(orderItem.trackingNumber)
                    (context).changeFragmentContainer(detailFragment, (context).smoothBottomBarStack[(context).smoothBottomBarStack.size - 1])
                }
                // đơn bị huỷ thành công (ko thành công) => viết review
                else if (orderItem.status.contains("huỷ", true)){
                    val detailFragment = OrderDetailCaseCompletedFragment.newInstance(orderItem.trackingNumber)
                    (context).changeFragmentContainer(detailFragment, (context).smoothBottomBarStack[(context).smoothBottomBarStack.size - 1])
                }

                else {
                    val detailFragment =
                        OrderDetailCaseReturnWithResultFragment.newInstance(orderItem.trackingNumber)
                    (context).changeFragmentContainer(
                        detailFragment,
                        (context).smoothBottomBarStack[(context).smoothBottomBarStack.size - 1]
                    )
                }
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        orderItemClickListener = null
    }

    private fun filterStatus(query: String) {
        userOrders.clear()
        if (query.isBlank()) {
            userOrders.addAll(ArrayList(originalValues))  // If the query is empty, show the original list
        } else {
            userOrders.addAll(ArrayList(originalValues.filter {
                it.status.contains(
                    query,
                    ignoreCase = true
                )
            }))  // Filter by status
        }
        adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
    }

    fun filterName(query: String) {
        userOrders.clear()
        if (query.isBlank()) {
            userOrders.addAll(ArrayList(originalValues))  // If the query is empty, show the original list
        } else {
            userOrders.addAll(ArrayList(originalValues.filter {
                it.trackingNumber.contains(
                    query,
                    ignoreCase = true
                )
            }))  // Filter by status
        }
        adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }


    fun completedButton() {
        filterStatus("Thành công")
    }

    fun processingButton() {
        filterStatus("Đang xử lý")
    }

    fun cancelledButton() {
        filterStatus("huỷ")
    }

    fun returnedButton() {
        filterStatus("trả")
    }
    fun returnProcessingButton(){
        filterStatus("trả đang duyệt")
    }
    fun returnSuccessButton(){
        filterStatus("trả thành công")
    }
    fun returnFailedButton(){
        filterStatus("bị từ chối")
    }
    fun deliveredButton() {
        filterStatus("đã giao")
    }

    fun unfilter() {
        filterStatus("")
    }

    companion object {
        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            MyOrderItemFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}
