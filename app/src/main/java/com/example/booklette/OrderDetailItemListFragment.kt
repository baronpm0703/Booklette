package com.example.booklette

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.booklette.databinding.FragmentOrderDetailItemListBinding
import com.example.booklette.placeholder.PlaceholderContent
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
    private lateinit var itemIDs: ArrayList<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)

            // Retrieve itemsMap from arguments
            itemIDs = it.getStringArrayList(ARG_ITEM_IDS) ?: ArrayList()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrderDetailItemListBinding.inflate(inflater, container, false)
        val view = binding.root

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = OrderDetailItemListRecyclerViewAdapter(PlaceholderContent.ITEMS)
            }
        }
        return view
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"
        const val ARG_ITEM_IDS = "item-ids"
        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int, itemIDs: ArrayList<String>): OrderDetailItemListFragment {
            return OrderDetailItemListFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                    // Pass itemIDs as an argument
                    putStringArrayList(ARG_ITEM_IDS, itemIDs)
                }
            }
        }
    }
}