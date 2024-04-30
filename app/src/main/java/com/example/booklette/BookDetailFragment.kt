package com.example.booklette

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booklette.databinding.FragmentBookDetailBinding
import com.example.booklette.model.BookObject
import com.example.booklette.model.Photo
import com.example.booklette.model.UserReviewObject
import com.example.booklette.model.VoucherObject
import com.github.razir.progressbutton.attachTextChangeAnimator
import com.github.razir.progressbutton.bindProgressButton
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage

import com.maxkeppeler.sheets.core.SheetStyle
import com.squareup.picasso.Picasso
import com.taufiqrahman.reviewratings.BarLabels
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Collections
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
    private lateinit var bookDetailUserReviewAdapter: bookDetailUserReviewAdapter

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private var bookID: String = ""

    private var voucherList = ArrayList<VoucherObject>()
    private var otherBookFromStore = ArrayList<BookObject>()
    private var otherBookFromStoreRating = ArrayList<Float>()
    private var otherBookFromOtherStore = ArrayList<BookObject>()
    private var otherBookFromOtherStoreRating = ArrayList<Float>()

    private lateinit var reviewDialogBookDetail: ReviewDialogBookDetail
    private var userReviewList = ArrayList<UserReviewObject>()
    private var userInfo: UserReviewObject? = null
    private var oldRating: Float = 0.0F
    private var isFollowed: MutableLiveData<Boolean> = MutableLiveData(false)


    private lateinit var bookList: Map<String, Any>
    private lateinit var bookDetail: Map<String, Any>

    private var avgRating: Float = 0.0F
    private var reviewListSize: Int = 0
    private var starCount = arrayListOf(0, 0, 0, 0, 0)


    val colors = intArrayOf(
        Color.parseColor("#0e9d58"),
        Color.parseColor("#bfd047"),
        Color.parseColor("#ffc105"),
        Color.parseColor("#ef7e14"),
        Color.parseColor("#d36259")
    )

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
    ): View? {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        db = Firebase.firestore
        auth = Firebase.auth
        storage = Firebase.storage("gs://book-store-3ed32.appspot.com")

        bookID = arguments?.getString("bookID").toString()
        // Save user Info
        lifecycleScope.launch {
            userInfo = auth.uid?.let { getInfoCurrentUser(it) }
            val initValues = InitFilterValuesReviewBookDetail()
            if (userInfo != null){
                initValues.rating = userInfo!!.ratings
                initValues.text = userInfo!!.description
                initValues.reviewPhotos = userInfo!!.reviewPhotos
                val index = userInfo!!.wishListObject.indexOf(bookID)
                if (index != -1)
                    isFollowed.value = true
                else isFollowed.value = false
            }

            reviewDialogBookDetail = ReviewDialogBookDetail(initValues)
        }

        controlNumberCounter()
        controlBottomMenu()
        addToCart()

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

        binding.clBookDetailMoreDetailPart.visibility = View.INVISIBLE

        db.collection("books").whereEqualTo("bookID", bookID).get().addOnSuccessListener { result ->
            for (document in result) {
                Picasso.get()
                    .load(document.data.get("image").toString())
                    .into(binding.ivBookImage)

                binding.txtBookCategory.text = document.data.get("genre").toString()
                binding.txtBookName.text = document.data.get("name").toString()
                binding.txtAuthorName.text = document.data.get("author").toString()
                binding.txtDescriptionBookDetail.text = document.data.get("description").toString()

                var salePercent = 0.0F
                if (document.data.get("best-deal-sale") != null)
                    salePercent = document.data.get("best-deal-sale").toString().toFloat()

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

                            binding.rvBookDetailShopVoucher.visibility = View.VISIBLE
                            binding.smBookDetailShopVoucher.visibility = View.INVISIBLE
                            binding.smBookDetailShopVoucher.stopShimmer()
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
                        }

                        var bookVoucher = bookDetail["discount"] as String
                        if (bookVoucher == "") {
                            Handler().postDelayed({
                                binding.rvBookDetailShopVoucher.visibility = View.VISIBLE
                                binding.smBookDetailShopVoucher.visibility = View.INVISIBLE
                                binding.smBookDetailShopVoucher.stopShimmer()
                            }, 3000)
                        }
                        else {
                            db.collection("discounts").whereEqualTo("discountID", bookVoucher).get().addOnSuccessListener { result ->
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
                                            result.documents[0].data?.get("discountType").toString(),
                                            result.documents[0].data?.get("endDate") as Timestamp,
                                            result.documents[0].data?.get("percent").toString().toFloat(),
                                            result.documents[0].data?.get("startDate") as Timestamp
                                        )
                                    )

                                    shopVoucherAdapter.notifyDataSetChanged()
                                }

                                Handler().postDelayed({
                                    binding.rvBookDetailShopVoucher.visibility = View.VISIBLE
                                    binding.smBookDetailShopVoucher.visibility = View.INVISIBLE
                                    binding.smBookDetailShopVoucher.stopShimmer()
                                }, 3000)
                            }

                            Handler().postDelayed({
                                binding.rvBookDetailShopVoucher.visibility = View.VISIBLE
                                binding.smBookDetailShopVoucher.visibility = View.INVISIBLE
                                binding.smBookDetailShopVoucher.stopShimmer()
                            }, 3000)
                        }
                    }

                    if (result.documents.size == 0) {
                        binding.txtBookStoreName.text = "NULL"
                        binding.txtStoreLocation.text = "NULL"
                    }
                    else {
                        binding.txtBookStoreName.text = result.documents[0].data?.get("storeName").toString()
                        binding.txtStoreLocation.text = result.documents[0].data?.get("storeLocation").toString()

                        Picasso.get()
                            .load(result.documents[0].data?.get("storeAvatar").toString())
                            .into(binding.ivStoreAvatar)
                    }

                    Handler().postDelayed({
                        binding.llBookStoreInfo.visibility = View.VISIBLE
                        binding.smBookStoreInfo.visibility = View.INVISIBLE
                        binding.smBookStoreInfo.stopShimmer()
                    }, 3000)
                }

                val reviewList = document.data.get("review") as ArrayList<Map<Any, Any>>

                for (review in reviewList)  {
                    avgRating += (review["score"]).toString().toFloat()

                    if ((review["score"]).toString().toFloat() > 0 && (review["score"]).toString().toFloat() <= 1)
                        starCount[0]++
                    else if ((review["score"]).toString().toFloat() <= 2)
                        starCount[1]++
                    else if ((review["score"]).toString().toFloat() <= 3)
                        starCount[2]++
                    else if ((review["score"]).toString().toFloat() <= 4)
                        starCount[3]++
                    else if ((review["score"]).toString().toFloat() <= 5)
                        starCount[4]++

                    db.collection("accounts").whereEqualTo("UID", review["UID"].toString()).get().addOnSuccessListener { result ->
                        for (tmp in result) {
                            val uid = tmp.data.get("UID").toString()
                            val username = tmp.data.get("fullname").toString()
                            val avatar = tmp.data.get("avt").toString()
                            val rating = review["score"].toString().toFloat()
                            val description = review["text"].toString()

                            userReviewList.add(
                                UserReviewObject(
                                uid,
                                username,
                                avatar,
                                rating,
                                description
                            )
                            )

                            bookDetailUserReviewAdapter.notifyDataSetChanged()
                        }
                    }
                }

                reviewListSize = reviewList.size
                avgRating /= reviewList.size
//                avgRating = round(avgRating * 10) / 10

                binding.ratingStarBar.rating = round(avgRating * 10) / 10
                binding.txtAVGrating.text = (round(avgRating * 10) / 10).toString()

                binding.txtRatingBookDetail.text = (round(avgRating * 10) / 10).toString()
                binding.txtNumberOfReview.text = reviewList.size.toString() + " review(s)"

                val raters = intArrayOf(
                    starCount[4],
                    starCount[3],
                    starCount[2],
                    starCount[1],
                    starCount[0]
                )

                binding.ratingReviews.createRatingBars(maxOf(starCount[0], starCount[1], starCount[2], starCount[3], starCount[4]), BarLabels.STYPE5, colors, raters)

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
//                    binding.ratingReviews.visibility = View.VISIBLE
//                    binding.ratingReviews.is
                }, 3000)
            }
        }

//        voucherAdapter = bookDetailFragmentVoucherViewPagerAdapter(requireContext(), data)
//        binding.vpVoucherBookDetail.adapter = voucherAdapter
//        binding.vpVoucherBookDetail.pageMargin = 20

        shopVoucherAdapter = bookDetailShopVoucherRVAdapter(context, voucherList)
        binding.rvBookDetailShopVoucher.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.rvBookDetailShopVoucher.adapter = shopVoucherAdapter

        otherBookFromShopAdapter = TopBookHomeFragmentAdapter(activity, otherBookFromStore, otherBookFromStoreRating)
        binding.rvOtherBookFromShopBookDetail.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.rvOtherBookFromShopBookDetail.adapter = otherBookFromShopAdapter
        getOtherBook()

        binding.txtBookDetailDescriptionMore.setOnClickListener {
            if (binding.txtBookDetailDescriptionMore.text == getString(R.string.bookDetailMore)) {
                binding.txtDescriptionBookDetail.maxLines = Int.MAX_VALUE
                binding.txtBookDetailDescriptionMore.text = getString(R.string.bookDetailLess)
            } else {
                binding.txtDescriptionBookDetail.maxLines = 10
                binding.txtBookDetailDescriptionMore.text = getString(R.string.bookDetailMore)
            }
        }

        bookDetailUserReviewAdapter = bookDetailUserReviewAdapter(context, userReviewList)
        binding.lvUserReviewDetail.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.lvUserReviewDetail.adapter = bookDetailUserReviewAdapter

        getOtherBookFromAllStore()
        bookDetailYouMayLoveAdapter = bookDetailYouMayLoveAdapter(context, otherBookFromOtherStore, otherBookFromOtherStoreRating)
        binding.rvBookDetailYouMayLove.layoutManager = GridLayoutManager(activity, 2)
        binding.rvBookDetailYouMayLove.adapter = bookDetailYouMayLoveAdapter

        // Open Review dialog
        binding.btnWriteReviewBookDetail.setOnClickListener{
            activity?.let {
//                val newTheme = R.style.BottomSheetSignNightTheme
//                requireActivity().theme.applyStyle(newTheme, true)
                reviewDialogBookDetail.show(it){
                    style(SheetStyle.BOTTOM_SHEET)
                    onPositive {
                        this.dismiss()
                        upLoadComment(getClientRating(), getClientReview(), getImage()) // Upload Comment
                    }
                }
            }
        }

        binding.imgOpenChatBookDetail.setOnClickListener({
            lifecycleScope.launch {
                val storeID = getBookStoreID(bookID)
                val storeUID = getUIDFromBookStoreID(storeID)

                val intent = Intent(context, ChannelChatActivity::class.java)
                intent.putExtra("storeUID", storeUID)

                Toast.makeText(activity, storeUID, Toast.LENGTH_SHORT).show()

                startActivity(intent)
            }
        })

        // Add to wishlist
        isFollowed.observe(viewLifecycleOwner) {
            binding.addToWishList.isChecked = it
        }
        binding.addToWishList.setOnClickListener{
            val item = binding.addToWishList
            item.isChecked = !item.isChecked
            if (item.isChecked) item.playAnimation()
        }


        return view
    }

    fun controlNumberCounter() {
        binding.btnMinusNumberCount.setOnClickListener({
            val number = binding.txtNumberCount.text.toString().toInt()

            if (number > 0) {
                binding.txtNumberCount.text = (number - 1).toString()
                binding.txtNumberCountBottom.text = (number - 1).toString()
            }
        })

        binding.btnPlusNumberCount.setOnClickListener({
            val number = binding.txtNumberCount.text.toString().toInt()
            binding.txtNumberCount.text = (number + 1).toString()
            binding.txtNumberCountBottom.text = (number + 1).toString()
        })
    }

    fun controlBottomMenu() {
        binding.nsvBookDetail.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener{_, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY && scrollY > 1300) {
                if (binding.cvBottomMenu.visibility == View.INVISIBLE) {
                    binding.cvBottomMenu.visibility = View.VISIBLE
                    val fadeInAnimation = AlphaAnimation(0.0f, 1.0f)
                    fadeInAnimation.duration = 500 // Adjust the duration as needed
                    binding.cvBottomMenu.startAnimation(fadeInAnimation)
                }

            } else if (scrollY < oldScrollY) {
                if (binding.cvBottomMenu.visibility == View.VISIBLE) {
                    val fadeOutAnimation = AlphaAnimation(1.0f, 0.0f)
                    fadeOutAnimation.duration = 500 // Adjust the duration as needed
                    fadeOutAnimation.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {}
                        override fun onAnimationRepeat(animation: Animation?) {}
                        override fun onAnimationEnd(animation: Animation?) {
                            binding.cvBottomMenu.visibility = View.INVISIBLE
                        }
                    })
                    binding.cvBottomMenu.startAnimation(fadeOutAnimation)
                }
            }
        })
    }

    fun addToCart() {
        bindProgressButton(binding.btnAddToCard)
        binding.btnAddToCard.attachTextChangeAnimator()
        requireActivity().bindProgressButton(binding.btnAddToCard)

        val userID = Firebase.auth.currentUser?.uid

        binding.btnAddToCard.setOnClickListener {
            binding.btnAddToCard.showProgress {
                buttonTextRes = R.string.book_detail_adding_to_cart
                progressColor = Color.WHITE
            }

            db.collection("accounts").whereEqualTo("UID", userID).get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        var tmp = document.data.get("cart")
                        var cart = mutableMapOf<String, Any>()

                        if (tmp != null)
                            cart = (document.data.get("cart") as Map<String, Any>).toMutableMap()

                        lifecycleScope.launch {
                            val data: MutableMap<Any, Any> = mutableMapOf()

                            val storeID = getBookStoreID(bookID)
                            val storeUID = getUIDFromBookStoreID(storeID)


                            data["quantity"] = binding.txtNumberCount.text.toString().toInt()
                            data["storeID"] = storeUID

                            cart[bookID] = data

                            if (storeID.isNotEmpty()) {
                                db.collection("accounts")
                                    .document(document.id)
                                    .update("cart", cart).addOnCompleteListener {
                                        Handler().postDelayed({
                                            binding.btnAddToCard.hideProgress(R.string.book_detail_update_quantity)
                                        }, 2000)
                                    }
                            } else {
                                Handler().postDelayed({
                                    binding.btnAddToCard.hideProgress(R.string.book_detail_update_quantity_denied)
                                }, 2000)
                            }
                        }
                    }
                }
        }
    }

    fun updateRatingAndComment(rating: Float, cmt: String, images: ArrayList<Photo>) {
        val docBookRef = db.collection("books").whereEqualTo("bookID", bookID)

        val storageRef = storage.reference
        docBookRef.get().addOnSuccessListener { result ->
            for (document in result){
                val reviewsArray =
                    document.get("review") as ArrayList<Map<String, Any>>

                val uid = userInfo!!.userID
                var rating = userInfo!!.ratings
                var cmt = userInfo!!.description

                // Each file added to storage
                for (image in images) {
                    val path = image.nameFile
                    val fileName = path?.substringAfterLast("/")
                    val reviewEachImageRef = storageRef.child("reviewImages/${userInfo!!.userID}/${fileName}")

                    val bitmap = image.image
                    val baos = ByteArrayOutputStream()
                    if (bitmap != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    }

                    val data = baos.toByteArray()
                    var uploadTask = reviewEachImageRef.putBytes(data)

                    uploadTask.addOnFailureListener { exception ->
                        Log.e("Upload Failure", "Failed to upload image: $exception")
                    }.addOnSuccessListener { taskSnapshot ->
                        Log.i("Upload Success", "Image uploaded successfully")
                        // You can also log or extract metadata if needed
                        val downloadUrl = taskSnapshot.storage.downloadUrl
                        Log.i("Download URL", "Download URL: $downloadUrl")
                    }

                }

                // update the path to user review uploaded image folder
                val folderImageRef = storageRef.child("reviewImages/${userInfo!!.userID}")
                val link = folderImageRef.toString()

                val updatedReview = mapOf(
                    "UID" to uid,
                    "image" to link,
                    "score" to rating.toInt(),
                    "text" to cmt
                )

                var reviewUpdated = false

                for ((index, review) in reviewsArray.withIndex()) {
                    if (review["UID"] == uid) { // Assuming "UID" uniquely identifies each review
                        reviewsArray[index] = updatedReview
                        reviewUpdated = true
                        break // Stop searching once the review is found and updated
                    }
                }

                // If the review is not found, add it to the front of the list
                if (!reviewUpdated) {
                    reviewsArray.add(0, updatedReview)
                }

                // Update the array field in the document
                document.reference.update("review", reviewsArray)
                    .addOnSuccessListener {
                        Log.i("update Review", "Success")
                    }
                    .addOnFailureListener {
                        Log.i("update Review", "Failed")
                    }

            }

        }

    }

    override fun onPause() {
        super.onPause()
        val item = binding.addToWishList

        val docAccountRef = db.collection("accounts").whereEqualTo("UID", userInfo?.userID)

        docAccountRef.get().addOnSuccessListener {
            for (document in it) {
                val userWishList = userInfo?.wishListObject

                // Check is item existed in list
                if (item.isChecked) {
                    val index = userWishList?.indexOf(bookID) ?: -2
                    if (index == -1){
                        userWishList?.add(bookID)
                    }
                }
                else {
                    val index = userWishList?.indexOf(bookID) ?: -2
                    if (index != -1){
                        userWishList?.remove(bookID)
                    }
                }

                if (userWishList != null)
                    document.reference.update("wishlist", userWishList)
                        .addOnSuccessListener {
                            Log.i("update Wish List", "Success")
                        }
                        .addOnFailureListener {
                            Log.i("update Wish List", "Failed")
                        }
            }
        }

    }
    suspend fun getInfoCurrentUser(UID: String): UserReviewObject? {
        var res: UserReviewObject? = null
        try {
            val docRef = db.collection("accounts").whereEqualTo("UID", UID).get().await()

            for (doc in docRef.documents) {
                val uid = doc.data?.get("UID").toString()
                val username = doc.data?.get("fullname").toString()
                val avatar = doc.data?.get("avt").toString()
                val wishListBookID = doc.data?.get("wishlist") as ArrayList<*>?

                var rating = 0.0F
                var cmt = ""
                var linkToFolder = ""
                val bookRef = db.collection("books").whereEqualTo("bookID", bookID).get().await()

                for (book in bookRef.documents) {
                    if (book.data?.get("review") != null) {

                        val reviewsArray =
                            book.data!!.get("review") as ArrayList<Map<String, Any>>

                        if (reviewsArray.isNotEmpty()) {
                            val reviewOfUser = reviewsArray.single{it -> it.get("UID") == UID}
                            rating = reviewOfUser.get("score").toString().toFloat()
                            cmt = reviewOfUser.get("text").toString()
                            linkToFolder = reviewOfUser.get("image").toString()
                        }

                    }
                }

                res = UserReviewObject(uid, username, avatar, rating, cmt)
                if (wishListBookID != null) {
                    res.wishListObject = wishListBookID as ArrayList<String>
                }
                if (linkToFolder.isNotEmpty()) {
                    res.reviewPhotos = downloadImagesFromFolder(linkToFolder)
                }
            }
        } catch (e: Exception){
            Log.e("Firebase Error", "Can't retrieve data")
            val docRef = db.collection("accounts").whereEqualTo("UID", UID).get().await()

            for (doc in docRef.documents) {
                val uid = doc.data?.get("UID").toString()
                val username = doc.data?.get("fullname").toString()
                val avatar = doc.data?.get("avt").toString()
                var rating = 0.0F
                var cmt = ""
                res = UserReviewObject(uid, username, avatar, rating, cmt)
            }

        }

        return res
    }

    fun downloadImagesFromFolder(folderPath: String): ArrayList<Photo>{
        val storage = Firebase.storage

        val listRef =  storage.getReferenceFromUrl(folderPath)

        val dataPhoto = ArrayList<Photo>()

        listRef.listAll()
            .addOnSuccessListener { listResult ->
                listResult.items.forEach { item ->

                    val originalFilename = item.name

                    // Download the file directly into a Bitmap
                    item.getBytes(Long.MAX_VALUE)
                        .addOnSuccessListener { bytes ->
                            // Convert the downloaded bytes to a Bitmap
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            val photo = Photo()
                            photo.id = Photo.totalID
                            photo.nameFile = item.name
                            photo.id?.let { Photo.updateTotalID(it) }
                            photo.image = bitmap
                            dataPhoto.add(photo)
                            // Now you can use the bitmap for further processing
                            // For example, you can display it in an ImageView
                            // imageView.setImageBitmap(bitmap)
                        }
                        .addOnFailureListener { exception ->
                            // Handle any errors
                            println("Failed to download $originalFilename: $exception")
                        }
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                println("Failed to list files in folder: $exception")
            }

        return dataPhoto
    }

    fun checkDoesUserReviewExist(UID: String): Int{
        if (userReviewList.isNotEmpty())
        {
            try {
                val existReview = userReviewList.single { it.userID == UID }
                return userReviewList.indexOf(existReview)
            } catch (e: Exception) {
                return -1
            }
        }
        return -1
    }
    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    fun upLoadComment(rating: Float, cmt: String, images: ArrayList<Photo>){
        oldRating = userInfo!!.ratings // Save the oldRating
        userInfo!!.ratings = rating
        userInfo!!.description = cmt

        // Update or Add new Review
        updateRatingAndComment(rating, cmt, images)
        if (checkDoesUserReviewExist(userInfo!!.userID) == -1){
            userReviewList.add(0, userInfo!!)

            // Update new AvgRating
            var newAvgRating = avgRating * reviewListSize
            reviewListSize += 1
            newAvgRating += userInfo!!.ratings
            avgRating = newAvgRating / reviewListSize
            // update UI
            binding.ratingStarBar.rating = round(avgRating * 10) / 10
            binding.txtAVGrating.text = (round(avgRating * 10) / 10).toString()

            binding.txtRatingBookDetail.text = (round(avgRating * 10) / 10).toString()
            binding.txtNumberOfReview.text = reviewListSize.toString() + " review(s)"

            // Define star it belong to
            if (userInfo!!.ratings > 0 && userInfo!!.ratings <= 1)
                starCount[0]++
            else if (userInfo!!.ratings <= 2)
                starCount[1]++
            else if (userInfo!!.ratings <= 3)
                starCount[2]++
            else if (userInfo!!.ratings <= 4)
                starCount[3]++
            else if (userInfo!!.ratings <= 5)
                starCount[4]++

            // Update rating bar chart
            val raters = intArrayOf(
                starCount[4],
                starCount[3],
                starCount[2],
                starCount[1],
                starCount[0]
            )

            binding.ratingReviews.createRatingBars(maxOf(starCount[0], starCount[1], starCount[2], starCount[3], starCount[4]), BarLabels.STYPE5, colors, raters)
        }
        else {
            // If User has already commented
            val index = checkDoesUserReviewExist(userInfo!!.userID)

//            val oldRating = userReviewList[index].ratings // Old rating
            userReviewList[index] = userInfo!! // Now u can update to new rating
            Collections.swap(userReviewList, index, 0)

            var newAvgRating = avgRating * reviewListSize
            newAvgRating -= oldRating // Remove old rating
            newAvgRating += userInfo!!.ratings // Update to new Rating from user
            avgRating = newAvgRating / reviewListSize // Update avgRating

            // update UI
            binding.ratingStarBar.rating = round(avgRating * 10) / 10
            binding.txtAVGrating.text = (round(avgRating * 10) / 10).toString()

            binding.txtRatingBookDetail.text = (round(avgRating * 10) / 10).toString()
            binding.txtNumberOfReview.text = reviewListSize.toString() + " review(s)"

            // Define star newRating belong to
            if (userInfo!!.ratings > 0 && userInfo!!.ratings <= 1)
                starCount[0]++
            else if (userInfo!!.ratings <= 2)
                starCount[1]++
            else if (userInfo!!.ratings <= 3)
                starCount[2]++
            else if (userInfo!!.ratings <= 4)
                starCount[3]++
            else if (userInfo!!.ratings <= 5)
                starCount[4]++

            // Remove Old rating belong to
            if (oldRating > 0 && oldRating <= 1)
                starCount[0]--
            else if (oldRating <= 2)
                starCount[1]--
            else if (oldRating <= 3)
                starCount[2]--
            else if (oldRating <= 4)
                starCount[3]--
            else if (oldRating <= 5)
                starCount[4]--

            // Update rating bar chart
            val raters = intArrayOf(
                starCount[4],
                starCount[3],
                starCount[2],
                starCount[1],
                starCount[0]
            )

            binding.ratingReviews.createRatingBars(maxOf(starCount[0], starCount[1], starCount[2], starCount[3], starCount[4]), BarLabels.STYPE5, colors, raters)

        }
        bookDetailUserReviewAdapter.notifyDataSetChanged()

        // Update Review Dialog with existed content
        val initValues = InitFilterValuesReviewBookDetail()
        initValues.rating = rating
        initValues.text = cmt
        initValues.reviewPhotos = images
        reviewDialogBookDetail.updateinitValue(initValues)


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

    suspend fun getBookStoreID(bookID: String): String {
        return try {
            val querySnapshot = db.collection("personalStores").whereNotEqualTo("items.$bookID.price", null).get().await()
            for (document in querySnapshot.documents) {
                return document.id.toString()
            }
            // Return a default value if the book price is not found
            ""
        } catch (e: Exception) {
            // Handle failures
            Log.e("Firestore", "Error getting store id", e)
            // Return a default value in case of failure
            ""
        }
    }

    suspend fun getUIDFromBookStoreID(storeID: String): String {
        if (storeID.isEmpty()) return ""
        val reference = db.collection("personalStores").document(storeID)

        return try {
            val querySnapshot = db.collection("accounts").whereEqualTo("store", reference).get().await()
            for (document in querySnapshot.documents) {
                return document.data!!.get("UID").toString()
            }
            // Return a default value if the book price is not found
            ""
        } catch (e: Exception) {
            // Handle failures
            Log.e("Firestore", "Error getting store id", e)
            // Return a default value in case of failure
            ""
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

// Sample download files
//val folderPath = "reviewImages/awpku0XpjfWlZ1lhjBOaPf4oJCZ2" // The path to the folder in Firebase Storage
//
//// Create a reference to the folder in Firebase Storage
//val folderRef = storage.getReferenceFromUrl("gs://book-store-3ed32.appspot.com/$folderPath")
//
//// List all items (files) within the folder
//folderRef.listAll().addOnSuccessListener { listResult ->
//    // Iterate through the items (files) in the folder
//    for (item in listResult.items) {
//        // Get the download URL for the file
//        item.downloadUrl.addOnSuccessListener { url ->
//            // Download the file using the download URL
//            val fileName = item.name
//            val localFile = File("/path/to/local/directory/$fileName") // Provide the path where you want to save the file locally
//            item.getFile(localFile).addOnSuccessListener {
//                // File downloaded successfully
//                Log.i("Download", "File downloaded successfully: $localFile")
//            }.addOnFailureListener { exception ->
//                // Handle failure to download the file
//                Log.e("Download", "Failed to download file $fileName: ${exception.message}")
//            }
//        }.addOnFailureListener { exception ->
//            // Handle failure to get the download URL for the file
//            Log.e("Download", "Failed to get download URL for file: ${exception.message}")
//        }
//    }
//}.addOnFailureListener { exception ->
//    // Handle failure to list items in the folder
//    Log.e("Download", "Failed to list items in folder: ${exception.message}")
//}
