package com.example.booklette

import android.graphics.Bitmap
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract.CommonDataKinds.Im
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.ui.text.intl.Locale
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.databinding.EditBookInShopDialogBinding
import com.example.booklette.databinding.FragmentManageshopBooksBinding
import com.example.booklette.databinding.FragmentManageshopDiscountprogramsBinding
import com.example.booklette.databinding.FragmentMyshopBinding
import com.example.booklette.model.HRecommendedBookObject
import com.example.booklette.model.ManageShopNewBookObject
import com.example.booklette.model.MyShopBookObject
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.maxkeppeler.sheets.core.SheetStyle
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import kotlin.math.abs
import kotlin.math.round

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

		val comingSoonTabBtn = view.findViewById<Button>(R.id.comingSoonTabBtn)
		comingSoonTabBtn.setBackground(context?.let { ContextCompat.getDrawable(it, R.drawable.manageshop_discount_tab_chosen) })
		comingSoonTabBtn.setTextColor(Color.WHITE)

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