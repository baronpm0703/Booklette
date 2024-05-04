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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.booklette.databinding.FragmentMyOrderBinding
import com.mancj.materialsearchbar.MaterialSearchBar


class MyOrderFragment : Fragment() {
    private var _binding: FragmentMyOrderBinding? = null
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
    private var lastReturnPressedButton : Button? = null
    private var firstRender: Boolean = true
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // not allow soft keyboard push fragment up


        // Inflate the layout for this fragment

        _binding = FragmentMyOrderBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.ivHomeProfile.setOnClickListener({
            (context as homeActivity).changeContainerToProfileFragment()
        })

        binding.ivNotificationIcon.setOnClickListener({
            (context as homeActivity).changeContainerToProfileFragment()
        })

        val processingButton : Button = binding.processingButton
        val deliveredButton : Button = binding.deliveredButton
        val completedButton : Button = binding.completedButton
        val cancelledButton: Button = binding.cancelledButton
        val returnedButton: Button = binding.returnedButton
        val returnButtonCaseLayout = binding.returnButtonCaseLayout

        val itemListContainer = binding.itemsRecyclerView // fragmentContainerView
        myOrderItemFragment = MyOrderItemFragment.newInstance(1)
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(itemListContainer.id, myOrderItemFragment)

        transaction.runOnCommit {
            // This code will be executed after the transaction is committed
            Log.d("testButton","click")
            processingButton.performClick()
        }
        transaction.commit()

        processingButton.setOnClickListener {

            if (lastPressedButton == processingButton){
//                    lastPressedButton = null
//                    myOrderItemFragment.unfilter()
                Log.d("testButton","==")
            }
            else if (firstRender){
                resetColorButton()
                firstRender = false
                lastPressedButton = processingButton
                processingButton.backgroundTintList = null
                //myOrderItemFragment.processingButton()
                Log.d("testButton","firstRender")
            }
            else{
                resetColorButton()
                lastPressedButton = processingButton
                processingButton.backgroundTintList = null
                myOrderItemFragment.processingButton()
                Log.d("testButton","else")
            }
            returnButtonCaseLayout.visibility = View.GONE
            processingButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            deliveredButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            completedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            cancelledButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            returnedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

        deliveredButton.setOnClickListener {

            if (lastPressedButton == deliveredButton){
//                    lastPressedButton = null
//                    myOrderItemFragment.unfilter()
            }
            else{
                resetColorButton()
                lastPressedButton = deliveredButton
                deliveredButton.backgroundTintList = null
                myOrderItemFragment.deliveredButton()
            }
            returnButtonCaseLayout.visibility = View.GONE
            processingButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            deliveredButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            completedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            cancelledButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            returnedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
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
            returnButtonCaseLayout.visibility = View.GONE
            processingButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            deliveredButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            completedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            cancelledButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            returnedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
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
            returnButtonCaseLayout.visibility = View.GONE
            processingButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            deliveredButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            completedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            cancelledButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            returnedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

        val returnProcessingButton = binding.returnProcessingButton
        val returnSuccessButton = binding.returnSuccessButton
        val returnFailedButton = binding.returnFailedButton
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

            processingButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            deliveredButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            completedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            cancelledButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            returnedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            returnButtonCaseLayout.visibility = View.VISIBLE
            returnProcessingButton.performClick()
        }

        returnProcessingButton.setOnClickListener {
            if (lastReturnPressedButton != returnProcessingButton){
                resetColorReturnButton()
                lastReturnPressedButton = returnProcessingButton
                returnProcessingButton.backgroundTintList = null
                myOrderItemFragment.returnProcessingButton()

                returnProcessingButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                returnSuccessButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                returnFailedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }

        }
        returnSuccessButton.setOnClickListener {
            if (lastReturnPressedButton != returnSuccessButton){
                resetColorReturnButton()
                lastReturnPressedButton = returnSuccessButton
                returnSuccessButton.backgroundTintList = null
                myOrderItemFragment.returnSuccessButton()

                returnProcessingButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                returnSuccessButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                returnFailedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        }
        returnFailedButton.setOnClickListener {
            if (lastReturnPressedButton != returnFailedButton){
                resetColorReturnButton()
                lastReturnPressedButton = returnFailedButton
                returnFailedButton.backgroundTintList = null
                myOrderItemFragment.returnFailedButton()

                returnProcessingButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                returnSuccessButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                returnFailedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }
//        val backButton = binding.backButton
//        backButton.setOnClickListener{
//            requireActivity().onBackPressedDispatcher.onBackPressed()
//        }


//        label = binding.Label
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
    private fun resetColorReturnButton(){
        if (lastReturnPressedButton != null){
            lastReturnPressedButton!!.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
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