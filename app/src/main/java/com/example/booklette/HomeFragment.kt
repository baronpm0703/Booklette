package com.example.booklette

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booklette.databinding.FragmentHomeBinding
import com.google.android.material.chip.Chip
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

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
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var topBookArrayList = ArrayList<BookObject>()
    private var topBookRating = ArrayList<Float>()

    lateinit var topBookAdapter: TopBookHomeFragmentAdapter

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
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        auth = Firebase.auth
        db = Firebase.firestore

        binding.homeFragmentCGTopBook.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.size > 0) {
                val chip: Chip? = group.findViewById(checkedIds[0])
                if (chip != null && chip.isChecked) {
                    Log.d("chip", chip.text.toString())
                    getTopBookByCategory(chip.text.toString())
                }
            }
            else {
                Log.d("unchip", "unchip")
                getTopBookByCategory("")
            }

        }

        binding.smHomeFragmentBestDeal.visibility = View.VISIBLE
        binding.rvBestDeal.visibility = View.INVISIBLE
        binding.smHomeFragmentBestDeal.startShimmer()

        binding.smHomeFragmentTopBook.visibility = View.VISIBLE
        binding.horizontalScrollView.visibility = View.INVISIBLE
        binding.smHomeFragmentTopBook.startShimmer()

        binding.smHomeFragmentTopBookRV.visibility = View.VISIBLE
        binding.rvTopBookHomeFragment.visibility = View.INVISIBLE
        binding.smHomeFragmentTopBookRV.startShimmer()

        if (auth.currentUser != null) {
            binding.txtWelcomeBack.text = "Welcome back, " + auth.currentUser!!.email.toString()
        }

        binding.btnSignOut.setOnClickListener({
            auth.signOut()
            startActivity(Intent(activity, LoginActivity::class.java))
        })

        var bestDeals = ArrayList<BookObject>();
        var book_deal_sale = ArrayList<Float>();
        val bestDealAdapter = activity?.let { HomeFragmentBestDealViewPagerAdapter(it, bestDeals, book_deal_sale) }
        binding.rvBestDeal.adapter = bestDealAdapter
        binding.rvBestDeal.pageMargin = 20
        binding.dotsIndicator.attachTo(binding.rvBestDeal)

        db.collection("books").whereNotEqualTo("best-deal-sale", null).get().addOnSuccessListener { result ->
            for (document in result) {
//                Log.d("firestore", "${document.id} => ${document.data.get("name")}")
                bestDeals.add(BookObject(document.data.get("id").toString(),
                    document.data.get("name").toString(),
                    document.data.get("genre").toString(),
                    document.data.get("author").toString(),
                    document.data.get("releaseDate").toString(),
                    document.data.get("image").toString(),
                    document.data.get("price").toString().toFloat()))

                book_deal_sale.add(document.data.get("best-deal-sale").toString().toFloat())
            }

            if (bestDealAdapter != null) {
                bestDealAdapter.notifyDataSetChanged()

                Handler().postDelayed({
                    // Code to be executed after the delay
                    // For example, you can start a new activity or update UI elements
                    binding.smHomeFragmentBestDeal.visibility = View.GONE
                    binding.rvBestDeal.visibility = View.VISIBLE
                    binding.smHomeFragmentBestDeal.stopShimmer()
                }, 2000)
            }
        }

        db.collection("top-book").get().addOnSuccessListener { result ->
//            Log.d("firestore", "${result.documents[0].data!!.get("time")}")
            val times = result.documents[0].data!!.get("time") as? ArrayList<String>

            for (time in times!!) {
//                Log.d("firestore", "${time}")

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

        /*
        ------
         */

        var temporary = ArrayList<String>()
        temporary.add("a")
        temporary.add("a")
        temporary.add("a")
        temporary.add("a")
        temporary.add("a")

        val newArrivalsAdapter = activity?.let { HomeFragmentNewArrivaltemAdapter(it, temporary) }
        binding.vpNewArrivalsHomeFragment.adapter = newArrivalsAdapter
        binding.vpNewArrivalsHomeFragment.pageMargin = 20

        topBookArrayList = ArrayList<BookObject>()
        topBookRating = ArrayList<Float>()
        binding.rvTopBookHomeFragment.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        topBookAdapter = TopBookHomeFragmentAdapter(topBookArrayList, topBookRating)
        binding.rvTopBookHomeFragment.adapter = topBookAdapter

        db.collection("books").get().addOnSuccessListener { result ->
            for (document in result) {
                topBookArrayList.add(BookObject(document.data.get("id").toString(),
                    document.data.get("name").toString(),
                    document.data.get("genre").toString(),
                    document.data.get("author").toString(),
                    document.data.get("releaseDate").toString(),
                    document.data.get("image").toString(),
                    document.data.get("price").toString().toFloat()))

//                val temp = document.data.get("review")
//                Log.d("abced", temp.toString())

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

        binding.rvTodayRCDHomeFragment.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.rvTodayRCDHomeFragment.adapter = TopBookHomeFragmentAdapter(topBookArrayList, topBookRating)

        var bookCategory = ArrayList<String>();
        bookCategory.add("Novel")
        bookCategory.add("Self-love")
        bookCategory.add("Science")
        bookCategory.add("Romantic")
        bookCategory.add("Lover")
        bookCategory.add("1989")
        bookCategory.add("midnights")


        binding.rvTodayRecommandationsType.adapter = HomeFragmentTodayRCDTypeAdapter(bookCategory)
        binding.rvTodayRecommandationsType.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        return view
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
                    topBookArrayList.add(BookObject(document.data.get("id").toString(),
                        document.data.get("name").toString(),
                        document.data.get("genre").toString(),
                        document.data.get("author").toString(),
                        document.data.get("releaseDate").toString(),
                        document.data.get("image").toString(),
                        document.data.get("price").toString().toFloat()))

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
                    topBookArrayList.add(BookObject(document.data.get("id").toString(),
                        document.data.get("name").toString(),
                        document.data.get("genre").toString(),
                        document.data.get("author").toString(),
                        document.data.get("releaseDate").toString(),
                        document.data.get("image").toString(),
                        document.data.get("price").toString().toFloat()))

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