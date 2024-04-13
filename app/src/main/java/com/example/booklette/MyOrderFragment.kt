package com.example.booklette

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.graphics.Color
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.databinding.FragmentMyOrderBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.mancj.materialsearchbar.MaterialSearchBar


class MyOrderFragment : Fragment() {
    private var _binding: FragmentMyOrderBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    private lateinit var myOrderItemFragment: MyOrderItemFragment

    private lateinit var label: TextView
    private lateinit var materialSearchBar: MaterialSearchBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    private var lastPressedButton : Button? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentMyOrderBinding.inflate(inflater, container, false)
        val view = binding.root

        val processingButton : Button = binding.processingButton
        val completedButton : Button = binding.completedButton
        val cancelledButton: Button = binding.cancelledButton
        val returnedButton: Button = binding.returnedButton

        val itemListContainer = binding.itemsRecyclerView // fragmentContainerView
        myOrderItemFragment = MyOrderItemFragment.newInstance(1)
        childFragmentManager.beginTransaction()
            .replace(itemListContainer.id,myOrderItemFragment)
            .commit()

        processingButton.setOnClickListener {
            resetColorButton()
            if (myOrderItemFragment != null) {
                if (lastPressedButton == processingButton){
                    lastPressedButton = null
                    myOrderItemFragment.unfilter()
                }
                else{
                    lastPressedButton = processingButton
                    processingButton.backgroundTintList = null
                    myOrderItemFragment.processingButton()
                }
            }
        }
        completedButton.setOnClickListener{
            resetColorButton()
            if (myOrderItemFragment != null) {
                if (lastPressedButton == completedButton){
                    lastPressedButton = null
                    myOrderItemFragment.unfilter()
                }
                else{
                    lastPressedButton = completedButton
                    completedButton.backgroundTintList = null
                    myOrderItemFragment.completedButton()
                }

            }
        }
        cancelledButton.setOnClickListener {
            resetColorButton()
            if (myOrderItemFragment != null) {
                if (lastPressedButton == cancelledButton){
                    lastPressedButton = null
                    myOrderItemFragment.unfilter()
                }
                else{
                    lastPressedButton = cancelledButton
                    cancelledButton.backgroundTintList = null
                    myOrderItemFragment.cancelledButton()
                }

            }
        }
        returnedButton.setOnClickListener {
            resetColorButton()
            if (myOrderItemFragment != null) {
                if (lastPressedButton == returnedButton){
                    lastPressedButton = null
                    myOrderItemFragment.returnedButton()
                }
                else{
                    lastPressedButton = returnedButton
                    returnedButton.backgroundTintList = null
                    myOrderItemFragment.returnedButton()
                }
            }
        }

        val backButton = binding.backButton
        backButton.setOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }


        label = binding.Label
        materialSearchBar = binding.searchBar


        return view
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun resetColorButton(){
        if (lastPressedButton != null){
            lastPressedButton!!.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MyOrder.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyOrderFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}