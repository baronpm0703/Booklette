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
import com.example.booklette.databinding.FragmentManageshopBinding
import com.example.booklette.databinding.FragmentMyshopBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
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
		return view
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