package com.example.booklette


import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.booklette.databinding.FragmentProductListBinding
import com.maxkeppeler.sheets.core.PositiveListener
import com.maxkeppeler.sheets.core.SheetStyle
import com.maxkeppeler.sheets.option.DisplayMode
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
        if (this.arguments?.getString("Genre") != null) binding.selectedGenre.setText(this.arguments?.getString("Genre").toString() )
        if (this.arguments?.getString("SearchResult") != null) binding.selectedGenre.setText(this.arguments?.getString("SearchResult").toString() )

        val bookList = ArrayList<String>()
        bookList.add("1")
        bookList.add("2")
        bookList.add("2")
        bookList.add("2")
        bookList.add("1")
        bookList.add("3")
        bookList.add("1")
        bookList.add("1")

        binding.gvProductList.adapter =
                activity?.let {ProductListFragmentGridViewAdapter(it, bookList)}

        // Set up the select dialog when click the sort
        binding.tvSort.setOnClickListener{
//            binding.linearLayout.visibility = View.VISIBLE
            activity?.let {
                val newTheme = R.style.BottomSheetSignNightTheme
                requireActivity().theme.applyStyle(newTheme, true)
                OptionSheet().show(it) {
                    title("Sort")
                    style(SheetStyle.BOTTOM_SHEET)
                    displayMode(DisplayMode.LIST)
                    titleColor(Color.parseColor("#FF0000"))
                    with(
                        Option(R.drawable.star,"Popular"),
                        Option(R.drawable.resource_new, "Newest"),
                        Option(R.drawable.favourite,"Customer Review"),
                        Option(R.drawable.lowtohigh, "Price: Lowest to high"),
                        Option(R.drawable.hightolow,"Price: Highest to low")

                    )
                    onPositive { index: Int, option: Option ->
                        // Handle selected option
                        Toast.makeText(activity, option.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val filterDialogProductList = FilterDialogProductList()
        binding.tvfilter.setOnClickListener {
            activity?.let {
//                val newTheme = R.style.BottomSheetSignNightTheme
//                requireActivity().theme.applyStyle(newTheme, true)
                filterDialogProductList.show(it){
                    style(SheetStyle.BOTTOM_SHEET)
                    title("Filter")
                    titleColor(Color.parseColor("#FF0000"))
                    onPositive {
                       ChangeTitle(getSliderValues())
                    }
                }
            }
        }


        // Inflate the layout for this fragment
        return view
    }

    fun ChangeTitle(selectedSlider: ArrayList<String>) {
        binding.selectedGenre.text = selectedSlider[0]
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