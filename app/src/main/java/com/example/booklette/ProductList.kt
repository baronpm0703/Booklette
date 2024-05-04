package com.example.booklette


import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.booklette.databinding.FragmentProductListBinding
import com.example.booklette.model.ProductsObject
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.maxkeppeler.sheets.core.SheetStyle
import com.maxkeppeler.sheets.option.DisplayMode
import com.maxkeppeler.sheets.option.Option
import com.maxkeppeler.sheets.option.OptionSheet
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.max
import kotlin.math.min

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

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var gvProductListAdapter: ProductListFragmentGridViewAdapter

    private var chosenGenre: String? = null
    private var wordToSearch: String? = null
    private var searchRes = ArrayList<String>()
    private var bookList: ArrayList<ProductsObject> = ArrayList()

    private var smallestAmount: Float = Float.MAX_VALUE
    private var largestAmount: Float = 0.0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        val view = binding.root

        // Authen and get data
        auth = Firebase.auth
        db = Firebase.firestore

        // After passing the selected genre from categories, set to this
        if (this.arguments?.getString("Genre") != null) {
            binding.selectedGenre.setText(this.arguments?.getString("Genre").toString() )
            chosenGenre = this.arguments?.getString("Genre").toString()
        }
        if (this.arguments?.getString("WordToSearch") != null) {
            binding.selectedGenre.setText(this.arguments?.getString("WordToSearch").toString() )
            wordToSearch = this.arguments?.getString("WordToSearch").toString()
            searchRes = requireArguments().getStringArrayList("SearchResult")!!
        }

        // Init filter dialog
        val initValues = InitFilterValuesProductList()
        initValues.rangslider.apply {
            add(smallestAmount)
            add(largestAmount)
        }
        var filterDialogProductList = FilterDialogProductList(true, initValues)
        if (searchRes.isNotEmpty())
            filterDialogProductList = FilterDialogProductList(false, initValues)

        // Set up back btn
        binding.ivBackToPrev.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Product List Grid view
        gvProductListAdapter = activity?.let { ProductListFragmentGridViewAdapter(it, bookList) }!!

        binding.gvProductList.adapter = gvProductListAdapter
        binding.gvProductList.visibility = View.GONE
        binding.notFoundBooks.visibility = View.GONE

        // Generate data
        if (!chosenGenre.equals(null)) {
            binding.ivSearch.visibility = View.GONE
            val docRef = db.collection("books").whereEqualTo("genre", chosenGenre).get()
            docRef.addOnSuccessListener { result ->
                lifecycleScope.launch {
                    if (result.isEmpty) {
                        binding.notFoundBooks.visibility = View.VISIBLE
                    }
                    val bookIDListToCalc = ArrayList<String>()
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
                        bookIDListToCalc.add(document.data["bookID"].toString())
                        val dDate = document.data["releaseDate"] as com.google.firebase.Timestamp

                        // Query BookPrice
                        val bookPrice: ArrayList<Float> = getBookPrice(document.data["bookID"].toString())
                        val bookQuantity = getBookQuantity(document.data["bookID"].toString())
                        // Each Store have diff price
                        bookPrice.forEach {
                            smallestAmount = min(smallestAmount, it)
                            largestAmount = max(largestAmount, it)

                            // Update the filter range slider
                            initValues.rangslider[0] = smallestAmount
                            initValues.rangslider[1] = largestAmount
                            filterDialogProductList.updateInitValues(initValues)


                            val productsObject = ProductsObject(
                                document.data["bookID"].toString(),
                                document.data["name"].toString(),
                                document.data["genre"].toString(),
                                document.data["author"].toString(),
                                dDate.toDate(),
                                document.data["image"].toString(),
                                it,
                                avg_rating / rating_num,
                                document.data["type"].toString()
                            )
                            productsObject.quantitySale = bookQuantity
                            bookList.add(productsObject)

                        }

                    }

                    bookList.sortBy {
                            book -> book.price
                    }

                    gvProductListAdapter.notifyDataSetChanged()


                    Handler().postDelayed({
                        binding.gvProductList.visibility = View.VISIBLE
                        binding.loadingAnimProductList.visibility = View.GONE
                    }, 2000)
                }
            }
        }
        if (!wordToSearch.equals(null)) {
            binding.horizontalScrollView.visibility = View.VISIBLE
            val params = (binding.relativeLayout2.layoutParams as ViewGroup.MarginLayoutParams)
            params.setMargins(0, 230, 0, 0)

            if (searchRes.isEmpty()) { binding.notFoundBooks.visibility = View.VISIBLE }
            else {
                searchRes.forEach {bookName ->
                    val docRef = db.collection("books").whereEqualTo("name", bookName).get()
                    docRef.addOnSuccessListener { result ->
                        if (result.isEmpty) {
                            binding.unavailbleInfo.visibility = View.VISIBLE
                        }

                        lifecycleScope.launch {
                            val bookIDListToCalc = ArrayList<String>()
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
                                bookIDListToCalc.add(document.data["bookID"].toString())
                                val dDate = document.data["releaseDate"] as com.google.firebase.Timestamp

                                val bookPrice: ArrayList<Float> = getBookPrice(document.data["bookID"].toString())
                                val bookQuantity = getBookQuantity(document.data["bookID"].toString())
                                // Each Store have diff price
                                bookPrice.forEach {
                                    smallestAmount = min(smallestAmount, it)
                                    largestAmount = max(largestAmount, it)
                                    initValues.rangslider[0] = smallestAmount
                                    initValues.rangslider[1] = largestAmount
                                    filterDialogProductList.updateInitValues(initValues)

                                    val productsObject = ProductsObject(
                                        document.data["bookID"].toString(),
                                        document.data["name"].toString(),
                                        document.data["genre"].toString(),
                                        document.data["author"].toString(),
                                        dDate.toDate(),
                                        document.data["image"].toString(),
                                        it,
                                        avg_rating / rating_num,
                                        document.data["type"].toString()
                                    )
                                    productsObject.quantitySale = bookQuantity
                                    bookList.add(productsObject)
                                }
                            }
                            if (gvProductListAdapter != null){
                                bookList.sortBy {
                                        book -> book.price
                                }

                                gvProductListAdapter.notifyDataSetChanged()

                                Handler().postDelayed({
                                    binding.gvProductList.visibility = View.VISIBLE
                                    binding.loadingAnimProductList.visibility = View.GONE
                                }, 2000)
                            }
                        }
                    }
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
                        Option(R.drawable.star,"Popular"),
                        Option(R.drawable.resource_new, "Newest"),
                        Option(R.drawable.favourite,"Customer Review"),
                        Option(R.drawable.lowtohigh, "Price: Lowest to high"),
                        Option(R.drawable.hightolow,"Price: Highest to low")

                    )
                    onPositive { index: Int, option: Option ->
                        // Handle selected option
                        when (index){
                            0 -> {
                                bookList.sortBy {
                                    book -> book.quantitySale
                                }
                                bookList.reverse()
                                gvProductListAdapter?.notifyDataSetChanged()
                                binding.tvSort.text = "Popular"
                            }
                            1 -> {
                                bookList.sortBy {
                                        book -> book.releaseDate
                                }
                                gvProductListAdapter?.notifyDataSetChanged()
                                binding.tvSort.text = "Newest"
                            }
                            2 -> {
                                bookList.sortBy {
                                        book -> book.rating
                                }
                                bookList.reverse()
                                gvProductListAdapter?.notifyDataSetChanged()
                                binding.tvSort.text = "Customer Review"
                            }
                            3 -> {
                                bookList.sortBy {
                                        book -> book.price
                                }
                                gvProductListAdapter?.notifyDataSetChanged()
                                binding.tvSort.text = "Price: Lowest to high"
                            }
                            4 -> {
                                bookList.sortBy {
                                        book -> book.price
                                }
                                bookList.sortBy {
                                        book -> book.price
                                }
                                bookList.reverse()
                                gvProductListAdapter?.notifyDataSetChanged()
                                binding.tvSort.text = "Price: Highest to low"
                            }
                        }
//                        Toast.makeText(activity, option.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Set onClick for filter dialog be displayed
        binding.tvfilter.setOnClickListener {
            activity?.let {
//                val newTheme = R.style.BottomSheetSignNightTheme
//                requireActivity().theme.applyStyle(newTheme, true)
                filterDialogProductList.show(it){
                    style(SheetStyle.BOTTOM_SHEET)
                    onPositive {
                        Toast.makeText(activity, "Is triggered", Toast.LENGTH_SHORT).show()
                        this.dismiss()
                        FilterWithAttributes(getSliderValues(), getChosenCategory(), getChosenType(), getChosenAge())
                    }
                }
            }
        }

        // Inflate the layout for this fragment
        return view
    }

    override fun onResume() {
        super.onResume()
        bookList.clear()

    }
    suspend fun getBookQuantity(bookID: String): Int {
        var res = 0
        return try {
            val querySnapshot = db.collection("orders").whereNotEqualTo("items.$bookID.quantity", null).get().await()
            for (document in querySnapshot.documents) {
                val orderList = document.data?.get("items") as? Map<String, Any>
                val orderItemDetail = orderList?.get(bookID) as? Map<String, Any>

                val quantity = orderItemDetail?.get("quantity").toString()
                if (quantity.isNotEmpty()) {
                    res += quantity.toInt()
                }
            }
            return res

        } catch (e: Exception) {
            // Handle failures
            Log.e("Firestore", "Error getting book quantity", e)
            // Return a default value in case of failure
            0
        }
    }

    fun getBookPrice(bookID: String): ArrayList<Float> {
        val res = ArrayList<Float>()
        try {
            val querySnapshot = db.collection("personalStores").whereNotEqualTo("items.$bookID", null).get()

            querySnapshot.addOnSuccessListener { result ->
                if (result.isEmpty) {
                    res.add(0.0F)
                } else {
                    for (document in result) {
                        val bookList = document.data.get("items") as? Map<String, Any>
                        val bookDetail = bookList?.get(bookID) as? Map<String, Any>
                        val price = bookDetail?.get("price").toString()
                        if (price.isNotEmpty()) {
                            res.add(price.toFloat())
                        }
                    }
                }

            }


        } catch (e: Exception) {
            // Handle failures
            Log.e("Firestore", "Error getting book price", e)
            // Return a default value in case of failure
            res.add(0.0F)
        }

        return res
    }


    fun FilterWithAttributes(selectedSlider: ArrayList<String>, selectedCategories: ArrayList<String>, selectedType: ArrayList<String>, selectedAge: ArrayList<String>) {
        val filterList = bookList.filter {
            checkInRangeSlider(it, selectedSlider) && checkInGenres(it, selectedCategories) && checkInCoverType(it, selectedType)
        } as ArrayList

        gvProductListAdapter.updateBookList(filterList)

        gvProductListAdapter.notifyDataSetChanged()
    }
    fun checkInRangeSlider(item: ProductsObject, slider: ArrayList<String>): Boolean {
        if (slider[0].isEmpty() || slider[1].isEmpty()) return true
        return item.price >= slider[0].toFloat() * 1000 && item.price <= slider[1].toFloat() * 1000
    }

    fun checkInGenres(item: ProductsObject, genres: ArrayList<String>): Boolean {
        if (genres.contains("All") || genres.isEmpty()) return true
        return genres.contains(item.genre)
    }

    fun checkInCoverType(item: ProductsObject, typeCover: ArrayList<String>): Boolean {
        if (typeCover.isEmpty()) return true
        return typeCover.contains(item.typeCover)
    }
    override fun onDestroyView() {
        super.onDestroyView()
//        _binding = null
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