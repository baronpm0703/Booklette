package com.example.booklette

import CartFragmentRecyclerViewAdapter
import CategoryFragmentGridViewAdapter
import com.example.booklette.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booklette.databinding.FragmentCartBinding


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CategoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        val view = binding.root

        val bookList = ArrayList<String>()
        bookList.add("1")
        bookList.add("2")
        bookList.add("1")
        bookList.add("2")
        bookList.add("1")
        bookList.add("1")
        bookList.add("2")
        bookList.add("1")
        bookList.add("2")

        // Add more items as needed

        val adapter = CartFragmentRecyclerViewAdapter(requireContext(), bookList)
        binding.rvCart.adapter = adapter
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}