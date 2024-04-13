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

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.util.Date

class MyOrderItemFragment : Fragment() {

    private var columnCount = 1
    private lateinit var adapter: MyOrderItemRecyclerViewAdapter
    private var userOrders = arrayListOf<OrderDataClass>()
    private var originalValues = arrayListOf<OrderDataClass>()
    private lateinit var db : FirebaseFirestore
    private var _binding: MyOrderItemListBinding? = null

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
        db = Firebase.firestore

        // Fetch data from Firestore
        db.collection("orders")
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val orderID = document.id
                    val orderData = document.data
                    val timeStamp = orderData?.get("creationDate") as Timestamp
                    val date: Date? = timeStamp?.toDate()
                    val trackingNumber: String = orderData?.get("orderID") as String
                    val itemsMap = orderData?.get("items") as? Map<String, Any>
                    var totalQuantity: Long = 0
                    itemsMap?.forEach { (_, itemData) ->
                        val itemMap = itemData as? Map<String, Any>
                        totalQuantity += itemMap?.get("quantity") as Long
                    }
                    val totalMoney = (orderData?.get("totalSum") as Long).toFloat()
                    val status = orderData?.get("status") as String
                    val newOrder = date?.let {
                        OrderDataClass(orderID,
                            it, trackingNumber, totalQuantity, totalMoney, status)
                    }
                    if (newOrder != null) {
                        userOrders.add(newOrder)

                    }
                    originalValues = ArrayList(userOrders)
                }
                adapter = MyOrderItemRecyclerViewAdapter(userOrders)

                // Check if the listener is already set before assigning it
                if (adapter.onButtonClick == null) {
                    adapter.onButtonClick = orderItemClickListener
                }

                view.adapter = adapter
                view.layoutManager = LinearLayoutManager(context)
            }
            .addOnFailureListener { exception ->
                Log.w("CanhBao", "Error getting documents.", exception)
            }

        return view
    }
    private var orderItemClickListener: ((OrderDataClass) -> Unit)? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is homeActivity) {
            orderItemClickListener = { orderItem ->
                val detailFragment = OrderDetailFragment.newInstance(orderItem.trackingNumber)
                (context ).changeFragmentContainer(detailFragment, (context ).smoothBottomBarStack[(context ).smoothBottomBarStack.size - 1])
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        orderItemClickListener = null
    }

    fun filterStatus(query: String) {
        userOrders.clear()
        if (query.isBlank()) {
            userOrders.addAll(ArrayList(originalValues))  // If the query is empty, show the original list
        } else {
            userOrders.addAll(ArrayList(originalValues.filter { it.status.contains(query, ignoreCase = true) }))  // Filter by status
        }
        adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
    }
    fun filterName(query: String) {
        userOrders.clear()
        if (query.isBlank()) {
            userOrders.addAll(ArrayList(originalValues))  // If the query is empty, show the original list
        } else {
            userOrders.addAll(ArrayList(originalValues.filter { it.trackingNumber.contains(query, ignoreCase = true) }))  // Filter by status
        }
        adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }


    fun completedButton(){
        filterStatus("Thành công")
    }
    fun processingButton(){
        filterStatus("Đang xử lý")
    }
    fun cancelledButton(){
        filterStatus("huỷ")
    }
    fun returnedButton(){
        filterStatus("trả")
    }
    fun unfilter(){
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
