package com.example.booklette

import android.graphics.Color
import android.graphics.Paint
import android.opengl.Visibility
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booklette.databinding.FragmentBookDetailBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso
import com.taufiqrahman.reviewratings.BarLabels
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

    private lateinit var db: FirebaseFirestore
    private  var bookID: String = ""

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

                val salePercent = document.data.get("best-deal-sale").toString().toFloat()
                db.collection("personalStores").whereNotEqualTo("items.$bookID.price", null).get().addOnSuccessListener { result ->
                    var bookList: Map<String, Any>
                    var bookDetail : Map<String, Any>

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
//                            Log.d("vouchers", bookVoucher.toString())
//                    db.collection("discounts").whereEqualTo("discountID", bookDetail[""])
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

                    binding.rvBookDetailShopVoucher.visibility = View.VISIBLE
                    binding.smBookDetailShopVoucher.visibility = View.INVISIBLE
                    binding.smBookDetailShopVoucher.stopShimmer()

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

        shopVoucherAdapter = bookDetailShopVoucherRVAdapter()
        binding.rvBookDetailShopVoucher.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.rvBookDetailShopVoucher.adapter = shopVoucherAdapter

        var books = ArrayList<BookObject>()
        books.add(BookObject("B001", "The Catcher in the Eyes", "Novel", "Chanh Tin", "10/03/2003", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b", 12.50F))
        books.add(BookObject("B001", "The Catcher in the Eyes", "Novel", "Chanh Tin", "10/03/2003", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b", 12.50F))
        books.add(BookObject("B001", "The Catcher in the Eyes", "Novel", "Chanh Tin", "10/03/2003", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b", 12.50F))
        books.add(BookObject("B001", "The Catcher in the Eyes", "Novel", "Chanh Tin", "10/03/2003", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b", 12.50F))
        books.add(BookObject("B001", "The Catcher in the Eyes", "Novel", "Chanh Tin", "10/03/2003", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b", 12.50F))
        books.add(BookObject("B001", "The Catcher in the Eyes", "Novel", "Chanh Tin", "10/03/2003", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b", 12.50F))

        var stars = ArrayList<Float>()
        stars.add(4.5F)
        stars.add(4.5F)
        stars.add(4.5F)
        stars.add(4.5F)
        stars.add(4.5F)
        stars.add(4.5F)

        otherBookFromShopAdapter = TopBookHomeFragmentAdapter(activity, books, stars)
        binding.rvOtherBookFromShopBookDetail.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.rvOtherBookFromShopBookDetail.adapter = otherBookFromShopAdapter

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

        binding.rvBookDetailYouMayLove.layoutManager = GridLayoutManager(activity, 2)
        binding.rvBookDetailYouMayLove.adapter = bookDetailYouMayLoveAdapter()

        return view
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