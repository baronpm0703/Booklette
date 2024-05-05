package com.example.booklette

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.booklette.databinding.FragmentManageshopDiscountprogramsBinding
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.firestore
import com.maxkeppeler.sheets.core.SheetStyle
import java.text.SimpleDateFormat

/**
 * A simple [Fragment] subclass.
 * Use the [ManageShopDiscountProgramsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ManageShopDiscountProgramsFragment : Fragment() {
	private lateinit var storeRef: DocumentReference

	private var _binding: FragmentManageshopDiscountprogramsBinding? = null

	private val binding get() = _binding!!
	private val bookViews = arrayListOf<View>()

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		_binding = FragmentManageshopDiscountprogramsBinding.inflate(inflater, container, false)
		val view = binding.root

		val auth = Firebase.auth
		val db = Firebase.firestore
		val discountList = arrayListOf<HashMap<String, Comparable<*>?>>()
		db.collection("accounts").whereEqualTo("UID", auth.uid).get()
			.addOnSuccessListener { documents ->
				if (documents.size() != 1) return@addOnSuccessListener    // Failsafe

				for (document in documents) {
					storeRef = document.getDocumentReference("store")!!

					storeRef.get().addOnSuccessListener {docSnapshot ->
						val books = docSnapshot.get("items") as Map<String, Map<String, Any>>
						for (book in books) {
							val bookValue = book.value

							if (bookValue["discount"] != "") {
								db.collection("discounts").whereEqualTo("discountID", bookValue["discount"]).get().addOnSuccessListener {
									val discountDocs = it.documents

									for (doc in discountDocs) {
										if (doc.get("discountType") == "product") {
											val discountMap = hashMapOf(
												"id" to doc.getString("discountID"),
												"name" to doc.getString("discountName"),
												"introduction" to doc.getString("discountIntroduction"),
												"orderLimit" to (doc.get("orderLimit") as Number).toLong(),
												"percent" to (doc.get("percent") as Number).toLong(),
												"startDate" to doc.get("startDate") as Timestamp,
												"endDate" to doc.get("endDate") as Timestamp,
												"bookID" to book.key
											)

											discountList.add(discountMap)
										}
									}

									if (books.entries.indexOf(book) == books.size - 1) {
										Handler().postDelayed({
											setDiscountListView(view, discountList)
											view.findViewById<TextView>(R.id.noProgramsTV).visibility = View.GONE
										}, 100)
									}
								}
							}
						}
					}
				}
			}

		val addDiscountProgramsFragment = ManageShopDiscountProgramsAddFragment()
		val addProgramBtn = view.findViewById<Button>(R.id.addProgramBtn)
		addProgramBtn.setOnClickListener {
			(context as homeActivity).changeFragmentContainer(
				addDiscountProgramsFragment,
				(context as homeActivity).smoothBottomBarStack[(context as homeActivity).smoothBottomBarStack.size - 1]
			)
		}

		view.findViewById<ImageView>(R.id.backBtn).setOnClickListener {
			requireActivity().onBackPressedDispatcher.onBackPressed()
		}

		return view
	}

	private fun setDiscountListView(view: View, discountList: ArrayList<HashMap<String, Comparable<*>?>>) {
		// Stop this function if fragment is already destroyed
		if (!isAdded || activity == null) return

		val content = view.findViewById<LinearLayout>(R.id.discountListScrollViewContent)

		for (discount in discountList) {
			var singleFrame: View = layoutInflater.inflate(R.layout.manageshop_discount_item, null)
			singleFrame.id = discountList.indexOf(discount)

			val discountNameText = singleFrame.findViewById<TextView>(R.id.discountNameText)
			val discountDateText = singleFrame.findViewById<TextView>(R.id.discountDateText)
			val viewDiscountBtn = singleFrame.findViewById<Button>(R.id.viewDiscountBtn)

			val format = SimpleDateFormat("yyyy-MM-dd")
			val startDate = format.format((discount["startDate"] as Timestamp).toDate())
			val endDate = format.format((discount["endDate"] as Timestamp).toDate())
			discountNameText.text = discount["name"].toString()
			discountDateText.text = startDate + " - " + endDate

			val viewDiscountDialog = ManageShopDiscountViewDialog(discount)
			viewDiscountBtn.setOnClickListener {
				activity?.let {
					viewDiscountDialog.show(it) {
						style(SheetStyle.BOTTOM_SHEET)
						onPositive {
							viewDiscountDialog.dismiss()
						}
					}
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