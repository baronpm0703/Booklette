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
import com.example.booklette.databinding.FragmentManageshopBooksBinding
import com.example.booklette.databinding.FragmentMyshopBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso
import kotlin.math.round

/**
 * A simple [Fragment] subclass.
 * Use the [ManageShopBooksFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ManageShopBooksFragment : Fragment() {
//	private var discountHScrollView: HorizontalScrollView? = null
//	private var newArrivalsHScrollView: HorizontalScrollView? = null

	private var _binding: FragmentManageshopBooksBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		_binding = FragmentManageshopBooksBinding.inflate(inflater, container, false)
		val view = binding.root

		val auth = Firebase.auth
		val db = Firebase.firestore

		return view
	}

	private fun addExistingBook() {

	}

	private fun addNewBook() {
		
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	fun changeFragmentContainer(fragment: Fragment) {
		parentFragmentManager.beginTransaction().replace(R.id.fcvNavigation, fragment).addToBackStack(null).commit()
	}
}