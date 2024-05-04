package com.example.booklette

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.booklette.databinding.FragmentMyprofileBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso

class MyProfileFragment : Fragment() {
	private var _binding: FragmentMyprofileBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		_binding = FragmentMyprofileBinding.inflate(inflater, container, false)
		val view = binding.root

		val auth = Firebase.auth
		val db = Firebase.firestore

		binding.btnSignOut.setOnClickListener {
			auth.signOut()
			startActivity(Intent(activity, LoginActivity::class.java))
			requireActivity().finish()
		}



		binding.languageBtn.setOnClickListener {
			val homeAct = (activity as homeActivity)
			homeAct.changeFragmentContainer(Language_Fragment(), homeAct.smoothBottomBarStack[homeAct.smoothBottomBarStack.size - 1])
		}

		binding.myReviewBtn.setOnClickListener {
			val homeAct = (activity as homeActivity)
			homeAct.changeFragmentContainer(MyReviewList(), homeAct.smoothBottomBarStack[homeAct.smoothBottomBarStack.size - 1])

		}

		// Toggle navigation to My Shop
		view.findViewById<Button>(R.id.myShopBtn).setOnClickListener {
			if (context is homeActivity) {
				var myShopFragment = MyShopFragment()

				(context as homeActivity).changeFragmentContainer(
					myShopFragment,
					(context as homeActivity).smoothBottomBarStack[(context as homeActivity).smoothBottomBarStack.size - 1]
				)
			}
		}


		// Hải: t add shop order vào
		val myShopOrderButton = view.findViewById<CardView>(R.id.myOrdersOption)
		myShopOrderButton.setOnClickListener{
			if (context is homeActivity) {
				var myShopOrderFragment = MyShopOrderFragment()

				(context as homeActivity).changeFragmentContainer(
					myShopOrderFragment,
					(context as homeActivity).smoothBottomBarStack[(context as homeActivity).smoothBottomBarStack.size - 1]
				)
			}
		}
		// Profile setting
		binding.settingsOption.setOnClickListener {
			val profileSettingFragment = ProfileSettingFragment()

			val homeAct = (activity as homeActivity)
			homeAct.changeFragmentContainer(profileSettingFragment, homeAct.smoothBottomBarStack[homeAct.smoothBottomBarStack.size - 1])
		}
		//
		binding.favorites.setOnClickListener {
			val userFavoritesBookFragment = UserFavoritesBookFragment()
			val homeAct = (activity as homeActivity)
			homeAct.changeFragmentContainer(userFavoritesBookFragment, homeAct.smoothBottomBarStack[homeAct.smoothBottomBarStack.size - 1])
		}

		db.collection("accounts").whereEqualTo("UID", auth.uid).get()
			.addOnSuccessListener { documents ->
//				Log.i("haimen", documents.size().toString())
				if (documents.size() != 1) return@addOnSuccessListener    // Failsafe

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
						shopRatingTV.text = String.format(
							"%.1f",
							(storeSnapshot.get("rating") as ArrayList<Float>).toFloatArray().average()
						)

						Handler().postDelayed({
							followerTV.visibility = View.VISIBLE
							followingTV.visibility = View.VISIBLE
							followingTV.visibility = View.VISIBLE
						}, 2000)
					}
				}
			}

		return view
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}