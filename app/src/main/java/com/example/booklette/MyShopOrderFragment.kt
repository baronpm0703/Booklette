package com.example.booklette

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.booklette.databinding.FragmentMyOrderBinding
import com.example.booklette.databinding.FragmentMyShopOrderBinding
import com.mancj.materialsearchbar.MaterialSearchBar


class MyShopOrderFragment : Fragment() {
    private var _binding: FragmentMyShopOrderBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    private lateinit var myOrderItemFragment: MyOrderItemFragment

    private lateinit var label: TextView
    private lateinit var searchBar: MaterialSearchBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    private var lastPressedButton : Button? = null
    private var firstRender: Boolean = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // not allow soft keyboard push fragment up


        // Inflate the layout for this fragment

        _binding = FragmentMyShopOrderBinding.inflate(inflater, container, false)
        val view = binding.root

        val toShipButton : Button = binding.toShipButton
        val shippingButton : Button = binding.shippingButton
        val completedButton : Button = binding.completedButton
        val cancelledButton: Button = binding.cancelledButton
        val returnedButton: Button = binding.returnedButton

        val itemListContainer = binding.itemsRecyclerView // fragmentContainerView
        myOrderItemFragment = MyOrderItemFragment.newInstance(1)
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(itemListContainer.id, myOrderItemFragment)

        transaction.runOnCommit {
            // This code will be executed after the transaction is committed
            Log.d("testButton","click")

        }
        transaction.commit()

        toShipButton.setOnClickListener {

            if (lastPressedButton == toShipButton){
//                    lastPressedButton = null
//                    myOrderItemFragment.unfilter()
                Log.d("testButton","==")
            }
            else if (firstRender){
                resetColorButton()
                firstRender = false
                lastPressedButton = toShipButton
                toShipButton.backgroundTintList = null
                //myOrderItemFragment.processingButton()
                Log.d("testButton","firstRender")
            }
            else{
                resetColorButton()
                lastPressedButton = toShipButton
                toShipButton.backgroundTintList = null
                myOrderItemFragment.processingButton()
                Log.d("testButton","else")
            }
        }

        shippingButton.setOnClickListener {

            if (lastPressedButton == shippingButton){
//                    lastPressedButton = null
//                    myOrderItemFragment.unfilter()
            }
            else{
                resetColorButton()
                lastPressedButton = shippingButton
                shippingButton.backgroundTintList = null
                myOrderItemFragment.deliveredButton()
            }
        }

        completedButton.setOnClickListener{

            if (lastPressedButton == completedButton){
//                    lastPressedButton = null
//                    myOrderItemFragment.unfilter()
            }
            else{
                resetColorButton()
                lastPressedButton = completedButton
                completedButton.backgroundTintList = null
                myOrderItemFragment.completedButton()
            }
        }
        cancelledButton.setOnClickListener {

            if (lastPressedButton == cancelledButton){
//                    lastPressedButton = null
//                    myOrderItemFragment.unfilter()
            }
            else{
                resetColorButton()
                lastPressedButton = cancelledButton
                cancelledButton.backgroundTintList = null
                myOrderItemFragment.cancelledButton()
            }
        }
        returnedButton.setOnClickListener {

            if (lastPressedButton == returnedButton){
//                    lastPressedButton = null
//                    myOrderItemFragment.returnedButton()
            }
            else{
                resetColorButton()
                lastPressedButton = returnedButton
                returnedButton.backgroundTintList = null
                myOrderItemFragment.returnedButton()
            }
        }

        val backButton = binding.backButton
        backButton.setOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }


        label = binding.Label
        searchBar = binding.searchBar
        searchBar.addTextChangeListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s?.toString() ?: ""
                myOrderItemFragment.filterName(searchText)
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        return view
    }
    override fun onResume() {
        super.onResume()
        //reloadFragment()
    }
    private fun reloadFragment() {
        getParentFragmentManager().beginTransaction()
            .detach(this)
            .attach(this)
            .commit()
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
            MyShopOrderFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}