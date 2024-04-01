package com.example.booklette

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booklette.databinding.FragmentShipAddressBinding
import com.maxkeppeler.sheets.core.SheetStyle
import android.graphics.Color
import com.example.booklette.databinding.BankCardRecyclerViewLayoutBinding
import com.example.booklette.databinding.FragmentBankCardBinding


class BankCardFragment : Fragment(){
    private var _binding: FragmentBankCardBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBankCardBinding.inflate(inflater, container, false)
        val view = binding.root

        val bankCardList = ArrayList<String>()
        bankCardList.add("1")
        bankCardList.add("2")
        bankCardList.add("1")
        bankCardList.add("2")


//        addressList.add("2")
//        addressList.add("2")

        // Add more items as needed

        val adapter = BankCardFragmentAdapter(requireContext(), bankCardList)
        binding.rvBankCard.adapter = adapter
        binding.rvBankCard.layoutManager = LinearLayoutManager(requireContext())

        binding.addBankCardBtn.setOnClickListener {
            activity?.let {
                AddBankCardDialog().show(it){
                    style(SheetStyle.BOTTOM_SHEET)
                    title("Add New Bank Card")
                    titleColor(Color.parseColor("#FF0000"))
                }
            }
        }

//        binding.addShipAddressBtn.setOnClickListener {
//            activity?.let {
//                AddShipAddressDialog().show(it){
//                    style(SheetStyle.BOTTOM_SHEET)
//                    title("Add New Address")
//                    titleColor(Color.parseColor("#FF0000"))
//                }
//            }
//        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}