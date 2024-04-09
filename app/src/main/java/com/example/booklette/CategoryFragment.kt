package com.example.booklette

import CategoryFragmentGridViewAdapter
import CustomSuggestionAdapter
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
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

    private var fuzzySearchArraySample = ArrayList<Map<String, String>>()

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
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        val view = binding.root

        // Authen and get data
        auth = Firebase.auth
        db = Firebase.firestore

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

        binding.gvCategories.adapter =
            activity?.let { CategoryFragmentGridViewAdapter(it, categories) }

        binding.gvCategories.setOnItemClickListener { parent, view, position, id ->
            val genre = categories[position]
            val productList = ProductList()
            val args = Bundle()
            args.putString("Genre", genre)
            productList.arguments = args

            val ft = activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fcvNavigation, productList)
                ?.commit()
        }


        val inflater = LayoutInflater.from(activity)
        var customSuggestionAdapter = activity?.let {
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
        binding.searchBar.setOnSearchActionListener(object: MaterialSearchBar.OnSearchActionListener{
            override fun onSearchStateChanged(enabled: Boolean) {
                Log.i("State", enabled.toString())

                if (enabled) {
                    binding.relativeLayout.visibility = View.GONE
                    binding.filterCategorySearchPage.visibility = View.GONE
                    binding.TopSearchLayout.visibility = View.VISIBLE
                    binding.searchBar.setPadding(0, binding.relativeLayout.height / 2, 0,  0)

                } else {
                    binding.searchBar.setPadding(0)
                    binding.relativeLayout.visibility = View.VISIBLE
                    binding.filterCategorySearchPage.visibility = View.VISIBLE
                    binding.TopSearchLayout.visibility = View.GONE
                }
            }

            override fun onSearchConfirmed(text: CharSequence?) {
                Log.i("Confirm", text.toString())
                val wordToSearch = text.toString()

                val sfs = SimpleFuzzySearch(fuzzySearchArraySample, arrayListOf("bookName", "genre", "author"))
                // Line break and debug for more result
                val resultFromFuzzySearch = sfs.search(wordToSearch) as ArrayList

                val productList = ProductList()
                val args = Bundle()

                if (resultFromFuzzySearch[0][1] == "genre") { // If first res relate to genre
                    val obj = resultFromFuzzySearch[0][0] as Map<String, String>
                    args.putString("Genre", obj["genre"])
                } else {
                    val listOfBookName = ArrayList<String>()
                    resultFromFuzzySearch.forEach { item ->
                        val obj = item[0] as Map<String, String>
                        val bookName = obj["bookName"].toString()

                        listOfBookName.add(bookName)
                    }
                    args.putString("WordToSearch", wordToSearch)
                    args.putStringArrayList("SearchResult", listOfBookName)
                }

                productList.arguments = args

                activity?.supportFragmentManager?.beginTransaction()
                    ?.replace(R.id.fcvNavigation, productList)
                    ?.commit()
            }

            override fun onButtonClicked(buttonCode: Int) {
                Log.i("Click","Search Click")
            }
        })

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
        _binding = null
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