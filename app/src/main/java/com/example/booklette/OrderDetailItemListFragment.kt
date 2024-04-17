package com.example.booklette

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.booklette.databinding.FragmentOrderDetailItemListBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.io.Serializable

/**
 * A fragment representing a list of Items.
 */
class OrderDetailItemListFragment : Fragment() {

    private var columnCount = 1
    private var _binding: FragmentOrderDetailItemListBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!

    // Define a property to hold the itemsMap
    private lateinit var itemsMap: Map<String, Any>
    private var listBooks = arrayListOf<DetailBookItem>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)

            // Retrieve itemsMap from arguments
            itemsMap = it.getSerializable(ARG_ITEMS_MAP) as? Map<String, Any> ?: emptyMap()
            Log.d("items",itemsMap.toString())
        }
    }
    private lateinit var bookIDs: ArrayList<String>
    private lateinit var adapter: OrderDetailItemListRecyclerViewAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrderDetailItemListBinding.inflate(inflater, container, false)
        val view = binding.root

        val db = Firebase.firestore

        bookIDs = ArrayList()


        itemsMap?.forEach { (itemID, itemData) ->
            Log.d("itemID", itemID)
            db.collection("books")
                .whereEqualTo("bookID", itemID)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        val bookData = document.data
                        val id = itemID
                        val name = bookData["name"].toString()
                        val author = bookData["author"].toString()
                        val imageUrl = bookData["image"].toString()

                        val itemMap = itemData as? Map<String, Any>
                        val price = itemMap?.get("totalSum") as Number
                        val floatPrice = price.toFloat()
                        val quantity = itemMap?.get("quantity") as Long

                        val detailBookItem = DetailBookItem(id, name, author, quantity, floatPrice, imageUrl)
                        listBooks.add(detailBookItem)
                    }
                    adapter = OrderDetailItemListRecyclerViewAdapter(listBooks)
                    view.adapter = adapter
                    view.layoutManager = LinearLayoutManager(context)
                }
                .addOnFailureListener { exception ->
                    Log.e("Loi", exception.toString())
                }
        }

        // Set the adapter
//        if (view is RecyclerView) {
//            with(view) {
//                layoutManager = when {
//                    columnCount <= 1 -> LinearLayoutManager(context)
//                    else -> GridLayoutManager(context, columnCount)
//                }
//                adapter = OrderDetailItemListRecyclerViewAdapter(listBooks)
//
//            }
//        }


        return view
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"
        const val ARG_ITEMS_MAP = "items-map"
        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int, itemsMap: Map<String, Any>): OrderDetailItemListFragment {
            return OrderDetailItemListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                    // Pass itemsMap as an argument
                    putSerializable(ARG_ITEMS_MAP, itemsMap as Serializable)
                }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}