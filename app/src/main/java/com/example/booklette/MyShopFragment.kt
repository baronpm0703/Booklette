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

	private var _binding: FragmentMyshopBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val shopTab = MyShopShopFragment()
		val bookTab = MyShopProductList()
		val categoryTab = CategoryFragment()

		// Inflate the layout for this fragment
		_binding = FragmentMyshopBinding.inflate(inflater, container, false)
		val view = binding.root

		val auth = Firebase.auth
		val db = Firebase.firestore

		val manageShopFragment = ManageShopFragment()
		val manageShopBtn = view.findViewById<Button>(R.id.manageShopBtn)
		manageShopBtn.setOnClickListener {
			(context as homeActivity).changeFragmentContainer(
				manageShopFragment,
				(context as homeActivity).smoothBottomBarStack[(context as homeActivity).smoothBottomBarStack.size - 1]
			)
		}

		// Shop info
		db.collection("accounts").whereEqualTo("UID", auth.uid).get()
			.addOnSuccessListener { documents ->
//				Log.i("haimen", documents.size().toString())
				if (documents.size() != 1) return@addOnSuccessListener	// Failsafe

				for (document in documents) {
					// Get avatar
					val usrAvtIV = view.findViewById<ImageView>(R.id.usrAvt)
					Picasso.get()
						.load(document.getString("avt"))
						.into(usrAvtIV)

					Handler().postDelayed({
						usrAvtIV.visibility = View.VISIBLE
					}, 2000)

					document.getDocumentReference("store")!!.get().addOnSuccessListener { storeSnapshot ->
						// Get seller's name
						val sellerNameTV = view.findViewById<TextView>(R.id.sellerName)
						sellerNameTV.text = storeSnapshot.get("storeName").toString()
						// Get shop's follow counts and average rating score
						val followerTV = view.findViewById<TextView>(R.id.followerCnt)
						followerTV.text = storeSnapshot.get("followers").toString()
						val followingTV = view.findViewById<TextView>(R.id.followingCnt)
						followingTV.text = storeSnapshot.get("following").toString()
						val shopRatingTV = view.findViewById<TextView>(R.id.shopRating)
						shopRatingTV.text = String.format("%.1f", (storeSnapshot.get("rating") as ArrayList<Float>).toFloatArray().average())

						Handler().postDelayed({
							sellerNameTV.visibility = View.VISIBLE
							followerTV.visibility = View.VISIBLE
							followingTV.visibility = View.VISIBLE
							followingTV.visibility = View.VISIBLE
						}, 2000)
					}
				}
			}

		requireActivity().supportFragmentManager.beginTransaction()
			.replace(R.id.tabDisplayFCV, shopTab)
			.commit()

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