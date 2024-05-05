package com.example.booklette

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.booklette.databinding.FragmentManageshopShopvouchersBinding
import com.example.booklette.model.*
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.firestore
import com.maxkeppeler.sheets.core.SheetStyle
import java.text.SimpleDateFormat

/**
 * A simple [Fragment] subclass.
 * Use the [ManageShopShopVouchersFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ManageShopShopVouchersFragment : Fragment() {
	private lateinit var storeRef: DocumentReference

	private var _binding: FragmentManageshopShopvouchersBinding? = null

	private val binding get() = _binding!!
	private val bookViews = arrayListOf<View>()

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		_binding = FragmentManageshopShopvouchersBinding.inflate(inflater, container, false)
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
						val vouchers = docSnapshot.get("shopVouchers") as ArrayList<String>
						for (voucher in vouchers) {
							db.collection("discounts").whereEqualTo("discountID", voucher).get().addOnSuccessListener {
								val discountDocs = it.documents

								for (doc in discountDocs) {
									if (doc.get("discountType") == "shop") {
										val discountMap = hashMapOf(
											"id" to doc.getString("discountID"),
											"name" to doc.getString("discountName"),
											"introduction" to doc.getString("discountIntroduction"),
											"orderLimit" to (doc.get("orderLimit") as Number).toLong(),
											"minimumOrder" to (doc.get("minimumOrder") as Number).toLong(),
											"percent" to (doc.get("percent") as Number).toLong(),
											"startDate" to doc.get("startDate") as Timestamp,
											"endDate" to doc.get("endDate") as Timestamp
										)

										discountList.add(discountMap)
									}
								}

								if (vouchers.indexOf(voucher) == vouchers.size - 1)
									Handler().postDelayed({
										setDiscountListView(view, discountList)
										view.findViewById<TextView>(R.id.noProgramsTV).visibility = View.GONE
									}, 100)
							}
						}
					}
				}
			}

		val addShopVouchersFragment = ManageShopShopVouchersAddFragment()
		val addVoucherBtn = view.findViewById<Button>(R.id.addVoucherBtn)
		addVoucherBtn.setOnClickListener {
			(context as homeActivity).changeFragmentContainer(
				addShopVouchersFragment,
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

			val viewDiscountDialog = ManageShopVoucherViewDialog(discount)
			viewDiscountBtn.setOnClickListener {
				activity?.let {
					viewDiscountDialog.show(it) {
						style(SheetStyle.BOTTOM_SHEET)
						onPositive {

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