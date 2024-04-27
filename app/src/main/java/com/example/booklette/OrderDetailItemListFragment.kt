package com.example.booklette

import VerticalSpaceItemDecoration
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var itemsMap: Map<String, Map<String,Any>>
    private var listBooks = arrayListOf<DetailBookItem>()

    private var allowSelection: Boolean = false
    private var allowMultipleSelection: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)

            // Retrieve itemsMap from arguments
            itemsMap = it.getSerializable(ARG_ITEMS_MAP) as? Map<String, Map<String,Any>> ?: emptyMap()

            allowSelection = it.getBoolean(ARG_SELECTION_CHECK)

            allowMultipleSelection = it.getBoolean(ARG_MULTIPLE_CHECK)
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


        itemsMap?.flatMap { (shopID, itemMap) ->
            itemMap.map { (itemId, itemData) ->
                Log.d("shopID", shopID)
                Log.d("itemId", itemId)
                Log.d("itemData", itemData.toString())

                // Get shop name
                db.collection("accounts")
                    .whereEqualTo("UID", shopID)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        for (document in querySnapshot) {
                            val storeData = document.data
                            val bookStoreName = storeData["fullname"].toString()

                            // Fetch books after getting store name
                            db.collection("books")
                                .whereEqualTo("bookID", itemId)
                                .get()
                                .addOnSuccessListener { bookQuerySnapshot ->
                                    for (bookDocument in bookQuerySnapshot) {
                                        val bookData = bookDocument.data
                                        val id = itemId
                                        val name = bookData["name"].toString()
                                        val author = bookData["author"].toString()
                                        val imageUrl = bookData["image"].toString()

                                        val itemMap = itemData as? Map<*, *>
                                        val price = itemMap?.get("totalSum") as Number
                                        val floatPrice = price.toFloat()
                                        val quantity = itemMap?.get("quantity") as Long

                                        val detailBookItem = DetailBookItem(id, name, author, quantity, floatPrice, imageUrl, bookStoreName)
                                        listBooks.add(detailBookItem)
                                    }

                                    // Update the adapter after fetching all books
                                    adapter = OrderDetailItemListRecyclerViewAdapter(listBooks)
                                    if (allowSelection){
                                        adapter.allowSelection()
                                        if (allowMultipleSelection){
                                            adapter.allowMultiple()
                                        }
                                    }
                                    view.adapter = adapter
                                    view.layoutManager = LinearLayoutManager(context)
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("Loi", exception.toString())
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("Loi", exception.toString())
                    }
            }
        }

        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = OrderDetailItemListRecyclerViewAdapter(listBooks)
                val spacing = 20.dpToPx(context) // Convert 10dp to pixels
                addItemDecoration(VerticalSpaceItemDecoration(spacing))
            }
        }





        return view
    }
    fun Int.dpToPx(context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"
        const val ARG_ITEMS_MAP = "items-map"
        const val ARG_SELECTION_CHECK = "default-false"
        const val ARG_MULTIPLE_CHECK = "default-false"
        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int, itemsMap: Map<String, Any>, allowSelection: Boolean, allowMultipleSelection: Boolean): OrderDetailItemListFragment {
            return OrderDetailItemListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                    // Pass itemsMap as an argument
                    putSerializable(ARG_ITEMS_MAP, itemsMap as Serializable)
                    // Pass selection bool and multiple
                    putBoolean(ARG_SELECTION_CHECK, allowSelection)
                    putBoolean(ARG_MULTIPLE_CHECK, allowMultipleSelection)
                }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}