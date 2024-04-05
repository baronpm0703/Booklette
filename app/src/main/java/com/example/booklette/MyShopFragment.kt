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
import com.example.booklette.databinding.FragmentMyshopBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso
import kotlin.math.round

/**
 * A simple [Fragment] subclass.
 * Use the [MyShopFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyShopFragment : Fragment() {
//	private var discountHScrollView: HorizontalScrollView? = null
//	private var newArrivalsHScrollView: HorizontalScrollView? = null
	private var discountViews = arrayListOf<View>()
	private var newArrivalsViews = arrayListOf<View>()
	private var bestSellersViews = arrayListOf<View>()

	private var _binding: FragmentMyshopBinding? = null
	private val binding get() = _binding!!

	val shopTab = MyShopShopFragment()
	val bookTab = MyShopProductList()
	val categoryTab = CategoryFragment()

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		_binding = FragmentMyshopBinding.inflate(inflater, container, false)
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

					document.getDocumentReference("store")!!.get().addOnSuccessListener { storeSnapshot ->
						// Get shop's follow counts and average rating score
						val followerTV = view.findViewById<TextView>(R.id.followerCnt)
						followerTV.text = storeSnapshot.get("followers").toString()
						val followingTV = view.findViewById<TextView>(R.id.followingCnt)
						followingTV.text = storeSnapshot.get("following").toString()
						val shopRatingTV = view.findViewById<TextView>(R.id.shopRating)
						shopRatingTV.text = String.format("%.1f", (storeSnapshot.get("rating") as ArrayList<Float>).toFloatArray().average())

						Handler().postDelayed({
							followerTV.visibility = View.VISIBLE
							followingTV.visibility = View.VISIBLE
							followingTV.visibility = View.VISIBLE
						}, 2000)
					}
				}
			}


		val shopTabButton = view.findViewById<Button>(R.id.shopBtn)
		val bookTabButton = view.findViewById<Button>(R.id.bookBtn)
		val categoryTabButton = view.findViewById<Button>(R.id.categoryBtn)

		requireActivity().supportFragmentManager.beginTransaction()
			.replace(R.id.tabDisplayFCV, shopTab)
			.commit()
		shopTabButton.setTextColor(Color.parseColor("#D45555"))

		shopTabButton.setOnClickListener {
			// Set color of the 3 buttons
			shopTabButton.setTextColor(Color.parseColor("#D45555"))
			bookTabButton.setTextColor(Color.parseColor("#000000"))
			categoryTabButton.setTextColor(Color.parseColor("#000000"))

			// Replace FCV's content with this fragment
			requireActivity().supportFragmentManager.beginTransaction()
				.replace(R.id.tabDisplayFCV, shopTab)
				.commit()
		}
		bookTabButton.setOnClickListener {
			// Set color of the 3 buttons
			shopTabButton.setTextColor(Color.parseColor("#000000"))
			bookTabButton.setTextColor(Color.parseColor("#D45555"))
			categoryTabButton.setTextColor(Color.parseColor("#000000"))

			// Replace FCV's content with this fragment
			requireActivity().supportFragmentManager.beginTransaction()
				.replace(R.id.tabDisplayFCV, bookTab)
				.commit()
		}
		categoryTabButton.setOnClickListener {
			// Set color of the 3 buttons
			shopTabButton.setTextColor(Color.parseColor("#000000"))
			bookTabButton.setTextColor(Color.parseColor("#000000"))
			categoryTabButton.setTextColor(Color.parseColor("#D45555"))

			// Replace FCV's content with this fragment
			requireActivity().supportFragmentManager.beginTransaction()
				.replace(R.id.tabDisplayFCV, categoryTab)
				.commit()
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