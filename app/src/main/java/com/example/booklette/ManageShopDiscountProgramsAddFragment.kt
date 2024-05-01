package com.example.booklette

import android.graphics.Bitmap
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract.CommonDataKinds.Im
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.ui.text.intl.Locale
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.databinding.EditBookInShopDialogBinding
import com.example.booklette.databinding.FragmentManageshopBooksBinding
import com.example.booklette.databinding.FragmentManageshopDiscountprogramsAddBinding
import com.example.booklette.databinding.FragmentManageshopDiscountprogramsBinding
import com.example.booklette.databinding.FragmentMyshopBinding
import com.example.booklette.model.*
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
import kotlin.math.round

/**
 * A simple [Fragment] subclass.
 * Use the [ManageShopDiscountProgramsAddFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ManageShopDiscountProgramsAddFragment : Fragment() {
	private lateinit var storeRef: DocumentReference

	private var _binding: FragmentManageshopDiscountprogramsAddBinding? = null

	private val binding get() = _binding!!
	private val bookViews = arrayListOf<View>()

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		_binding = FragmentManageshopDiscountprogramsAddBinding.inflate(inflater, container, false)
		val view = binding.root

		val auth = Firebase.auth
		val db = Firebase.firestore
		db.collection("accounts").whereEqualTo("UID", auth.uid).get()
			.addOnSuccessListener { documents ->
				if (documents.size() != 1) return@addOnSuccessListener    // Failsafe

				for (document in documents) {
					storeRef = document.getDocumentReference("store")!!
				}
			}

		view.findViewById<Button>(R.id.addProgramBtn).setOnClickListener{
			val id = ""
			val name = view.findViewById<EditText>(R.id.programNameET).text.toString()
			val startTime = view.findViewById<EditText>(R.id.starttimeET).text.toString()
			val endTime = view.findViewById<EditText>(R.id.endtimeET).text.toString()
			val discountPercent = view.findViewById<EditText>(R.id.discountPercentET).text.toString()
			val orderLimit = view.findViewById<EditText>(R.id.orderLimitET).text.toString()
			val introduction = view.findViewById<EditText>(R.id.introductionET).text.toString()

			if (name.isEmpty() || startTime.isEmpty() || endTime.isEmpty() || discountPercent.isEmpty() || orderLimit.isEmpty() || introduction.isEmpty()) {
				if (Locale.current.language == "en")
					Toast.makeText(context, "All fields must be filled", Toast.LENGTH_SHORT).show()
				else
					Toast.makeText(context, "Các trường không được phép bỏ trống", Toast.LENGTH_SHORT).show()
			}
			else if (discountPercent.toLongOrNull() == null || orderLimit.toLongOrNull() == null)
				if (Locale.current.language == "en")
					Toast.makeText(context, "% discount, order limit and minimum order must be numbers", Toast.LENGTH_SHORT).show()
				else
					Toast.makeText(context, "Phần trăm giảm giá, đơn tối đa và tối thiểu đơn hàng phải là số", Toast.LENGTH_SHORT).show()
			else {
				val dateParser = SimpleDateFormat("yyyy-MM-dd")
				val startTimeTS = Timestamp(dateParser.parse(startTime))
				val endTimeTS = Timestamp(dateParser.parse(endTime))
				val newProgramObject = DiscountProgramObject(id, name, introduction, startTimeTS, endTimeTS, orderLimit.toLong(), discountPercent.toLong())

				addNewProgram(newProgramObject)

				Handler().postDelayed({

				}, 10)
			}
		}

		return view
	}

	private fun addNewProgram(programObject: DiscountProgramObject) {
		val db = Firebase.firestore

		val discColl = db.collection("discounts")
		discColl.get().addOnSuccessListener {
			val documents = it.documents
			var max: Long = 0
			for (document in documents) {
				val id = document.get("discountID").toString().filter { it.isDigit() }.toLong()

				if (id > max) max = id
			}
			val newDiscountID = "DSC" + (max + 1)

			val newDiscMap = hashMapOf(
				"discountID" to newDiscountID,
				"discountIntroduction" to programObject.discountIntroduction,
				"discountName" to programObject.discountName,
				"discountType" to "product",
				"endDate" to programObject.endDate,
				"orderLimit" to programObject.orderLimit,
				"percent" to programObject.percent,
				"startDate" to programObject.startDate
			)

			discColl.add(newDiscMap).addOnSuccessListener {
				storeRef.get().addOnSuccessListener {
					val updateDiscMap = hashMapOf(
						"items/${programObject.productID}/discount" to newDiscountID
					)
					storeRef.update(updateDiscMap as Map<String, Any>)
				}

				Handler().postDelayed({
					addSuccessDialog()
				}, 10)
			}
		}
	}

	private fun addSuccessDialog() {
		val layoutInflater = LayoutInflater.from(requireContext())
		val view = layoutInflater.inflate(R.layout.manageshop_voucher_add_success_dialog, null)
		val builder = AlertDialog.Builder(requireContext())
		builder.setView(view)
		val dialog = builder.create()

		val discountProgramsFragment = ManageShopDiscountProgramsFragment()
		view.findViewById<Button>(R.id.dismissBtn).setOnClickListener {
			dialog.dismiss()
			(context as homeActivity).changeFragmentContainer(
				discountProgramsFragment,
				(context as homeActivity).smoothBottomBarStack[(context as homeActivity).smoothBottomBarStack.size - 1]
			)
		}

		dialog.show()
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	fun changeFragmentContainer(fragment: Fragment) {
		parentFragmentManager.beginTransaction().replace(R.id.fcvNavigation, fragment).addToBackStack(null).commit()
	}
}