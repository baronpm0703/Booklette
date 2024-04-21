package com.example.booklette

import android.graphics.Color
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
import com.example.booklette.model.ManageShopNewBookObject
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso
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

		return view
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