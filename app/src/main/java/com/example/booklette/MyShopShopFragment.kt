package com.example.booklette

import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Im
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.databinding.FragmentMyshopBinding
import com.example.booklette.databinding.FragmentMyshopShopTabBinding
import com.squareup.picasso.Picasso

/**
 * A simple [Fragment] subclass.
 * Use the [MyShopShopFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyShopShopFragment : Fragment() {
//	private var discountHScrollView: HorizontalScrollView? = null
//	private var newArrivalsHScrollView: HorizontalScrollView? = null
	private var discountViews = arrayListOf<View>()
	private var newArrivalsViews = arrayListOf<View>()
	private var bestSellersViews = arrayListOf<View>()

	private var _binding: FragmentMyshopShopTabBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		_binding = FragmentMyshopShopTabBinding.inflate(inflater, container, false)
		val view = binding.root

//		discountHScrollView = view.findViewById(R.id.discountHScroll)
//		newArrivalsHScrollView = view.findViewById(R.id.newArrivalsScrollView)

		// Discount scroll view
		var discounts = arrayListOf(Triple(10, 100, 30), Triple(20, 150, 20), Triple(30, 120, 50))
		setDiscountItemViews(view, discounts)

		// New arrivals scroll view
		var newArrivals = arrayListOf<BookObject>()
		newArrivals.add(BookObject("B001", "The Catcher in the Eyes", "Novel", "Chanh Tin", "10/03/2003", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b", 12.50F))
		newArrivals.add(BookObject("B001", "The Catcher in the Eyes", "Novel", "Chanh Tin", "10/03/2003", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b", 12.50F))
		newArrivals.add(BookObject("B001", "The Catcher in the Eyes", "Novel", "Chanh Tin", "10/03/2003", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b", 12.50F))
		var newArrivalsDisc = arrayListOf(5F, 4F, 3F, 2F, 1F)
		setNewArrivalsItemViews(view, newArrivals, newArrivalsDisc)

		// Best Sellers scroll view
		var bestSellers = arrayListOf<BookObject>()
		bestSellers.add(BookObject("B001", "The Catcher in the Eyes", "Novel", "Chanh Tin", "10/03/2003", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b", 12.50F))
		bestSellers.add(BookObject("B001", "The Catcher in the Eyes", "Novel", "Chanh Tin", "10/03/2003", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b", 12.50F))
		bestSellers.add(BookObject("B001", "The Catcher in the Eyes", "Novel", "Chanh Tin", "10/03/2003", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b", 12.50F))
		var bestSellersDisc = arrayListOf(5F, 4F, 3F, 2F, 1F)
		setBestSellersItemViews(view, bestSellers, bestSellersDisc)

		// Highly Recommended scroll view
		var highlyRecommended = arrayListOf<HRecommendedBookObject>()
		highlyRecommended.add(HRecommendedBookObject(BookObject("B001", "The Catcher in the Eyes", "Novel", "Chanh Tin", "10/03/2003", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b", 12.50F), 4.5F))
		highlyRecommended.add(HRecommendedBookObject(BookObject("B001", "The Catcher in the Eyes", "Novel", "Chanh Tin", "10/03/2003", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b", 12.50F), 4.5F))
		highlyRecommended.add(HRecommendedBookObject(BookObject("B001", "The Catcher in the Eyes", "Novel", "Chanh Tin", "10/03/2003", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b", 12.50F), 4.5F))
		highlyRecommended.add(HRecommendedBookObject(BookObject("B001", "The Catcher in the Eyes", "Novel", "Chanh Tin", "10/03/2003", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b", 12.50F), 4.5F))
		highlyRecommended.add(HRecommendedBookObject(BookObject("B001", "The Catcher in the Eyes", "Novel", "Chanh Tin", "10/03/2003", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b", 12.50F), 4.5F))
		setHighlyRecommendedItemViews(view, highlyRecommended)

		return view
	}

	private fun setDiscountItemViews(view: View, discounts: ArrayList<Triple<Int, Int, Int>>) {
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
	}

	private fun setNewArrivalsItemViews(view: View, newArrivals: ArrayList<BookObject>, discounts: ArrayList<Float>) {
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
			bookPriceText.text = newArrival.price.toString()
			bookDiscountText.text = discounts[singleFrame.id].toString() + "%"

			newArrivalsViews.add(singleFrame)
			content.addView(singleFrame)
		}
	}

	private fun setBestSellersItemViews(view: View, newArrivals: ArrayList<BookObject>, discounts: ArrayList<Float>) {
		val content = view.findViewById<LinearLayout>(R.id.bestSellersScrollViewContent)

		for (newArrival in newArrivals) {
			var singleFrame: View
			val id = newArrivals.indexOf(newArrival)

			if (id == 0) singleFrame = layoutInflater.inflate(R.layout.myshop_book_item_bestseller_top1, null)
			else if (id == 1) singleFrame = layoutInflater.inflate(R.layout.myshop_book_item_bestseller_top2, null)
			else singleFrame = layoutInflater.inflate(R.layout.myshop_book_item, null)

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
			bookPriceText.text = newArrival.price.toString()
			bookDiscountText.text = discounts[singleFrame.id].toString() + "%"

			bestSellersViews.add(singleFrame)
			content.addView(singleFrame)
		}
	}

	private fun setHighlyRecommendedItemViews(view: View, books: ArrayList<HRecommendedBookObject>) {
		val content = view.findViewById<RecyclerView>(R.id.highlyRecommendedList)

		var highlyRecommendedAdapter = MyShopHighlyRecommendedAdapter(books)
		highlyRecommendedAdapter.setOnItemClickListener(object: MyShopHighlyRecommendedAdapter.onItemClickListener {
			override fun onItemClick(position: Int) {

			}

		})
		content!!.adapter = highlyRecommendedAdapter
		content.setLayoutManager(GridLayoutManager(view.context, 2, GridLayoutManager.HORIZONTAL, false))
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