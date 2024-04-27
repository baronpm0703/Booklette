package com.example.booklette

import CategoryFragmentGridViewAdapter
import CustomSuggestionAdapter
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.example.booklette.databinding.FragmentCategoryBinding
import com.example.booklette.model.SimpleFuzzySearch
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.mancj.materialsearchbar.MaterialSearchBar
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter
import com.maxkeppeler.sheets.core.SheetStyle
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.Locale


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CategoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CategoryFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    private var suggestions = ArrayList<String>()

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private val REQ_CODE_SPEECH_INPUT = 100

    private var fuzzySearchArraySample = ArrayList<Map<String, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        super.onResume()
        binding.searchBar.closeSearch()
        binding.searchBar.clearSuggestions()
        binding.searchBar.lastSuggestions = suggestions
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        val view = binding.root

        // Authen and get data
        auth = Firebase.auth
        db = Firebase.firestore

        binding.ivHCMUS.setOnClickListener{
            val genre = "Hcmus-book"
            val productListHCMUS = ProductListHCMUS()
            val args = Bundle()
            args.putString("Genre", genre)
            productListHCMUS.arguments = args

            // Have to cast homePage to "activity as HomePage", otherwise the supportFragment can recognize the host
            val homeAct = (activity as homeActivity)
            homeAct.changeFragmentContainer(productListHCMUS, homeAct.smoothBottomBarStack[homeAct.smoothBottomBarStack.size - 1])
        }
        // Init fixed categories
        val categories = ArrayList<String>()
        categories.add("Non-fiction")
        categories.add("Classics")
        categories.add("Fantasy")
        categories.add("Young adult")
        categories.add("Crime")
        categories.add("Horror")
        categories.add("Sci-fi")
        categories.add("Drama")
        categories.add("Novel")
        categories.add("Self-Help")

        // Pass those categories to CustomAdapter then re-assign to layout GV adapter
        binding.gvCategories.adapter =
            activity?.let { CategoryFragmentGridViewAdapter(it, categories) }

        // On every item selected, get the item's genre and call the changeFragment from home Page
        // In order to allow onBack/Gesture navigation work with been conflicted/fragment layout stack
        binding.gvCategories.setOnItemClickListener { parent, view, position, id ->
            val genre = categories[position]
            val productList = ProductList()
            val args = Bundle()
            args.putString("Genre", genre)
            productList.arguments = args

            // Have to cast homePage to "activity as HomePage", otherwise the supportFragment can recognize the host
            val homeAct = (activity as homeActivity)
            homeAct.changeFragmentContainer(productList, homeAct.smoothBottomBarStack[homeAct.smoothBottomBarStack.size - 1])
        }

        val customSuggestionAdapter = activity?.let {
            CustomSuggestionAdapter(it, inflater, binding.searchBar)
        }

        if (suggestions.isEmpty()) {
            runBlocking {
                suggestions = getBookNamesAlongSetupDataArraySample()
            }
        }

//        val suggestions = listOf("Apple", "Banana", "Orange", "Mango", "Potato", "Melon", "Dragon Fruit", "Pineapple", "Chilly")
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

                if (enabled) {
                    binding.relativeLayout.visibility = View.GONE
//                    binding.filterCategorySearchPage.visibility = View.GONE
                    binding.TopSearchLayout.visibility = View.VISIBLE
                    binding.searchBar.setPadding(0, binding.relativeLayout.height / 2, 0,  0)

                } else {
                    binding.searchBar.setPadding(0)
                    binding.relativeLayout.visibility = View.VISIBLE
//                    binding.filterCategorySearchPage.visibility = View.VISIBLE
                    binding.TopSearchLayout.visibility = View.GONE
                }
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
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
            }
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                Log.d("LOG_TAG", javaClass.simpleName + " text changed " + binding.searchBar.getText())
                // send the entered text to our filter and let it manage everything
                if (customSuggestionAdapter != null) {
                    customSuggestionAdapter.getFilter().filter(binding.searchBar.getText())
                }
            }
            override fun afterTextChanged(editable: Editable) {}
        })

        val filterDialogSearch = FilterDialogSearch()
        binding.filterCategorySearchPage.setOnClickListener{
            activity?.let {
                filterDialogSearch.show(it){
                    style(SheetStyle.BOTTOM_SHEET)
                    title("Filter")
                    titleColor(Color.parseColor("#FF0000"))
                }
            }
        }

        val topSearch = arrayListOf("Hot", "Non-Fiction", "Fiction", "Horror", "LifeStyle Book", "Report Book", "Material", "Children Book", "Over 18s", "Mysterious")
        binding.topSearchGV.adapter = FilterTypeGVAdapter(requireActivity(), topSearch)

        binding.topSearchGV.setOnItemClickListener { parent, view, position, id ->
            Log.i("Top Search", topSearch[position])
        }

        return view
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
         * @return A new instance of fragment CategoryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CategoryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}