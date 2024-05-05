package com.example.booklette

import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.databinding.FragmentMyshopShopTabBinding
import com.example.booklette.databinding.FragmentViewshopShopTabBinding
import com.example.booklette.model.BookObject
import com.example.booklette.model.HRecommendedBookObject
import com.example.booklette.model.MyShopBookObject
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/**
 * A simple [Fragment] subclass.
 * Use the [ViewShopShopFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewShopShopFragment : Fragment() {
//	private var discountHScrollView: HorizontalScrollView? = null
//	private var newArrivalsHScrollView: HorizontalScrollView? = null
	private var discountViews = arrayListOf<View>()
	private var newArrivalsViews = arrayListOf<View>()
	private var bestSellersViews = arrayListOf<View>()

	private var _binding: FragmentViewshopShopTabBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		_binding = FragmentViewshopShopTabBinding.inflate(inflater, container, false)
		val view = binding.root

		val auth = Firebase.auth
		val db = Firebase.firestore

//		discountHScrollView = view.findViewById(R.id.discountHScroll)
//		newArrivalsHScrollView = view.findViewById(R.id.newArrivalsScrollView)
		val noBookView = view.findViewById<View>(R.id.noBookView1)
		val newArrivalsFlavorText = view.findViewById<TextView>(R.id.newFlavorText)
		val newArrivalsBtn = view.findViewById<TextView>(R.id.newMoreBtn)
		val bestSellersFlavorText = view.findViewById<TextView>(R.id.bestFlavorText)
		val bestSellersBtn = view.findViewById<TextView>(R.id.bestMoreBtn)
		val highlyRecommendedFlavorText = view.findViewById<RelativeLayout>(R.id.recommendedFlavorText)

		// Data arrays
		val discounts = arrayListOf<Triple<Long, Long, Long>>()
		val newArrivals = arrayListOf<MyShopBookObject>()
		val bestSellers = arrayListOf<MyShopBookObject>()
		val highlyRecommended = arrayListOf<HRecommendedBookObject>()
		val args = arguments
		if (args != null) {
			args.getString("shopID")?.let {
				db.collection("personalStores").document(it).get().addOnSuccessListener { storeSnapshot ->
					val shopVouchers = storeSnapshot.get("shopVouchers") as ArrayList<String>
					for (voucher in shopVouchers) {
						db.collection("discounts").whereEqualTo("discountID", voucher).get().addOnSuccessListener {
							val vouchers = it.documents

							for (voucher in vouchers) {
								if (voucher.getString("discountType") != "shop") continue

								val voucherExpire = voucher.get("endDate") as Timestamp
								val dateFrom = Calendar.getInstance().apply { time = Timestamp.now().toDate() }
								val dateTo = Calendar.getInstance().apply { time = voucherExpire.toDate() }
								val voucherPeriod = abs(dateTo.get(Calendar.DAY_OF_YEAR) - dateFrom.get(Calendar.DAY_OF_YEAR)).toLong()
								val voucherPercent = voucher.get("percent") as Long
								val voucherMinimum = voucher.get("minimumOrder") as Long

								discounts.add(Triple(voucherPercent, voucherMinimum, voucherPeriod))
							}
						}
					}

					val books = storeSnapshot.get("items") as Map<String, Map<String, Any>>
					if (books.size > 0) {
						books.map { entry ->
							val book = entry.value.toMutableMap()

							db.collection("books").whereEqualTo("bookID", entry.key).get().addOnSuccessListener {bookQuery ->
								if (bookQuery.size() != 1) return@addOnSuccessListener   // Failsafe

								for (info in bookQuery) {
									val bookId = info.get("bookID").toString()
									val bookImg = info.get("image").toString()
									val bookName = info.get("name").toString()
									val bookGenre = info.get("genre").toString()
									val bookAuthor = info.get("author").toString()
									val bookReleaseDate = info.get("releaseDate") as Timestamp
									val bookPrice = book["price"] as Number
									val bookRemain = book["remain"].toString().toLong()
									val bookSold = book["sold"].toString().toLong()
									val bookStatus = book["status"].toString()
									val bookDescription = info.get("description").toString()
									val bookType = info.get("type").toString()
									val bookRatings = info.get("review") as ArrayList<Map<String, Any>>
									var bookRatingScore: Float = 0F
									bookRatings.forEach {
										bookRatingScore += (it.get("score") as Long).toFloat()
									}
									val bookRatingCnt = bookRatings.size
									bookRatingScore /= bookRatingCnt
									// Get a book's discount info
									val bookDiscountId = book["discount"] as String
									var bookDiscount = 0F
									db.collection("discounts").whereEqualTo("discountID", bookDiscountId).get().addOnSuccessListener {
										Log.i("MyShop", bookName + " - " + it.documents.size.toString())
										if (it.documents.size > 0)
											bookDiscount = (it.documents[0].get("percent") as Long).toFloat() / 100

										val newBookObject = MyShopBookObject(
											bookId, bookName, bookGenre, bookAuthor, bookImg, bookPrice.toLong(), bookDiscount, bookRemain, bookSold, bookStatus, bookReleaseDate, bookDescription, bookType
										)
										val newRecommendedBookObject = HRecommendedBookObject(
											bookId, bookImg, bookRatingScore, bookRatingCnt.toLong(), bookName, bookPrice.toLong()
										)

										val dateFrom = Calendar.getInstance().apply { time = Timestamp.now().toDate() }
										val dateTo = Calendar.getInstance().apply { time = bookReleaseDate.toDate() }
										val period = abs(dateTo.get(Calendar.MONTH) - dateFrom.get(Calendar.MONTH))
										if (period <= 1) {
											newArrivals.add(newBookObject)
										}
										if (bestSellers.size == 0 || bestSellers[0].sold >= bookSold)
											bestSellers.add(newBookObject)
										else bestSellers.add(0, newBookObject)
										if (highlyRecommended.size == 0 || highlyRecommended[0].rating >= bookRatingScore)
											highlyRecommended.add(0, newRecommendedBookObject)
										else highlyRecommended.add(newRecommendedBookObject)
									}
								}
							}
						}
						noBookView.visibility = View.GONE
					} else {
						noBookView.visibility = View.VISIBLE
						newArrivalsFlavorText.visibility = View.GONE
						newArrivalsBtn.visibility = View.GONE
						bestSellersFlavorText.visibility = View.GONE
						bestSellersBtn.visibility = View.GONE
						highlyRecommendedFlavorText.visibility = View.GONE
					}

					Handler().postDelayed({
						// Discount scroll view
						setDiscountItemViews(view, discounts)
						// New Arrivals scroll view
						setNewArrivalsItemViews(view, newArrivals)
						// Best Sellers scroll view
						setBestSellersItemViews(view, bestSellers)
						// Highly Recommended scroll view
						setHighlyRecommendedItemViews(view, highlyRecommended)
					}, 2000)
				}
			}
		}

		return view
	}

	private fun setDiscountItemViews(view: View, discounts: ArrayList<Triple<Long, Long, Long>>) {
		// Stop this function if fragment is already destroyed
		if (!isAdded || activity == null) return

		val content = view.findViewById<LinearLayout>(R.id.discountHScrollContent)

		for (discount in discounts) {
			var singleFrame: View = layoutInflater.inflate(R.layout.myshop_discount_item, null)
			singleFrame.id = discounts.indexOf(discount)

			val discountPercentView = singleFrame.findViewById<TextView>(R.id.discountPercentText)
			val discountMinimumView = singleFrame.findViewById<TextView>(R.id.discountMinimumText)
			val discountExpireView = singleFrame.findViewById<TextView>(R.id.discountExpireText)
			discountPercentView.text = discount.first.toString() + "%"
			discountMinimumView.text = discount.second.toString()
			discountExpireView.text = discount.third.toString()

			discountViews.add(singleFrame)
			content.addView(singleFrame)
		}

		content.visibility = View.VISIBLE
	}

	private fun setNewArrivalsItemViews(view: View, newArrivals: ArrayList<MyShopBookObject>) {
		// Stop this function if fragment is already destroyed
		if (!isAdded || activity == null) return

		val content = view.findViewById<LinearLayout>(R.id.newArrivalsScrollViewContent)

		for (newArrival in newArrivals) {
			var singleFrame: View = layoutInflater.inflate(R.layout.myshop_book_item, null)
			singleFrame.id = newArrivals.indexOf(newArrival)

			val bookImg = singleFrame.findViewById<ImageView>(R.id.bookImg)
			val bookGenreText = singleFrame.findViewById<TextView>(R.id.bookGenreText)
			val bookNameText = singleFrame.findViewById<TextView>(R.id.bookNameText)
			val bookAuthorText = singleFrame.findViewById<TextView>(R.id.bookAuthorText)
			val bookPriceText = singleFrame.findViewById<TextView>(R.id.bookPriceText)
			val bookDiscountText = singleFrame.findViewById<TextView>(R.id.bookDiscountText)

			Picasso.get()
				.load(newArrival.image)
				.into(bookImg)
			bookGenreText.text = newArrival.genre
			bookNameText.text = newArrival.name
			bookAuthorText.text = newArrival.author
			bookPriceText.text = newArrival.shopPrice.toString()
			bookDiscountText.text = (newArrival.discount * 100).toString() + "%"

			singleFrame.setOnClickListener {
				if (context is homeActivity) {
					var bdFragment = BookDetailFragment()

					var bundle = Bundle()
					bundle.putString("bookID", newArrival.id)

					bdFragment.arguments = bundle

//                Toast.makeText(context, pageTitles[position].bookID.toString(), Toast.LENGTH_SHORT).show()

					(context as homeActivity).changeFragmentContainer(
						bdFragment,
						(context as homeActivity).smoothBottomBarStack[(context as homeActivity).smoothBottomBarStack.size - 1]
					)
				}
			}

			newArrivalsViews.add(singleFrame)
			content.addView(singleFrame)
		}

		content.visibility = View.VISIBLE
	}

	private fun setBestSellersItemViews(view: View, bestSellers: ArrayList<MyShopBookObject>) {
		// Stop this function if fragment is already destroyed
		if (!isAdded || activity == null) return

		val content = view.findViewById<LinearLayout>(R.id.bestSellersScrollViewContent)

		for (bestSeller in bestSellers) {
			var singleFrame: View
			val id = bestSellers.indexOf(bestSeller)

			if (id == 0) singleFrame = layoutInflater.inflate(R.layout.myshop_book_item_bestseller_top1, null)
			else if (id == 1) singleFrame = layoutInflater.inflate(R.layout.myshop_book_item_bestseller_top2, null)
			else singleFrame = layoutInflater.inflate(R.layout.myshop_book_item, null)

			singleFrame.id = bestSellers.indexOf(bestSeller)

			val bookImg = singleFrame.findViewById<ImageView>(R.id.bookImg)
			val bookGenreText = singleFrame.findViewById<TextView>(R.id.bookGenreText)
			val bookNameText = singleFrame.findViewById<TextView>(R.id.bookNameText)
			val bookAuthorText = singleFrame.findViewById<TextView>(R.id.bookAuthorText)
			val bookPriceText = singleFrame.findViewById<TextView>(R.id.bookPriceText)
			val bookDiscountText = singleFrame.findViewById<TextView>(R.id.bookDiscountText)

			Picasso.get()
				.load(bestSeller.image)
				.into(bookImg)
			bookGenreText.text = bestSeller.genre
			bookNameText.text = bestSeller.name
			bookAuthorText.text = bestSeller.author
			bookPriceText.text = bestSeller.shopPrice.toString()
			bookDiscountText.text = (bestSeller.discount * 100).toString() + "%"

			singleFrame.setOnClickListener {
				if (context is homeActivity) {
					var bdFragment = BookDetailFragment()

					var bundle = Bundle()
					bundle.putString("bookID", bestSeller.id)

					bdFragment.arguments = bundle

//                Toast.makeText(context, pageTitles[position].bookID.toString(), Toast.LENGTH_SHORT).show()

					(context as homeActivity).changeFragmentContainer(
						bdFragment,
						(context as homeActivity).smoothBottomBarStack[(context as homeActivity).smoothBottomBarStack.size - 1]
					)
				}
			}

			bestSellersViews.add(singleFrame)
			content.addView(singleFrame)
		}

		content.visibility = View.VISIBLE
	}

	private fun setHighlyRecommendedItemViews(view: View, books: ArrayList<HRecommendedBookObject>) {
		val content = view.findViewById<RecyclerView>(R.id.highlyRecommendedList)

		var highlyRecommendedAdapter = MyShopHighlyRecommendedAdapter(books)
		highlyRecommendedAdapter.setOnItemClickListener(object: MyShopHighlyRecommendedAdapter.onItemClickListener {
			override fun onItemClick(position: Int) {
				if (context is homeActivity) {
					var bdFragment = BookDetailFragment()

					var bundle = Bundle()
					bundle.putString("bookID", books[position].id)

					bdFragment.arguments = bundle

//                Toast.makeText(context, pageTitles[position].bookID.toString(), Toast.LENGTH_SHORT).show()

					(context as homeActivity).changeFragmentContainer(
						bdFragment,
						(context as homeActivity).smoothBottomBarStack[(context as homeActivity).smoothBottomBarStack.size - 1]
					)
				}
			}

		})
		content!!.adapter = highlyRecommendedAdapter
		content.setLayoutManager(GridLayoutManager(view.context, 2, GridLayoutManager.HORIZONTAL, false))

		content.visibility = View.VISIBLE
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

//	companion object {
//		/**
//		 * Use this factory method to create a new instance of
//		 * this fragment using the provided parameters.
//		 *
//		 * @param param1 Parameter 1.
//		 * @param param2 Parameter 2.
//		 * @return A new instance of fragment MyShopFragment.
//		 */
//		// TODO: Rename and change types and number of parameters
//		@JvmStatic
//		fun newInstance(param1: String, param2: String) =
//			MyShopFragment().apply {
//				arguments = Bundle().apply {
//					putString(ARG_PARAM1, param1)
//					putString(ARG_PARAM2, param2)
//				}
//			}
//	}
}