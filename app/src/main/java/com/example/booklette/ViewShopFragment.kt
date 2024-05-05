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
import com.example.booklette.databinding.FragmentViewshopBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso
import kotlin.math.round

/**
 * A simple [Fragment] subclass.
 * Use the [ViewShopFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewShopFragment : Fragment() {
//	private var discountHScrollView: HorizontalScrollView? = null
//	private var newArrivalsHScrollView: HorizontalScrollView? = null

	private var _binding: FragmentViewshopBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val shopTab = ViewShopShopFragment()

		// Inflate the layout for this fragment
		_binding = FragmentViewshopBinding.inflate(inflater, container, false)
		val view = binding.root

		val auth = Firebase.auth
		val db = Firebase.firestore

		// Shop info
		val args = arguments
		if (args != null) {
			args.getString("shopID")?.let {
				db.collection("personalStores").document(it).get().addOnSuccessListener { storeSnapshot ->
					// Get avatar
					val usrAvtIV = view.findViewById<ImageView>(R.id.usrAvt)
					val avt = storeSnapshot.getString("storeAvatar")
					val defaultAvt = "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Accounts%2Fdefault.png?alt=media&token=fd80f83e-7717-4279-a090-4dc97fa435b9"
					if (!avt.isNullOrEmpty())
						Picasso.get()
							.load(avt)
							.into(usrAvtIV)
					else
						Picasso.get()
							.load(defaultAvt)
							.into(usrAvtIV)
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
						usrAvtIV.visibility = View.VISIBLE
						sellerNameTV.visibility = View.VISIBLE
						followerTV.visibility = View.VISIBLE
						followingTV.visibility = View.VISIBLE
						followingTV.visibility = View.VISIBLE
					}, 2000)
				}
			}
		}

		shopTab.arguments = args
		requireActivity().supportFragmentManager.beginTransaction()
			.replace(R.id.tabDisplayFCV, shopTab)
			.commit()

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