package com.example.booklette

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.booklette.databinding.FragmentMyprofileBinding

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

		// Toggle navigation to My Shop

		return view
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}