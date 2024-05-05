package com.example.booklette

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.text.intl.Locale
import com.example.booklette.databinding.FragmentManageshopBooksBinding
import com.example.booklette.model.ManageShopNewBookObject
import com.example.booklette.model.MyShopBookObject
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.maxkeppeler.sheets.core.SheetStyle
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import kotlin.math.abs

/**
 * A simple [Fragment] subclass.
 * Use the [ManageShopBooksFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ManageShopBooksFragment : Fragment() {
	private lateinit var storeRef: DocumentReference

	private var _binding: FragmentManageshopBooksBinding? = null

	private val binding get() = _binding!!
	private val bookViews = arrayListOf<View>()

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
					storeRef = document.getDocumentReference("store")!!
					storeRef.get().addOnSuccessListener { storeSnapshot ->
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
											if (it.documents.size > 0)
												bookDiscount = (it.documents[0].get("percent") as Long).toFloat() / 100

											val newBookObject = MyShopBookObject(
												bookId, bookName, bookGenre, bookAuthor, bookImg, bookPrice.toLong(), bookDiscount, bookRemain, bookSold, bookStatus, bookReleaseDate, bookDescription, bookType
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
						}, 1000)
					}
				}
			}

		addBookDialog(view)

		view.findViewById<ImageView>(R.id.backBtn).setOnClickListener {
			requireActivity().onBackPressedDispatcher.onBackPressed()
		}

		return view
	}

	private fun boostBook(bookObject: MyShopBookObject) {
		val db = Firebase.firestore
		val bookColl = db.collection("books")
		val updatedData = hashMapOf(
			"releaseDate" to Timestamp.now()
		)
		bookColl.whereEqualTo("bookID", bookObject.id).get().addOnSuccessListener {
			for (bookDoc in it.documents) {
				bookColl.document(bookDoc.id).update(updatedData as Map<String, Any>)
			}
		}
	}

	private fun addNewBook(newBook: ManageShopNewBookObject) {
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
			val newBookID = "BK" + (max + 1).toString().padStart(3, '0')

			// Upload book image to storage
			val storageRef = Firebase.storage.reference
			val bookImage = newBook.image[0]
			val imageRef = storageRef.child("Books/${newBookID}")
			val baos = ByteArrayOutputStream()
			val bitmap = bookImage.image
			if (bitmap != null) {
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
			}
			val data = baos.toByteArray()
			imageRef.putBytes(data).addOnSuccessListener {
				it.storage.downloadUrl.addOnSuccessListener { result ->
					// Add to Books collection
					val newBookMap = hashMapOf(
						"author" to newBook.author,
						"best-deal-sale" to 0.0,
						"bookID" to newBookID,
						"description" to newBook.description,
						"genre" to newBook.genre,
						"image" to result,
						"name" to newBook.name,
						"releaseDate" to newBook.releaseDate,
						"review" to emptyList<Any>(),
						"top-book" to newBook.topBook,
						"type" to newBook.type
					)

					bookColl.add(newBookMap)

					// Add book to current user's personal store
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
									"sold" to 0,
									"status" to ""
								)

								bookList[newBookID] = newStoreBookMap

								store.update("items", bookList).addOnSuccessListener {
									addBookSuccessDialog()
								}
							}
						}

					Handler().postDelayed({
						val content = view?.findViewById<LinearLayout>(R.id.discountListScrollViewContent)

						var singleFrame: View = layoutInflater.inflate(R.layout.manageshop_book_item, null)
						singleFrame.id = bookViews.size

						val bookImg = singleFrame.findViewById<ImageView>(R.id.bookImg)
						val bookGenreText = singleFrame.findViewById<TextView>(R.id.bookGenreText)
						val bookNameText = singleFrame.findViewById<TextView>(R.id.bookNameText)
						val bookAuthorText = singleFrame.findViewById<TextView>(R.id.bookAuthorText)
						val bookPriceText = singleFrame.findViewById<TextView>(R.id.bookPriceText)
						val editBtn = singleFrame.findViewById<Button>(R.id.editBookBtn)
						val deleteBtn = singleFrame.findViewById<Button>(R.id.deleteBookBtn)

						Picasso.get()
							.load(result)
							.into(bookImg)
						bookGenreText.text = newBook.genre
						bookNameText.text = newBook.name
						bookAuthorText.text = newBook.author
						bookPriceText.text = newBook.price.toLong().toString()
						val book = MyShopBookObject(newBookID, newBook.name, newBook.genre, newBook.author, result.toString(), newBook.price.toLong(), 0F, newBook.quantity, 0, "", newBook.releaseDate, newBook.description, newBook.type)

						editBtn.setOnClickListener {
							editBookDialog(singleFrame.id, book)
						}

						deleteBtn.setOnClickListener {
							deleteBookDialog(singleFrame, book.id)
						}

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

						bookViews.add(singleFrame)
						content?.addView(singleFrame)
					}, 10)
				}
			}
		}
	}

	private fun addBookDialog(view: View) {
		val addBookBtn = view.findViewById<Button>(R.id.addBookBtn)
		val addBookInitValues = ManageShopAddBookToShopDialogInitFilterValues()
		val addBookDialog = ManageShopAddBookToShopDialog(addBookInitValues)
		addBookBtn.setOnClickListener {
			activity?.let {
				addBookDialog.show(it) {
					style(SheetStyle.BOTTOM_SHEET)
					onPositive {
						val data = addBookDialog.getBookObject()
						val bookName = data["name"].toString()
						val bookGenre = data["genre"].toString()
						val bookAuthor = data["author"].toString()
						val bookReleaseDate = data["releaseDate"]
						val bookImage = addBookDialog.getImage()
						val bookPrice = data["price"].toString()
						val bookDesc = data["desc"].toString()
						val bookType = data["type"].toString()
						val bookQuantity = data["quantity"].toString()
						if (bookName.isEmpty() || bookGenre.isEmpty() || bookAuthor.isEmpty() || bookImage.isEmpty() || bookPrice.isEmpty() || bookDesc.isEmpty() || bookType.isEmpty() || bookQuantity.isEmpty()) {
							if (Locale.current.language == "en")
								Toast.makeText(context, "All fields must be filled", Toast.LENGTH_SHORT).show()
							else
								Toast.makeText(context, "Các trường không được phép bỏ trống", Toast.LENGTH_SHORT).show()
						}
						else if (bookPrice.toFloatOrNull() == null || bookQuantity.toLongOrNull() == null)
							if (Locale.current.language == "en")
								Toast.makeText(context, "Price and quantity must be numbers", Toast.LENGTH_SHORT).show()
							else
								Toast.makeText(context, "Giá và số lượng tồn phải là số", Toast.LENGTH_SHORT).show()
						else {
							val dataObj = ManageShopNewBookObject(bookName, bookGenre, bookAuthor, data["releaseDate"] as Timestamp, bookImage, bookPrice.toFloat(), bookDesc, "This decade", bookType, bookQuantity.toLong())

							addNewBook(dataObj)

							this.dismiss()
						}
					}
				}
			}
		}
	}

	private fun boostErrorDialog(hoursLeft: Float) {
		val layoutInflater = LayoutInflater.from(requireContext())
		val view = layoutInflater.inflate(R.layout.manageshop_pushbook_error_dialog, null)
		view.findViewById<TextView>(R.id.boostError3Tv).text = hoursLeft.toString()
		val builder = AlertDialog.Builder(requireContext())
		builder.setView(view)
		val dialog = builder.create()

		view.findViewById<Button>(R.id.dismissBtn).setOnClickListener {
			dialog.dismiss()
		}

		dialog.show()
	}

	private fun addBookSuccessDialog() {
		val layoutInflater = LayoutInflater.from(requireContext())
		val view = layoutInflater.inflate(R.layout.manageshop_addbook_success_dialog, null)
		val builder = AlertDialog.Builder(requireContext())
		builder.setView(view)
		val dialog = builder.create()

		view.findViewById<Button>(R.id.dismissBtn).setOnClickListener {
			dialog.dismiss()
		}

		dialog.show()
	}

	private fun editBook(numId: Int, bookId: String, bookDetail: ManageShopNewBookObject) {
		val db = Firebase.firestore

		// Upload book image to storage
		val storageRef = Firebase.storage.reference
		val bookImage = bookDetail.image[0]
		val imageRef = storageRef.child("Books/${bookId}")
		val baos = ByteArrayOutputStream()
		val bitmap = bookImage.image
		if (bitmap != null) {
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
		}
		val data = baos.toByteArray()
		imageRef.putBytes(data).addOnSuccessListener {
			it.storage.downloadUrl.addOnSuccessListener { result ->
				// Add to Books collection
				val updatedBookDetail = hashMapOf(
					"name" to bookDetail.name,
					"description" to bookDetail.description,
					"genre" to bookDetail.genre,
					"type" to bookDetail.type,
					"author" to bookDetail.author,
					"image" to result,
					"releaseDate" to bookDetail.releaseDate
				)
				val updatedShopBookDetail = hashMapOf(
					"items.${bookId}.price" to bookDetail.price,
					"items.${bookId}.remain" to bookDetail.quantity
				)

				val bookColl = db.collection("books")
				bookColl.whereEqualTo("bookID", bookId).get().addOnSuccessListener {
					val documentId = it.documents[0].id
					bookColl.document(documentId).update(updatedBookDetail as Map<String, Any>)
				}

				storeRef.update(updatedShopBookDetail as Map<String, Any>)

				Handler().postDelayed({
					val singleFrame = bookViews[numId]
					val bookImg = singleFrame.findViewById<ImageView>(R.id.bookImg)
					val bookGenreText = singleFrame.findViewById<TextView>(R.id.bookGenreText)
					val bookNameText = singleFrame.findViewById<TextView>(R.id.bookNameText)
					val bookAuthorText = singleFrame.findViewById<TextView>(R.id.bookAuthorText)
					val bookPriceText = singleFrame.findViewById<TextView>(R.id.bookPriceText)

					Picasso.get().load(result).into(bookImg)
					bookGenreText.text = bookDetail.genre
					bookNameText.text = bookDetail.name
					bookAuthorText.text = bookDetail.author
					bookPriceText.text = bookDetail.price.toLong().toString()
				}, 10)
			}
		}
	}

	private fun editBookDialog(numId: Int, bookDetail: MyShopBookObject) {
		val editBookInitValues = ManageShopAddBookToShopDialogInitFilterValues()
		editBookInitValues.name = bookDetail.name
		editBookInitValues.category = bookDetail.genre
		editBookInitValues.author = bookDetail.author
		editBookInitValues.price = bookDetail.shopPrice.toString()
		editBookInitValues.desc = bookDetail.description
		editBookInitValues.type = bookDetail.type
		editBookInitValues.quantity = bookDetail.remain.toString()
		editBookInitValues.image = bookDetail.image


		val editBookDialog = ManageShopEditBookInShopDialog(editBookInitValues)
		editBookDialog.show(requireContext()) {
			style(SheetStyle.BOTTOM_SHEET)
			onPositive {
				val data = editBookDialog.getBookObject()
				val bookName = data["name"].toString()
				val bookGenre = data["genre"].toString()
				val bookAuthor = data["author"].toString()
				val bookReleaseDate = data["releaseDate"]
				val bookImage = editBookDialog.getImage()
				val bookPrice = data["price"].toString()
				val bookDesc = data["desc"].toString()
				val bookType = data["type"].toString()
				val bookQuantity = data["quantity"].toString()
				if (bookName.isEmpty() || bookGenre.isEmpty() || bookAuthor.isEmpty() || bookImage.isEmpty() || bookPrice.isEmpty() || bookDesc.isEmpty() || bookType.isEmpty() || bookQuantity.isEmpty()) {
					if (Locale.current.language == "en")
						Toast.makeText(context, "All fields must be filled", Toast.LENGTH_SHORT).show()
					else
						Toast.makeText(context, "Các trường không được phép bỏ trống", Toast.LENGTH_SHORT).show()
				}
				else if (bookPrice.toFloatOrNull() == null || bookQuantity.toLongOrNull() == null)
					if (Locale.current.language == "en")
						Toast.makeText(context, "Price and quantity must be numbers", Toast.LENGTH_SHORT).show()
					else
						Toast.makeText(context, "Giá và số lượng tồn phải là số", Toast.LENGTH_SHORT).show()
				else {
					val dataObj = ManageShopNewBookObject(bookName, bookGenre, bookAuthor, data["releaseDate"] as Timestamp, bookImage, bookPrice.toFloat(), bookDesc, "This decade", bookType, bookQuantity.toLong())

					editBook(numId, bookDetail.id, dataObj)

					this.dismiss()
				}
			}
		}
	}

	private fun deleteBookDialog(singleFrame: View, bookId: String) {
		val layoutInflater = LayoutInflater.from(requireContext())
		val view = layoutInflater.inflate(R.layout.manageshop_delete_dialog, null)
		val builder = AlertDialog.Builder(requireContext())
		builder.setView(view)
		val dialog = builder.create()

		view.findViewById<Button>(R.id.deleteYesBtn).setOnClickListener {
			storeRef.get().addOnSuccessListener {
				val deleteBook = hashMapOf(
					"items." + bookId to FieldValue.delete()
				)

				storeRef.update(deleteBook as Map<String, Any>).addOnSuccessListener {
					dialog.dismiss()

					Handler().postDelayed({
						val singleFrame = bookViews[singleFrame.id]
						bookViews.remove(singleFrame)
						(singleFrame.parent as ViewGroup).removeView(singleFrame)
					}, 10)
				}
			}
		}
		view.findViewById<Button>(R.id.deleteNoBtn).setOnClickListener {
			dialog.dismiss()
		}

		dialog.show()
	}

	private fun setBookListViews(view: View, bookList: ArrayList<MyShopBookObject>) {
		// Stop this function if fragment is already destroyed
		if (!isAdded || activity == null) return

		val content = view.findViewById<LinearLayout>(R.id.discountListScrollViewContent)

		for (book in bookList) {
			var singleFrame: View = layoutInflater.inflate(R.layout.manageshop_book_item, null)
			singleFrame.id = bookList.indexOf(book)

			val bookImg = singleFrame.findViewById<ImageView>(R.id.bookImg)
			val bookGenreText = singleFrame.findViewById<TextView>(R.id.bookGenreText)
			val bookNameText = singleFrame.findViewById<TextView>(R.id.bookNameText)
			val bookAuthorText = singleFrame.findViewById<TextView>(R.id.bookAuthorText)
			val bookPriceText = singleFrame.findViewById<TextView>(R.id.bookPriceText)
			val boostBtn = singleFrame.findViewById<Button>(R.id.boostBookBtn)
			val editBtn = singleFrame.findViewById<Button>(R.id.editBookBtn)
			val deleteBtn = singleFrame.findViewById<Button>(R.id.deleteBookBtn)

			Picasso.get()
				.load(book.image)
				.into(bookImg)
			bookGenreText.text = book.genre
			bookNameText.text = book.name
			bookAuthorText.text = book.author
			bookPriceText.text = book.shopPrice.toString()
			var boostTime = abs(Timestamp.now().toDate().time - book.releaseDate.toDate().time) / 1000
			if (boostTime < 14400) {
				boostBtn.text = resources.getString(R.string.manageshop_mybooks_boosted)
				boostBtn.setBackgroundColor(Color.parseColor("#B9B9B9"))
			}

			boostBtn.setOnClickListener {
				if (boostTime < 14400)
					boostErrorDialog(4 - (boostTime / 3600).toFloat())
				else {
					boostBook(book)
					book.releaseDate = Timestamp.now()
					boostTime = 0
					boostBtn.text = resources.getString(R.string.manageshop_mybooks_boosted)
					boostBtn.setBackgroundColor(Color.parseColor("#B9B9B9"))
				}
			}
			editBtn.setOnClickListener {
				editBookDialog(singleFrame.id, book)
			}

			deleteBtn.setOnClickListener {
				deleteBookDialog(singleFrame, book.id)
			}

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

			bookViews.add(singleFrame)
			content.addView(singleFrame)
		}

		content.visibility = View.VISIBLE
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	fun changeFragmentContainer(fragment: Fragment) {
		parentFragmentManager.beginTransaction().replace(R.id.fcvNavigation, fragment).addToBackStack(null).commit()
	}
}