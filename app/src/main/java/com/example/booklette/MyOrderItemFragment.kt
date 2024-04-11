package com.example.booklette

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.booklette.placeholder.PlaceholderContent
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date

class MyOrderItemFragment : Fragment() {

    private var columnCount = 1
    private lateinit var adapter: MyOrderItemRecyclerViewAdapter
    private var userOrders = arrayListOf<OrderDataClass>()
    private var originalValues = arrayListOf<OrderDataClass>()
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
        val view = inflater.inflate(R.layout.my_order_item_list, container, false)

        adapter = MyOrderItemRecyclerViewAdapter(userOrders)

        val db = Firebase.firestore

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = this@MyOrderItemFragment.adapter
            }
        }

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
                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged()

            }
            .addOnFailureListener { exception ->
                Log.w("CanhBao", "Error getting documents.", exception)
            }

        adapter.onButtonClick = { orderItem ->
            val detailFragment = OrderDetailFragment.newInstance(orderItem.trackingNumber)
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fcvNavigation, detailFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return view
    }
    fun filter(query: String) {
        userOrders.clear()
        if (query.isBlank()) {
            userOrders.addAll(ArrayList(originalValues))  // If the query is empty, show the original list
        } else {
            userOrders.addAll(ArrayList(originalValues.filter { it.status.contains(query, ignoreCase = false) }))  // Filter by status
        }
        adapter.notifyDataSetChanged() // Notify the adapter that the data has changed
    }





    fun completedButton(){
        filter("Thành công")
    }
    fun processingButton(){
        filter("Đang xử lý")
    }
    fun cancelledButton(){
        filter("huỷ")
    }
    fun returnedButton(){
        filter("trả")
    }
    fun unfilter(){
        filter("")
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