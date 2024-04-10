package com.example.booklette

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booklette.databinding.FragmentBookDetailBinding
import com.example.booklette.model.BookObject
import com.example.booklette.model.VoucherObject
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso
import com.taufiqrahman.reviewratings.BarLabels
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Random
import kotlin.math.round


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BookDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BookDetailFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var voucherAdapter: bookDetailFragmentVoucherViewPagerAdapter
    private lateinit var shopVoucherAdapter: bookDetailShopVoucherRVAdapter
    private lateinit var otherBookFromShopAdapter: TopBookHomeFragmentAdapter
    private lateinit var bookDetailYouMayLoveAdapter: bookDetailYouMayLoveAdapter

    private lateinit var db: FirebaseFirestore
    private var bookID: String = ""

    private var voucherList = ArrayList<VoucherObject>()
    private var otherBookFromStore = ArrayList<BookObject>()
    private var otherBookFromStoreRating = ArrayList<Float>()
    private var otherBookFromOtherStore = ArrayList<BookObject>()
    private var otherBookFromOtherStoreRating = ArrayList<Float>()

    private lateinit var bookList: Map<String, Any>
    private lateinit var bookDetail: Map<String, Any>

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
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        db = Firebase.firestore
        bookID = arguments?.getString("bookID").toString()

        binding.txtBookCategory.visibility = View.INVISIBLE
        binding.smCategoryBook.visibility = View.VISIBLE
        binding.smCategoryBook.startShimmer()

        binding.txtBookName.visibility = View.INVISIBLE
        binding.smBookName.visibility = View.VISIBLE
        binding.smBookName.startShimmer()

        binding.txtAuthorName.visibility = View.INVISIBLE
        binding.smAuthorName.visibility = View.VISIBLE
        binding.smAuthorName.startShimmer()

        binding.txtBookOriginalPrice.visibility = View.INVISIBLE
        binding.smBookOriginalPrice.visibility = View.VISIBLE
        binding.smBookOriginalPrice.startShimmer()

        binding.txtBookRealPrice.visibility = View.INVISIBLE
        binding.smBookRealPrice.visibility = View.VISIBLE
        binding.smBookRealPrice.startShimmer()

        binding.rlSalePercent.visibility = View.INVISIBLE
        binding.smSalePercent.visibility = View.VISIBLE
        binding.smSalePercent.startShimmer()

        binding.llRatingStarBook.visibility = View.GONE
        binding.smRatingStarBook.visibility = View.VISIBLE
        binding.smRatingStarBook.startShimmer()

        binding.txtVoucher.visibility = View.INVISIBLE
        binding.smVoucherSection.visibility = View.VISIBLE
        binding.smVoucherSection.startShimmer()

        binding.vpVoucherBookDetail.visibility = View.INVISIBLE
        binding.smVoucherBookDetail.visibility = View.VISIBLE
        binding.smVoucherBookDetail.startShimmer()

        binding.llBookStoreInfo.visibility = View.INVISIBLE
        binding.smBookStoreInfo.visibility = View.VISIBLE
        binding.smBookStoreInfo.startShimmer()

        binding.txtOtherBookBookDetail.visibility = View.INVISIBLE
        binding.smTxtOtherBookBookDetail.visibility = View.VISIBLE
        binding.smTxtOtherBookBookDetail.startShimmer()

        binding.rvBookDetailShopVoucher.visibility = View.INVISIBLE
        binding.smBookDetailShopVoucher.visibility = View.VISIBLE
        binding.smBookDetailShopVoucher.startShimmer()

        binding.clBookDetailMoreDetailPart.visibility = View.GONE

        db.collection("books").whereEqualTo("bookID", bookID).get().addOnSuccessListener { result ->
            for (document in result) {
                Picasso.get()
                    .load(document.data.get("image").toString())
                    .into(binding.ivBookImage)

                binding.txtBookCategory.text = document.data.get("genre").toString()
                binding.txtBookName.text = document.data.get("name").toString()
                binding.txtAuthorName.text = document.data.get("author").toString()
                binding.txtDescriptionBookDetail.text = document.data.get("description").toString()

                val salePercent = document.data.get("best-deal-sale").toString().toFloat()
                db.collection("personalStores").whereNotEqualTo("items.$bookID.price", null).get().addOnSuccessListener { result ->
                    if (result.documents.size == 0) {
                        binding.txtBookOriginalPrice.visibility = View.GONE
                        binding.txtSalePercent.visibility = View.GONE
                        binding.txtBookRealPrice.text = "0 VND"

                        Handler().postDelayed({
                            binding.smBookOriginalPrice.visibility = View.INVISIBLE
                            binding.smBookOriginalPrice.stopShimmer()

                            binding.txtBookRealPrice.visibility = View.VISIBLE
                            binding.smBookRealPrice.visibility = View.INVISIBLE
                            binding.smBookRealPrice.stopShimmer()

                            binding.smSalePercent.visibility = View.INVISIBLE
                            binding.smSalePercent.stopShimmer()
                        }, 3000)
                    }
                    else {
                        val dcm = result.documents[0]

                        bookList = (dcm.data?.get("items") as? Map<String, Any>)!!
                        bookDetail = (bookList?.get(bookID) as? Map<String, Any>)!!
                        val price = bookDetail?.get("price").toString().toFloat()

                        if (salePercent == null || salePercent == 0.0F) {
                            binding.txtBookOriginalPrice.visibility = View.GONE
                            binding.txtSalePercent.visibility = View.GONE
                            binding.txtBookRealPrice.text = price.toString() + " VND"

                            Handler().postDelayed({
                                binding.smBookOriginalPrice.visibility = View.INVISIBLE
                                binding.smBookOriginalPrice.stopShimmer()

                                binding.txtBookRealPrice.visibility = View.VISIBLE
                                binding.smBookRealPrice.visibility = View.INVISIBLE
                                binding.smBookRealPrice.stopShimmer()

                                binding.smSalePercent.visibility = View.INVISIBLE
                                binding.smSalePercent.stopShimmer()
                            }, 3000)
                        }
                        else {
                            binding.txtBookOriginalPrice.apply {
                                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                                text = price.toString() + " VND"
                            }
                            binding.txtBookRealPrice.text =
                                (price - price * salePercent).toString() + " VND" // MUST DO LATER: need to get the price when passing data from another fragment to this one
                            binding.txtSalePercent.text =
                                (salePercent * 100).toString() + "% OFF " // MUST DO LATER: need to get the price when passing data from another fragment to this one

                            Handler().postDelayed({
                                binding.txtBookOriginalPrice.visibility = View.VISIBLE
                                binding.smBookOriginalPrice.visibility = View.INVISIBLE
                                binding.smBookOriginalPrice.stopShimmer()

                                binding.txtBookRealPrice.visibility = View.VISIBLE
                                binding.smBookRealPrice.visibility = View.INVISIBLE
                                binding.smBookRealPrice.stopShimmer()

                                binding.rlSalePercent.visibility = View.VISIBLE
                                binding.smSalePercent.visibility = View.INVISIBLE
                                binding.smSalePercent.stopShimmer()
                            }, 3000)

                            var bookVoucher = bookDetail["discounts"] as ArrayList<String>
                            for (vch in bookVoucher) {
                                db.collection("discounts").whereEqualTo("discountID", vch).get().addOnSuccessListener { result ->
                                    if (result.documents.size == 0) {
                                        binding.rvBookDetailShopVoucher.visibility = View.GONE
                                        binding.smBookDetailShopVoucher.visibility = View.INVISIBLE
                                        binding.smBookDetailShopVoucher.stopShimmer()
                                    }
                                    else {
                                        voucherList.add(
                                            VoucherObject(
                                                result.documents[0].data?.get("discountFilter").toString(),
                                                result.documents[0].data?.get("discountID").toString(),
                                                result.documents[0].data?.get("discountName").toString(),
                                                result.documents[0].data?.get("discountType").toString().toInt(),
                                                result.documents[0].data?.get("endDate") as Timestamp,
                                                result.documents[0].data?.get("percent").toString().toFloat(),
                                                result.documents[0].data?.get("startDate") as Timestamp
                                            )
                                        )

                                        shopVoucherAdapter.notifyDataSetChanged()

                                        Handler().postDelayed({
                                            binding.rvBookDetailShopVoucher.visibility = View.VISIBLE
                                            binding.smBookDetailShopVoucher.visibility = View.INVISIBLE
                                            binding.smBookDetailShopVoucher.stopShimmer()
                                        }, 3000)
                                    }
                                }
                            }
                        }
                    }

                    binding.txtBookStoreName.text = result.documents[0].data?.get("storeName").toString()
                    binding.txtStoreLocation.text = result.documents[0].data?.get("storeLocation").toString()
                    Picasso.get()
                        .load(result.documents[0].data?.get("storeAvatar").toString())
                        .into(binding.ivStoreAvatar)

                    Handler().postDelayed({
                        binding.llBookStoreInfo.visibility = View.VISIBLE
                        binding.smBookStoreInfo.visibility = View.INVISIBLE
                        binding.smBookStoreInfo.stopShimmer()
                    }, 3000)
                }

                val reviewList = document.data.get("review") as ArrayList<Map<Any, Any>>

                var avgRating = 0.0F
                for (review in reviewList) avgRating += (review["score"]).toString().toFloat()
                avgRating /= reviewList.size
                avgRating = round(avgRating * 10) / 10

                binding.ratingStarBar.rating = avgRating
                binding.txtAVGrating.text = avgRating.toString()

                binding.txtRatingBookDetail.text = avgRating.toString()
                binding.txtNumberOfReview.text = reviewList.size.toString() + " review(s)"

                Handler().postDelayed({
                    binding.txtBookCategory.visibility = View.VISIBLE
                    binding.smCategoryBook.visibility = View.INVISIBLE
                    binding.smCategoryBook.stopShimmer()

                    binding.txtBookName.visibility = View.VISIBLE
                    binding.smBookName.visibility = View.INVISIBLE
                    binding.smBookName.startShimmer()

                    binding.txtAuthorName.visibility = View.VISIBLE
                    binding.smAuthorName.visibility = View.INVISIBLE
                    binding.smAuthorName.stopShimmer()

                    binding.llRatingStarBook.visibility = View.VISIBLE
                    binding.smRatingStarBook.visibility = View.GONE
                    binding.smRatingStarBook.stopShimmer()

                    binding.txtVoucher.visibility = View.GONE
                    binding.smVoucherSection.visibility = View.INVISIBLE
                    binding.smVoucherSection.stopShimmer()

                    binding.vpVoucherBookDetail.visibility = View.GONE
                    binding.smVoucherBookDetail.visibility = View.INVISIBLE
                    binding.smVoucherBookDetail.stopShimmer()

                    binding.txtOtherBookBookDetail.visibility = View.VISIBLE
                    binding.smTxtOtherBookBookDetail.visibility = View.INVISIBLE
                    binding.smTxtOtherBookBookDetail.stopShimmer()

                    binding.bookDetailDotLoading.visibility = View.GONE
                    binding.clBookDetailMoreDetailPart.visibility = View.VISIBLE
                }, 3000)
            }
        }

        var data = ArrayList<String>()
        data.add("A")
        data.add("A")
        data.add("A")
        data.add("A")
        data.add("A")

        voucherAdapter = bookDetailFragmentVoucherViewPagerAdapter(requireContext(), data)
        binding.vpVoucherBookDetail.adapter = voucherAdapter
        binding.vpVoucherBookDetail.pageMargin = 20

        shopVoucherAdapter = bookDetailShopVoucherRVAdapter(context, voucherList)
        binding.rvBookDetailShopVoucher.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.rvBookDetailShopVoucher.adapter = shopVoucherAdapter

        otherBookFromShopAdapter = TopBookHomeFragmentAdapter(activity, otherBookFromStore, otherBookFromStoreRating)
        binding.rvOtherBookFromShopBookDetail.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.rvOtherBookFromShopBookDetail.adapter = otherBookFromShopAdapter
        getOtherBook()

        binding.txtBookDetailDescriptionMore.setOnClickListener({
            if (binding.txtBookDetailDescriptionMore.text == getString(R.string.bookDetailMore)) {
                binding.txtDescriptionBookDetail.maxLines = Int.MAX_VALUE
                binding.txtBookDetailDescriptionMore.text = getString(R.string.bookDetailLess)
            }
            else {
                binding.txtDescriptionBookDetail.maxLines = 10
                binding.txtBookDetailDescriptionMore.text = getString(R.string.bookDetailMore)
            }
        })

        val colors = intArrayOf(
            Color.parseColor("#0e9d58"),
            Color.parseColor("#bfd047"),
            Color.parseColor("#ffc105"),
            Color.parseColor("#ef7e14"),
            Color.parseColor("#d36259")
        )

        val raters = intArrayOf(
            Random().nextInt(100),
            Random().nextInt(100),
            Random().nextInt(100),
            Random().nextInt(100),
            Random().nextInt(100)
        )

        binding.ratingReviews.createRatingBars(100, BarLabels.STYPE5, colors, raters)

        binding.lvUserReviewDetail.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.lvUserReviewDetail.adapter = bookDetailUserReviewAdapter()

        getOtherBookFromAllStore()
        bookDetailYouMayLoveAdapter = bookDetailYouMayLoveAdapter(context, otherBookFromOtherStore, otherBookFromOtherStoreRating)
        binding.rvBookDetailYouMayLove.layoutManager = GridLayoutManager(activity, 2)
        binding.rvBookDetailYouMayLove.adapter = bookDetailYouMayLoveAdapter

        return view
    }

    fun getOtherBookFromAllStore() {
            db.collection("books").whereNotEqualTo("bookID", bookID).limit(kotlin.random.Random.nextLong(4, 8 + 1)).get().addOnSuccessListener { result ->
                lifecycleScope.launch {
                    for (document in result) {
                        val tmp_id = document.data.get("bookID").toString()
                        var tmp = 0.0F

                        tmp = getBookPrice1(tmp_id)
                        otherBookFromOtherStore.add(
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

                        otherBookFromOtherStoreRating.add(avg_rating / rating_num)
                    }

                    if (bookDetailYouMayLoveAdapter != null) {
                        val indices = otherBookFromOtherStore.indices.shuffled()

                        val shuffledList1 = ArrayList<BookObject>()
                        val shuffledList2 = ArrayList<Float>()
                        for (index in indices) {
                            shuffledList1.add(otherBookFromOtherStore[index])
                            shuffledList2.add(otherBookFromOtherStoreRating[index])
                        }

                        otherBookFromOtherStore.clear()
                        otherBookFromOtherStore.addAll(shuffledList1)

                        otherBookFromOtherStoreRating.clear()
                        otherBookFromOtherStoreRating.addAll(shuffledList2)

                        bookDetailYouMayLoveAdapter.notifyDataSetChanged()
                    }
                }
            }
    }

    fun getOtherBook() {
        db.collection("personalStores").whereNotEqualTo("items.$bookID.price", null).get().addOnSuccessListener {result ->
            for (document in result) {
                var storeBookList = (document.data?.get("items") as? Map<String, Any>)!!
                var storeBookDetail = (storeBookList?.get(bookID) as? Map<String, Any>)!!

                for ((key, value) in storeBookList) {
                    if (key != bookID) {
                        db.collection("books").whereEqualTo("bookID", key).get().addOnSuccessListener { result ->
                            lifecycleScope.launch {
                                for (document in result) {
                                    val tmp_id = document.data.get("bookID").toString()
                                    var tmp = 0.0F

                                    tmp = getBookPrice1(tmp_id)
                                    otherBookFromStore.add(
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
                                }

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

                                otherBookFromStoreRating.add(avg_rating / rating_num)


                                if (otherBookFromShopAdapter != null) {
                                    otherBookFromShopAdapter.notifyDataSetChanged()
                                }
                            }
                        }
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BookDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BookDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}