package com.example.booklette

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.booklette.databinding.FragmentManageshopShopvouchersBinding
import com.example.booklette.model.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.firestore

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

		val comingSoonTabBtn = view.findViewById<Button>(R.id.comingSoonTabBtn)
		comingSoonTabBtn.setBackground(context?.let { ContextCompat.getDrawable(it, R.drawable.manageshop_discount_tab_chosen) })
		comingSoonTabBtn.setTextColor(Color.WHITE)

		val addShopVouchersFragment = ManageShopShopVouchersAddFragment()
		val addVoucherBtn = view.findViewById<Button>(R.id.addVoucherBtn)
		addVoucherBtn.setOnClickListener {
			(context as homeActivity).changeFragmentContainer(
				addShopVouchersFragment,
				(context as homeActivity).smoothBottomBarStack[(context as homeActivity).smoothBottomBarStack.size - 1]
			)
		}

		return view
	}

	private fun addNewProgram(voucherObject: ShopVoucherObject) {
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
				"discountIntroduction" to voucherObject.discountIntroduction,
				"discountName" to voucherObject.discountName,
				"discountType" to "product",
				"endDate" to voucherObject.endDate,
				"minimumOrder" to voucherObject.minimumOrder,
				"orderLimit" to voucherObject.orderLimit,
				"percent" to voucherObject.percent,
				"startDate" to voucherObject.startDate
			)
			discColl.add(newDiscMap)
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