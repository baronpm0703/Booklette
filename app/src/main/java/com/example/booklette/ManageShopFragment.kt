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
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.databinding.FragmentManageshopBinding
import com.example.booklette.databinding.FragmentMyshopBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.rpc.Help
import com.squareup.picasso.Picasso
import kotlin.math.round

/**
 * A simple [Fragment] subclass.
 * Use the [ManageShopFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ManageShopFragment : Fragment() {
//	private var discountHScrollView: HorizontalScrollView? = null
//	private var newArrivalsHScrollView: HorizontalScrollView? = null

	private var _binding: FragmentManageshopBinding? = null
	private val binding get() = _binding!!

	val shopTab = MyShopShopFragment()
	val bookTab = MyShopProductList()
	val categoryTab = CategoryFragment()

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		_binding = FragmentManageshopBinding.inflate(inflater, container, false)
		val view = binding.root

		val auth = Firebase.auth
		val db = Firebase.firestore

		// Shop info
		db.collection("accounts").whereEqualTo("UID", auth.uid).get()
			.addOnSuccessListener { documents ->
//				Log.i("haimen", documents.size().toString())
				if (documents.size() != 1) return@addOnSuccessListener	// Failsafe

				for (document in documents) {
					// Get avatar and seller's name
					val usrAvtIV = view.findViewById<ImageView>(R.id.usrAvt)
					Picasso.get()
						.load(document.getString("avt"))
						.into(usrAvtIV)
					val sellerNameTV = view.findViewById<TextView>(R.id.sellerName)
					sellerNameTV.text = document.getString("fullname")

					Handler().postDelayed({
						usrAvtIV.visibility = View.VISIBLE
						sellerNameTV.visibility = View.VISIBLE
					}, 2000)
				}
			}

		val bookFragment = ManageShopBooksFragment()
		view.findViewById<Button>(R.id.myBookBtn).setOnClickListener {
			(context as homeActivity).changeFragmentContainer(
				bookFragment,
				(context as homeActivity).smoothBottomBarStack[(context as homeActivity).smoothBottomBarStack.size - 1]
			)
		}

		val layoutInflater = LayoutInflater.from(requireContext())
		val programDialogLayout = layoutInflater.inflate(R.layout.manageshop_program_choice_dialog, null)
		val builder = AlertDialog.Builder(requireContext())
		builder.setView(programDialogLayout)
		val programDialog = builder.create()
		val discountProgramsFragment = ManageShopDiscountProgramsFragment()
		val shopVouchersFragment = ManageShopShopVouchersFragment()
		programDialogLayout.findViewById<Button>(R.id.discountProgramBtn).setOnClickListener {
			(context as homeActivity).changeFragmentContainer(
				discountProgramsFragment,
				(context as homeActivity).smoothBottomBarStack[(context as homeActivity).smoothBottomBarStack.size - 1]
			)
			programDialog.dismiss()
		}
		programDialogLayout.findViewById<Button>(R.id.shopVoucherBtn).setOnClickListener {
			(context as homeActivity).changeFragmentContainer(
				shopVouchersFragment,
				(context as homeActivity).smoothBottomBarStack[(context as homeActivity).smoothBottomBarStack.size - 1]
			)
			programDialog.dismiss()
		}
		view.findViewById<Button>(R.id.programBtn).setOnClickListener {
			programDialog.show()
		}

		val helpCenterFragment = HelpCenterFragment()
		view.findViewById<Button>(R.id.helpCenterBtn).setOnClickListener {
			(context as homeActivity).changeFragmentContainer(
				helpCenterFragment,
				(context as homeActivity).smoothBottomBarStack[(context as homeActivity).smoothBottomBarStack.size - 1]
			)
		}

		view.findViewById<ImageView>(R.id.backBtn).setOnClickListener {
			requireActivity().onBackPressedDispatcher.onBackPressed()
		}

		return view
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	fun changeFragmentContainer(fragment: Fragment) {
		parentFragmentManager.beginTransaction().replace(R.id.fcvNavigation, fragment).addToBackStack(null).commit()
	}
}