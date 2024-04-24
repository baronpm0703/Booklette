package com.example.booklette

import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract.CommonDataKinds.Im
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.databinding.FragmentManageshopBooksBinding
import com.example.booklette.databinding.FragmentMyshopBinding
import com.example.booklette.model.HRecommendedBookObject
import com.example.booklette.model.ManageShopNewBookObject
import com.example.booklette.model.MyShopBookObject
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso
import kotlin.math.abs
import kotlin.math.round

/**
 * A simple [Fragment] subclass.
 * Use the [ManageShopBooksFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ManageShopBooksFragment : Fragment() {
//	private var discountHScrollView: HorizontalScrollView? = null
//	private var newArrivalsHScrollView: HorizontalScrollView? = null

	private var _binding: FragmentManageshopBooksBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		_binding = FragmentManageshopBooksBinding.inflate(inflater, container, false)
		val view = binding.root

		val auth = Firebase.auth
		val db = Firebase.firestore

		val bookList = arrayListOf<MyShopBookObject>()
		db.collection("accounts").whereEqualTo("UID", auth.uid).get()
			.addOnSuccessListener { documents ->
				if (documents.size() != 1) return@addOnSuccessListener	// Failsafe

				for (document in documents) {
					document.getDocumentReference("store")!!.get().addOnSuccessListener { storeSnapshot ->
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
										val bookPrice = book["price"] as Long
										val bookRemain = book["remain"] as Long
										val bookSold = book["sold"] as Long
										val bookStatus = book["status"].toString()
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
												bookId, bookName, bookGenre, bookAuthor, bookImg, bookPrice, bookDiscount, bookRemain, bookSold, bookStatus, bookReleaseDate
											)
											bookList.add(newBookObject)
										}
									}
								}
							}
						}

						Handler().postDelayed({
							// Book list scroll view
							setBookListViews(view, bookList)
						}, 200)
					}
				}
			}

		return view
	}

	private fun setBookListViews(view: View, bookList: ArrayList<MyShopBookObject>) {
		val content = view.findViewById<LinearLayout>(R.id.bookListScrollViewContent)

		for (book in bookList) {
			var singleFrame: View = layoutInflater.inflate(R.layout.manageshop_book_item, null)
			singleFrame.id = bookList.indexOf(book)

			val bookImg = singleFrame.findViewById<ImageView>(R.id.bookImg)
			val bookGenreText = singleFrame.findViewById<TextView>(R.id.bookGenreText)
			val bookNameText = singleFrame.findViewById<TextView>(R.id.bookNameText)
			val bookAuthorText = singleFrame.findViewById<TextView>(R.id.bookAuthorText)
			val bookPriceText = singleFrame.findViewById<TextView>(R.id.bookPriceText)

			Picasso.get()
				.load(book.image)
				.into(bookImg)
			bookGenreText.text = book.genre
			bookNameText.text = book.name
			bookAuthorText.text = book.author
			bookPriceText.text = book.shopPrice.toString()

			singleFrame.setOnClickListener {
				if (context is homeActivity) {
					var bdFragment = BookDetailFragment()

					var bundle = Bundle()
					bundle.putString("bookID", book.id)

					bdFragment.arguments = bundle

//                Toast.makeText(context, pageTitles[position].bookID.toString(), Toast.LENGTH_SHORT).show()

					(context as homeActivity).changeFragmentContainer(
						bdFragment,
						(context as homeActivity).smoothBottomBarStack[(context as homeActivity).smoothBottomBarStack.size - 1]
					)
				}
			}

			content.addView(singleFrame)
		}

		content.visibility = View.VISIBLE
	}

	private fun addNewBook(view: View, newBook: ManageShopNewBookObject) {
		val auth = Firebase.auth
		val db = Firebase.firestore

		val bookColl = db.collection("books")
		bookColl.get().addOnSuccessListener {
			val documents = it.documents
			var max: Long = 0
			for (document in documents) {
				val id = document.get("bookID").toString().filter { it.isDigit() }.toLong()

				if (id > max) max = id
			}
			val newBookID = "BK" + (max + 1)
			val newBookMap = hashMapOf(
				"author" to newBook.author,
				"best-deal-sale" to 0.0,
				"bookID" to newBookID,
				"description" to newBook.description,
				"genre" to newBook.genre,
				"image" to newBook.image,
				"name" to newBook.name,
				"releaseDate" to newBook.releaseDate,
				"review" to emptyArray<Any>(),
				"top-book" to newBook.topBook,
				"type" to newBook.type
			)

			bookColl.add(newBookMap)

			db.collection("accounts").whereEqualTo("UID", auth.uid).get()
				.addOnSuccessListener { documents ->
					if (documents.size() != 1) return@addOnSuccessListener	// Failsafe

					val document = documents.documents[0]
					val store = document.getDocumentReference("store")
					store!!.get().addOnSuccessListener { storeSnapshot ->
						val bookList = storeSnapshot.get("items") as HashMap<String, Map<String, Any>>

						val newStoreBookMap = hashMapOf(
							"discount" to "",
							"price" to newBook.price,
							"remain" to newBook.quantity,
							"sold" to newBook.quantity,
							"status" to ""
						)

						bookList[newBookID] = newStoreBookMap

						store.update("items", bookList)
					}
				}

			Handler().postDelayed({
				
			}, 2000)
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	fun changeFragmentContainer(fragment: Fragment) {
		parentFragmentManager.beginTransaction().replace(R.id.fcvNavigation, fragment).addToBackStack(null).commit()
	}
}