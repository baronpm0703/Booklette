package com.example.booklette

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booklette.databinding.FragmentShipAddressBinding

class ShipAddressFragment : Fragment(){
    private var _binding: FragmentShipAddressBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShipAddressBinding.inflate(inflater, container, false)
        val view = binding.root

        val addressList = ArrayList<String>()
        addressList.add("1")
        addressList.add("2")
        addressList.add("2")
        addressList.add("2")

        // Add more items as needed

        val adapter = ShipAddressFragmentRecycleViewAdapter(requireContext(), addressList)
        binding.rvShipAddress.adapter = adapter
        binding.rvShipAddress.layoutManager = LinearLayoutManager(requireContext())

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}