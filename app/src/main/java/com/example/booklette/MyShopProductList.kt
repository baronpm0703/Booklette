package com.example.booklette

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.booklette.databinding.FragmentProductListBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.maxkeppeler.sheets.core.SheetStyle
import com.maxkeppeler.sheets.option.DisplayMode
import com.maxkeppeler.sheets.option.Option
import com.maxkeppeler.sheets.option.OptionSheet
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyShopProductList.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyShopProductList : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var chosenGenre: String? = null
    private var searchRes: String? = null
    private var bookList: ArrayList<ProductsObject> = ArrayList()
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

        // Authen and get data
        auth = Firebase.auth
        db = Firebase.firestore

        // After passing the selected genre from categories, set to this
        if (this.arguments?.getString("Genre") != null) binding.selectedGenre.setText(this.arguments?.getString("Genre").toString() )
        if (this.arguments?.getString("SearchResult") != null) binding.selectedGenre.setText(this.arguments?.getString("SearchResult").toString() )


        val gvProductListAdapter = activity?.let { ProductListFragmentGridViewAdapter(it, bookList) }

        binding.gvProductList.adapter = gvProductListAdapter

        if (!chosenGenre.equals(null)) {
            val docRef = db.collection("books").whereEqualTo("genre", chosenGenre).get()
            docRef.addOnSuccessListener { result ->
                if (result.isEmpty) {
                    binding.unavailbleInfo.visibility = View.VISIBLE
                }
                val bookIDListToCalcQuantity = ArrayList<String>()
                for (document in result) {
                    var avg_rating = 0.0F;
                    var rating_num = 1;
                    if (document.data.get("review") != null) {
                        val reviewsArray = document.data.get("review") as ArrayList<Map<String, Any>>
                        rating_num = reviewsArray.size

                        for (reviewMap in reviewsArray) {
                            val uid = reviewMap["UID"] as String
                            val image = reviewMap["image"] as String
                            val score = (reviewMap["score"] as Long).toInt()
                            val text = reviewMap["text"] as String

                            avg_rating += score
                        }
                    }
                    bookIDListToCalcQuantity.add(document.data["id"].toString())
                    val dDate = document.data["releaseDate"] as com.google.firebase.Timestamp
                    bookList.add(ProductsObject(
                        document.data["id"].toString(),
                        document.data["name"].toString(),
                        document.data["genre"].toString(),
                        document.data["author"].toString(),
                        dDate.toDate(),
                        document.data["image"].toString(),
                        document.data["price"].toString().toFloat(),
                        avg_rating / rating_num
                    ))
                }

                if (gvProductListAdapter != null){
                    bookList.sortBy {
                            book -> book.price
                    }
                    val quantityEachBook = ScanOrderCalcSellingAmount(bookIDListToCalcQuantity)
                    var index = 0
                    quantityEachBook.forEach {
                        bookList[index++].quantitySale = it.toInt()
                    }
                    gvProductListAdapter.notifyDataSetChanged()

                    Handler().postDelayed({
                        // Code to be executed after the delay
                        // For example, you can start a new activity or update UI elements
                        //                        binding.smHomeFragmentBestDeal.visibility = View.GONE
                        binding.gvProductList.visibility = View.VISIBLE
                        //                        binding.smHomeFragmentBestDeal.stopShimmer()
                    }, 2000)
                }
            }
        }

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
						Option(R.drawable.star, "Popular"),
						Option(R.drawable.resource_new, "Newest"),
						Option(R.drawable.favourite, "Customer Review"),
						Option(R.drawable.lowtohigh, "Price: Lowest to high"),
						Option(R.drawable.hightolow, "Price: Highest to low")

                    )
                    onPositive { index: Int, option: Option ->
                        // Handle selected option
                        Toast.makeText(activity, option.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val filterDialogProductList = FilterDialogProductList(false)
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

    fun ScanOrderCalcSellingAmount(booksNeedObserver: ArrayList<String>): ArrayList<Long>{
        val res = ArrayList<Long>(booksNeedObserver.size)
        res.fill(0)
        db.collection("orders").get().addOnSuccessListener { result ->
            for (doc in result){
                if (doc.data.get("items") != null) {
                    val itemsArray = doc.data.get("items") as ArrayList<Map<String, Any>>

                    for (item in itemsArray) {
                        val itemID = item["itemID"] as String
                        val quantity = item["quantity"] as Long
                        val indexContainItem = booksNeedObserver.indexOf(itemID)
                        if (indexContainItem != -1){
                            res[indexContainItem] += quantity
                        }
                    }
                }
            }
        }
        return res
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
            MyShopProductList().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}