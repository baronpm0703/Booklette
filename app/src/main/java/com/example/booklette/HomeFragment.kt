package com.example.booklette

import CustomSuggestionAdapter
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booklette.databinding.FragmentHomeBinding
import com.example.booklette.model.BookObject
import com.example.booklette.model.SimpleFuzzySearch
import com.google.android.material.chip.Chip
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.mancj.materialsearchbar.MaterialSearchBar
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var topBookArrayList = ArrayList<BookObject>()
    private var hcmusBookArrayList = ArrayList<BookObject>()
    private var topBookRating = ArrayList<Float>()
    private var bookCategory = ArrayList<String>();
    private var RCDBookList = ArrayList<BookObject>()
    private var RCDBookRating = ArrayList<Float>()
    private var BookNewArrivalList = ArrayList<BookObject>()
    private var BookNewArrivalSaleList = ArrayList<Float>()

    lateinit var topBookAdapter: TopBookHomeFragmentAdapter
    lateinit var hcmusBookHomeFragmentAdapter: HcmusBookHomeFragmentAdapter
    lateinit var RCDAdapter: HomeFragmentTodayRCDTypeAdapter
    lateinit var RCDBookAdapter: TopBookHomeFragmentAdapter

    private var suggestions = ArrayList<String>()
    private var fuzzySearchArraySample = ArrayList<Map<String, String>>()
    private val REQ_CODE_SPEECH_INPUT = 100

    private var selectedChip: String = ""

    //best deal
    var bestDeals = ArrayList<BookObject>()
    var book_deal_sale = ArrayList<Float>()
    lateinit var bestDealAdapter: HomeFragmentBestDealViewPagerAdapter

    //top book
    var times = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        auth = Firebase.auth
        db = Firebase.firestore

        hcmusBookHomeRVInitialize()

        bestDealInitialize()

        topBookInitialize(inflater)

        handleChipSelected()

        topBookRVInitialize()

        recommandationTypeInitialize()

        recommandationBookInitialize()

        newArrivalInitalize()

        addLogicToSearchBar(inflater)

        if (auth.currentUser != null) {
            binding.txtWelcomeBack.text = "Welcome back, " + auth.currentUser!!.email.toString()
        }

        binding.btnSignOut.setOnClickListener {
            auth.signOut()
            startActivity(Intent(activity, LoginActivity::class.java))
        }

        return view
    }

    private suspend fun getBookNamesAlongSetupDataArraySample(): ArrayList<String>{
        val res = ArrayList<String>()
        val docRef = db.collection("books").get().await()
        docRef.documents.forEach{ doc ->
            val name = doc.data?.get("name").toString()
            val genre = doc.data?.get("genre").toString()
            val author = doc.data?.get("author").toString()
            res.add(name)

            fuzzySearchArraySample.add(mapOf(
                "bookName" to name,
                "genre" to genre,
                "author" to author
            ))
        }
        return res
    }
    private fun addLogicToSearchBar(inflater: LayoutInflater) {
        val customSuggestionAdapter = activity?.let {
            CustomSuggestionAdapter(it, inflater, binding.searchBar)
        }

        if (suggestions.isEmpty()) {
            runBlocking {
                suggestions = getBookNamesAlongSetupDataArraySample()
            }
        }

        if (customSuggestionAdapter != null) {
            customSuggestionAdapter.suggestions = suggestions

            binding.searchBar.setCustomSuggestionAdapter(customSuggestionAdapter)
        } else {
            binding.searchBar.lastSuggestions = suggestions
        }

        binding.searchBar.setSpeechMode(true)
        binding.searchBar.setOnSearchActionListener(object: MaterialSearchBar.OnSearchActionListener{
            override fun onSearchStateChanged(enabled: Boolean) {
                Log.i("State", enabled.toString())
            }

            override fun onSearchConfirmed(text: CharSequence?) {
                Log.i("Confirm", text.toString())
                val wordToSearch = text.toString() // Client input

                // Define the available sample to search, and attribute to which we want our client input compare
                val sfs = SimpleFuzzySearch(fuzzySearchArraySample, arrayListOf("bookName", "genre", "author"))
                // Line break and debug for more result
                val resultFromFuzzySearch = sfs.search(wordToSearch) as ArrayList // Implement my smart search algorithms
                // Define prospect fragment
                val productList = ProductList()
                val args = Bundle()

                // Conditions to define attribute to which client input belong
                // Usually, if the user enter genre keyword, the first array's item have the "genre" attribute type
                if (resultFromFuzzySearch.isNotEmpty() && resultFromFuzzySearch[0][1] == "genre") {
                    // The "resultFromFuzzySearch" return an Array contain possible/available result relate to client input
                    val obj = resultFromFuzzySearch[0][0] as Map<String, String>
                    args.putString("Genre", obj["genre"])
                } else { // Other case, which have bookName or author of book relate to input
                    val listOfBookName = ArrayList<String>() // Just focus on the BookName cause productList about books..
                    resultFromFuzzySearch.forEach { item ->
                        // Place a line break here to watch the structure of the item
                        val obj = item[0] as Map<String, String>
                        val bookName = obj["bookName"].toString()

                        listOfBookName.add(bookName)
                    }
                    args.putString("WordToSearch", wordToSearch)
                    args.putStringArrayList("SearchResult", listOfBookName)
                }

                // Add args to fragment we 'bout change to
                productList.arguments = args
                // Have to cast homePage to "activity as HomePage", otherwise the supportFragment can recognize the host
                val homeAct = (activity as homeActivity)
                homeAct.changeFragmentContainer(productList, homeAct.smoothBottomBarStack[homeAct.smoothBottomBarStack.size - 1]) //Let the homePage handle changing fragment
            }

            override fun onButtonClicked(buttonCode: Int) {
                when (buttonCode) {
                    MaterialSearchBar.BUTTON_NAVIGATION -> {
                        // Handle navigation button click
                        // For example, open a drawer
                        // drawerLayout.openDrawer(GravityCompat.START)
                    }
                    MaterialSearchBar.BUTTON_SPEECH -> {
                        // Handle speech button click
                        promptSpeechInput()
                    }
                }
            }
        })

        // Handle behavior of the search bar, may fit with adding effect
        binding.searchBar.addTextChangeListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                Log.d("LOG_TAG", javaClass.simpleName + " text changed " + binding.searchBar.getText())
                // send the entered text to our filter and let it manage everything
                if (customSuggestionAdapter != null) {
                    customSuggestionAdapter.getFilter().filter(binding.searchBar.getText())
                }
            }
            override fun afterTextChanged(editable: Editable) {}
        })
    }
    private fun promptSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            getString(R.string.speech_prompt)
        )
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
        } catch (a: ActivityNotFoundException) {
            Toast.makeText(
                requireActivity().applicationContext,
                getString(R.string.speech_not_supported),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CODE_SPEECH_INPUT -> {
                if (resultCode == AppCompatActivity.RESULT_OK && null != data) {
                    val result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    Toast.makeText(requireActivity(), result!![0], Toast.LENGTH_SHORT).show()
                    binding.searchBar.setPlaceHolder(result[0])
                    binding.searchBar.text = result[0]
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.searchBar.closeSearch()
        binding.searchBar.clearSuggestions()
        binding.searchBar.lastSuggestions = suggestions
    }
    private fun handleChipSelected() {
        for (i in 0 until binding.homeFragmentCGTopBook.childCount) {
            val view = binding.homeFragmentCGTopBook.getChildAt(i)
            if (view is Chip) {
                val chip = view as Chip
                if (chip.text == selectedChip) {
                    chip.isChecked = true
                }
            }
        }

        binding.homeFragmentCGTopBook.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.size > 0) {
                val chip: Chip? = group.findViewById(checkedIds[0])
                if (chip != null && chip.isChecked) {
                    Log.d("chip", chip.text.toString())
                    getTopBookByCategory(chip.text.toString())
                    selectedChip = chip.text.toString()
                }
            }
            else {
                Log.d("unchip", "unchip")
                getTopBookByCategory("")
            }

        }
    }

    private fun newArrivalInitalize() {
        val newArrivalsAdapter = activity?.let { HomeFragmentNewArrivaltemAdapter(it, BookNewArrivalList, BookNewArrivalSaleList) }
        binding.vpNewArrivalsHomeFragment.adapter = newArrivalsAdapter
        binding.vpNewArrivalsHomeFragment.pageMargin = 20

        if (BookNewArrivalList.size == 0 || BookNewArrivalSaleList.size == 0) {
            binding.smNewArrivalsHomeFragment.visibility = View.VISIBLE
            binding.vpNewArrivalsHomeFragment.visibility = View.GONE
            binding.smNewArrivalsHomeFragment.startShimmer()

            db.collection("books").whereEqualTo("is-new-arrival", true).get().addOnSuccessListener { result ->
                lifecycleScope.launch {
                    for (document in result) {
                        val tmp_id = document.data.get("bookID").toString()
                        var tmp = 0.0F

                        tmp = getBookPrice1(tmp_id)

                        BookNewArrivalList.add(
                            BookObject(
                                document.data.get("bookID").toString(),
                                document.data.get("name").toString(),
                                document.data.get("genre").toString(),
                                document.data.get("author").toString(),
                                document.data.get("releaseDate").toString(),
                                document.data.get("image").toString(),
                                tmp,
                                document.data.get("description").toString()
                            )
                        )

                        if (document.data.get("best-deal-sale") != null)
                            BookNewArrivalSaleList.add(
                                document.data.get("best-deal-sale").toString().toFloat()
                            )
                        else
                            BookNewArrivalSaleList.add(0F)
                    }

                    if (newArrivalsAdapter != null) {
                        newArrivalsAdapter.notifyDataSetChanged()

                        Handler().postDelayed({
                            binding.smNewArrivalsHomeFragment.visibility = View.GONE
                            binding.vpNewArrivalsHomeFragment.visibility = View.VISIBLE
                            binding.smNewArrivalsHomeFragment.stopShimmer()
                        }, 2000)
                    }
                }
            }
        }
        else {
            binding.smNewArrivalsHomeFragment.visibility = View.GONE
            binding.vpNewArrivalsHomeFragment.visibility = View.VISIBLE
            binding.smNewArrivalsHomeFragment.stopShimmer()
        }
    }

    private fun recommandationBookInitialize() {
        RCDBookAdapter = TopBookHomeFragmentAdapter(activity, RCDBookList, RCDBookRating)
        binding.rvTodayRCDHomeFragment.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.rvTodayRCDHomeFragment.adapter = RCDBookAdapter

        if (RCDBookList.size == 0 || RCDBookRating.size == 0) {
            binding.smHomeFragmentRCDBookRV.visibility = View.VISIBLE
            binding.rvTodayRCDHomeFragment.visibility = View.INVISIBLE
            binding.smHomeFragmentRCDBookRV.startShimmer()

            db.collection("books").get().addOnSuccessListener { result ->
                lifecycleScope.launch {
                    for (document in result) {
                        val tmp_id = document.data.get("bookID").toString()
                        var tmp = 0.0F

                        tmp = getBookPrice1(tmp_id)

                        RCDBookList.add(
                            BookObject(
                                document.data.get("bookID").toString(),
                                document.data.get("name").toString(),
                                document.data.get("genre").toString(),
                                document.data.get("author").toString(),
                                document.data.get("releaseDate").toString(),
                                document.data.get("image").toString(),
//                    document.data.get("price").toString().toFloat()
                                tmp,
                                document.data.get("description").toString()
                            )
                        )

                        var avg_rating = 0.0F;
                        var rating_num = 1;
                        if (document.data.get("review") != null) {
                            val reviewsArray =
                                document.data.get("review") as ArrayList<Map<String, Any>>
                            rating_num = reviewsArray.size

                            for (reviewMap in reviewsArray) {
                                val uid = reviewMap["UID"] as String
                                val image = reviewMap["image"] as String
                                val score = (reviewMap["score"] as Long).toInt()
                                val text = reviewMap["text"] as String

                                avg_rating += score
                            }
                        }

                        RCDBookRating.add(avg_rating / rating_num)
                    }

                    if (RCDBookAdapter != null) {
                        RCDBookAdapter.notifyDataSetChanged()

                        Handler().postDelayed({
                            binding.smHomeFragmentRCDBookRV.visibility = View.GONE
                            binding.rvTodayRCDHomeFragment.visibility = View.VISIBLE
                            binding.smHomeFragmentRCDBookRV.stopShimmer()
                        }, 2000)
                    }
                }
            }
        }
    }

    private fun recommandationTypeInitialize() {
        RCDAdapter = HomeFragmentTodayRCDTypeAdapter(bookCategory, this)
        binding.rvTodayRecommandationsType.adapter = RCDAdapter
        binding.rvTodayRecommandationsType.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        if (bookCategory.size == 0) {
            binding.smRCDHomeFragment.visibility = View.VISIBLE
            binding.rvTodayRecommandationsType.visibility = View.INVISIBLE
            binding.smRCDHomeFragment.startShimmer()

            db.collection("book-category").get().addOnSuccessListener { result ->
                val times = result.documents[0].data!!.get("categories") as? ArrayList<String>

                for (time in times!!)
                    bookCategory.add(time.toString())

                Handler().postDelayed({
                    RCDAdapter.notifyDataSetChanged()

                    binding.smRCDHomeFragment.visibility = View.INVISIBLE
                    binding.rvTodayRecommandationsType.visibility = View.VISIBLE
                    binding.smRCDHomeFragment.stopShimmer()
                }, 2000)
            }
        }
        else {
            binding.smRCDHomeFragment.visibility = View.INVISIBLE
            binding.rvTodayRecommandationsType.visibility = View.VISIBLE
            binding.smRCDHomeFragment.stopShimmer()
        }
    }

    private fun topBookRVInitialize() {
        binding.rvTopBookHomeFragment.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        topBookAdapter = TopBookHomeFragmentAdapter(activity, topBookArrayList, topBookRating)
        binding.rvTopBookHomeFragment.adapter = topBookAdapter

        if (topBookArrayList.size == 0 || topBookRating.size == 0) {
            binding.smHomeFragmentTopBookRV.visibility = View.VISIBLE
            binding.rvTopBookHomeFragment.visibility = View.INVISIBLE
            binding.smHomeFragmentTopBookRV.startShimmer()

            db.collection("books").get().addOnSuccessListener { result ->
                lifecycleScope.launch {

                    for (document in result) {
                        val tmp_id = document.data.get("bookID").toString()
                        var tmp = 0.0F

                        tmp = getBookPrice1(tmp_id)

                        topBookArrayList.add(
                            BookObject(
                                document.data.get("bookID").toString(),
                                document.data.get("name").toString(),
                                document.data.get("genre").toString(),
                                document.data.get("author").toString(),
                                document.data.get("releaseDate").toString(),
                                document.data.get("image").toString(),
//                    document.data.get("price").toString().toFloat()
                                tmp,
                                document.data.get("description").toString()
                            )
                        )

                        var avg_rating = 0.0F;
                        var rating_num = 1;
                        if (document.data.get("review") != null) {
                            val reviewsArray =
                                document.data.get("review") as ArrayList<Map<String, Any>>
                            rating_num = reviewsArray.size

                            for (reviewMap in reviewsArray) {
                                val uid = reviewMap["UID"] as String
                                val image = reviewMap["image"] as String
                                val score = (reviewMap["score"] as Long).toInt()
                                val text = reviewMap["text"] as String

                                avg_rating += score
                            }
                        }

                        topBookRating.add(avg_rating / rating_num)
                    }

                    topBookAdapter.notifyDataSetChanged()

                    Handler().postDelayed({
                        binding.smHomeFragmentTopBookRV.visibility = View.GONE
                        binding.rvTopBookHomeFragment.visibility = View.VISIBLE
                        binding.smHomeFragmentTopBookRV.stopShimmer()
                    }, 2000)
                }
            }
        }
    }

    private fun hcmusBookHomeRVInitialize() {
        binding.rvHcmusBook.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        hcmusBookHomeFragmentAdapter = HcmusBookHomeFragmentAdapter(activity, topBookArrayList)
        binding.rvHcmusBook.adapter = hcmusBookHomeFragmentAdapter

        if (topBookArrayList.size == 0 || topBookRating.size == 0) {
            binding.smHcmusBookRV.visibility = View.VISIBLE
            binding.rvHcmusBook.visibility = View.GONE
            binding.smHcmusBookRV.startShimmer()

            val chosenGenre = "Hcmus-book"
            db.collection("books").whereEqualTo("genre", chosenGenre).get().addOnSuccessListener { result ->
                lifecycleScope.launch {

                    for (document in result) {
                        val tmp_id = document.data.get("bookID").toString()
                        var tmp = 0.0F

                        tmp = getBookPrice1(tmp_id)

                        hcmusBookArrayList.add(
                            BookObject(
                                document.data.get("bookID").toString(),
                                document.data.get("name").toString(),
                                document.data.get("genre").toString(),
                                document.data.get("author").toString(),
                                document.data.get("releaseDate").toString(),
                                document.data.get("image").toString(),
//                    document.data.get("price").toString().toFloat()
                                tmp,
                                document.data.get("description").toString()
                            )
                        )
                    }

                    hcmusBookHomeFragmentAdapter.notifyDataSetChanged()

                    Handler().postDelayed({
                        binding.smHcmusBookRV.visibility = View.GONE
                        binding.rvHcmusBook.visibility = View.VISIBLE
                        binding.smHcmusBookRV.stopShimmer()
                    }, 2000)
                }
            }
        }
    }

    private fun topBookInitialize(inflater: LayoutInflater) {

        if (times.size == 0) {
            binding.smHomeFragmentTopBook.visibility = View.VISIBLE
            binding.horizontalScrollView.visibility = View.INVISIBLE
            binding.smHomeFragmentTopBook.startShimmer()

            db.collection("top-book").get().addOnSuccessListener { result ->
                times = (result.documents[0].data!!.get("time") as? ArrayList<String>)!!

                for (time in times!!) {

                    val chip = inflater.inflate(R.layout.home_fragment_chip_top_book, binding.homeFragmentCGTopBook, false) as Chip
                    chip.text = time.toString()

                    binding.homeFragmentCGTopBook.addView(chip)
                }

                Handler().postDelayed({
                    binding.smHomeFragmentTopBook.visibility = View.GONE
                    binding.horizontalScrollView.visibility = View.VISIBLE
                    binding.smHomeFragmentTopBook.stopShimmer()
                }, 2000)
            }
        }
        else {
            for (time in times!!) {

                val chip = inflater.inflate(R.layout.home_fragment_chip_top_book, binding.homeFragmentCGTopBook, false) as Chip
                chip.text = time.toString()

                binding.homeFragmentCGTopBook.addView(chip)
            }

            binding.smHomeFragmentTopBook.visibility = View.GONE
            binding.horizontalScrollView.visibility = View.VISIBLE
            binding.smHomeFragmentTopBook.stopShimmer()
        }
    }

    fun bestDealInitialize() {
        bestDealAdapter =
            activity?.let { HomeFragmentBestDealViewPagerAdapter(it, bestDeals, book_deal_sale) }!!
        binding.rvBestDeal.adapter = bestDealAdapter
        binding.rvBestDeal.pageMargin = 20
        binding.dotsIndicator.attachTo(binding.rvBestDeal)

        if (bestDeals.size == 0 || book_deal_sale.size == 0) {
            binding.smHomeFragmentBestDeal.visibility = View.VISIBLE
            binding.rvBestDeal.visibility = View.INVISIBLE
            binding.smHomeFragmentBestDeal.startShimmer()

            db.collection("books").whereNotEqualTo("best-deal-sale", null).get().addOnSuccessListener { result ->
                lifecycleScope.launch {
                    for (document in result) {
                        val tmp_id = document.data.get("bookID").toString()
                        var tmp = 0.0F

                        tmp = getBookPrice1(tmp_id)
                        bestDeals.add(
                            BookObject(
                                document.data.get("bookID").toString(),
                                document.data.get("name").toString(),
                                document.data.get("genre").toString(),
                                document.data.get("author").toString(),
                                document.data.get("releaseDate").toString(),
                                document.data.get("image").toString(),
//                    document.data.get("price").toString().toFloat()
                                tmp,
                                document.data.get("description").toString()
                            )
                        )

                        book_deal_sale.add(document.data.get("best-deal-sale").toString().toFloat())
                    }


                    if (bestDealAdapter != null) {
                        bestDealAdapter.notifyDataSetChanged()

                        Handler().postDelayed({
                            binding.smHomeFragmentBestDeal.visibility = View.GONE
                            binding.rvBestDeal.visibility = View.VISIBLE
                            binding.smHomeFragmentBestDeal.stopShimmer()
                        }, 2000)
                    }
                }
            }
        }
    }

    suspend fun getBookPrice1(bookID: String): Float {
        return try {
            val querySnapshot = db.collection("personalStores").whereNotEqualTo("items.$bookID.price", null).get().await()
            for (document in querySnapshot.documents) {
                val bookList = document.data?.get("items") as? Map<String, Any>
                val bookDetail = bookList?.get(bookID) as? Map<String, Any>

                val price = bookDetail?.get("price").toString()
                if (!price.isNullOrEmpty()) {
                    return price.toFloat()
                }
            }
            // Return a default value if the book price is not found
            0.0F
        } catch (e: Exception) {
            // Handle failures
            Log.e("Firestore", "Error getting book price", e)
            // Return a default value in case of failure
            0.0F
        }
    }

    fun getRCDBookCategories(kind: String) {
        binding.smHomeFragmentRCDBookRV.visibility = View.VISIBLE
        binding.rvTodayRCDHomeFragment.visibility = View.INVISIBLE
        binding.smHomeFragmentRCDBookRV.startShimmer()

        RCDBookList.clear()
        RCDBookRating.clear()

        db.collection("books").whereEqualTo("genre", kind).get().addOnSuccessListener { result ->
            for (document in result) {
                val tmp_id = document.data.get("bookID").toString()
                var tmp = 0.0F

                runBlocking {
                    tmp = getBookPrice1(tmp_id)
                }

                RCDBookList.add(
                    BookObject(document.data.get("id").toString(),
                    document.data.get("name").toString(),
                    document.data.get("genre").toString(),
                    document.data.get("author").toString(),
                    document.data.get("releaseDate").toString(),
                    document.data.get("image").toString(),
                    tmp,
                    document.data.get("description").toString()
//                    document.data.get("price").toString().toFloat()
                )
                )

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

                RCDBookRating.add(avg_rating / rating_num)
            }

            if (RCDBookAdapter != null) {
                RCDBookAdapter.notifyDataSetChanged()

                Handler().postDelayed({
                    binding.smHomeFragmentRCDBookRV.visibility = View.GONE
                    binding.rvTodayRCDHomeFragment.visibility = View.VISIBLE
                    binding.smHomeFragmentRCDBookRV.stopShimmer()
                }, 2000)
            }
        }
    }

    fun getTopBookByCategory(time: String) {
        binding.smHomeFragmentTopBookRV.visibility = View.VISIBLE
        binding.rvTopBookHomeFragment.visibility = View.INVISIBLE
        binding.smHomeFragmentTopBookRV.startShimmer()

        topBookArrayList.clear()
        topBookRating.clear()

        if (time == "") {
            db.collection("books").get().addOnSuccessListener { result ->
                for (document in result) {
                    val tmp_id = document.data.get("bookID").toString()
                    var tmp = 0.0F

                    runBlocking {
                        tmp = getBookPrice1(tmp_id)
                    }

                    topBookArrayList.add(
                        BookObject(document.data.get("id").toString(),
                        document.data.get("name").toString(),
                        document.data.get("genre").toString(),
                        document.data.get("author").toString(),
                        document.data.get("releaseDate").toString(),
                        document.data.get("image").toString(),
                        tmp,
                        document.data.get("description").toString()
//                        document.data.get("price").toString().toFloat()
                    )
                    )

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

                    topBookRating.add(avg_rating / rating_num)
                }

                if (topBookAdapter != null) {
                    topBookAdapter.notifyDataSetChanged()

                    Handler().postDelayed({
                        binding.smHomeFragmentTopBookRV.visibility = View.GONE
                        binding.rvTopBookHomeFragment.visibility = View.VISIBLE
                        binding.smHomeFragmentTopBookRV.stopShimmer()
                    }, 2000)
                }
            }
        }
        else {
            db.collection("books").whereEqualTo("top-book", time).get().addOnSuccessListener { result ->
                for (document in result) {
                    val tmp_id = document.data.get("bookID").toString()
                    var tmp = 0.0F

                    runBlocking {
                        tmp = getBookPrice1(tmp_id)
                    }

                    topBookArrayList.add(
                        BookObject(document.data.get("id").toString(),
                        document.data.get("name").toString(),
                        document.data.get("genre").toString(),
                        document.data.get("author").toString(),
                        document.data.get("releaseDate").toString(),
                        document.data.get("image").toString(),
                        tmp,
                        document.data.get("description").toString()
//                        document.data.get("price").toString().toFloat()
                    )
                    )

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

                    topBookRating.add(avg_rating / rating_num)
                }

                if (topBookAdapter != null) {
                    topBookAdapter.notifyDataSetChanged()

                    Handler().postDelayed({
                        binding.smHomeFragmentTopBookRV.visibility = View.GONE
                        binding.rvTopBookHomeFragment.visibility = View.VISIBLE
                        binding.smHomeFragmentTopBookRV.stopShimmer()
                    }, 2000)
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}