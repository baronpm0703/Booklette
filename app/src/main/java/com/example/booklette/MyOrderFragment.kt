package com.example.booklette

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.databinding.FragmentMyOrderBinding


class MyOrderFragment : Fragment() {
    private var _binding: FragmentMyOrderBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
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
        val childFragment = childFragmentManager.findFragmentById(itemListContainer.id) as? MyOrderItemFragment



        processingButton.setOnClickListener {
            resetColorButton()
            if (childFragment != null) {
                if (lastPressedButton == processingButton){
                    lastPressedButton = null
                    childFragment.unfilter()
                }
                else{
                    lastPressedButton = processingButton
                    processingButton.backgroundTintList = null
                    childFragment.processingButton()
                }
            }
        }
        completedButton.setOnClickListener{
            resetColorButton()
            if (childFragment != null) {
                if (lastPressedButton == completedButton){
                    lastPressedButton = null
                    childFragment.unfilter()
                }
                else{
                    lastPressedButton = completedButton
                    completedButton.backgroundTintList = null
                    childFragment.completedButton()
                }

            }
        }
        cancelledButton.setOnClickListener {
            resetColorButton()
            if (childFragment != null) {
                if (lastPressedButton == cancelledButton){
                    lastPressedButton = null
                    childFragment.unfilter()
                }
                else{
                    lastPressedButton = cancelledButton
                    cancelledButton.backgroundTintList = null
                    childFragment.cancelledButton()
                }

            }
        }
        returnedButton.setOnClickListener {
            resetColorButton()
            if (childFragment != null) {
                if (lastPressedButton == returnedButton){
                    lastPressedButton = null
                    childFragment.returnedButton()
                }
                else{
                    lastPressedButton = returnedButton
                    returnedButton.backgroundTintList = null
                    childFragment.returnedButton()
                }
            }
        }

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