package com.example.booklette

import com.otpview.R
//import android.R
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import com.example.booklette.databinding.FragmentProductListBinding
import com.maxkeppeler.sheets.option.Option
import com.maxkeppeler.sheets.option.OptionSheet

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProductList.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProductList : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        val view = binding.root

        // After passing the selected genre from categories, set to this
        binding.selectedGenre.setText(this.arguments?.getString("Genre").toString() )

        val bookList = ArrayList<String>()
        bookList.add("1")
        bookList.add("2")
        bookList.add("2")
        bookList.add("2")
        bookList.add("1")
        bookList.add("1")
        bookList.add("1")
        bookList.add("1")

        binding.gvProductList.adapter =
                activity?.let {ProductListFragmentGridViewAdapter(it, bookList)}

        // Pre-set checked item form radio group
        binding.linearLayout.visibility = View.GONE
        val checked = binding.sortItem.getChildAt(3) as RadioButton

        checked.setBackgroundColor(Color.parseColor("#D45555"))
        checked.setTextColor(Color.parseColor("#FFFFFF"))

        // Set up a listener for the RadioGroup
        binding.sortItem.setOnCheckedChangeListener { group, checkedId ->
            for (i in 0 until group.childCount) {
                val radioButton = group.getChildAt(i) as RadioButton
                // Check if the current RadioButton is not the one that is checked
                if (radioButton.id != checkedId) {
                    // Reset background color and text color to default
                    radioButton.setBackgroundColor(Color.TRANSPARENT) // Change to default background color
                    radioButton.setTextColor(Color.BLACK) // Change to default text color
                } else {
                    // If the current RadioButton is the one that is checked, change its appearance
                    radioButton.setBackgroundColor(Color.parseColor("#D45555"))
                    radioButton.setTextColor(Color.parseColor("#FFFFFF"))
                }
            }
        }

        // Set up the select dialog when click the sort
        binding.tvSort.setOnClickListener{
            binding.linearLayout.visibility = View.VISIBLE
        }

        // Apply Sort Type
        binding.ivApplySort.setOnClickListener{
            binding.linearLayout.visibility = View.GONE
        }

        binding.ivBackFromSort.setOnClickListener{
            binding.linearLayout.visibility = View.GONE
        }


//        activity?.let {
//            OptionSheet().show(it) {
//                title("Text message")
//                with(
//                    Option(, "Copy"),
//                    Option(R.drawable.ic_cart, "Translate"),
//                    Option(R.drawable.ic_cart, "Paste")
//                )
//                onPositive { index: Int, option: Option ->
//                    // Handle selected option
//                }
//            }
//        }

        // Inflate the layout for this fragment
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProductList.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProductList().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}