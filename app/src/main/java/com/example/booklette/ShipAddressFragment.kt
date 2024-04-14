package com.example.booklette

import CheckOutFragment
import ShipAddressObject
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booklette.databinding.FragmentShipAddressBinding
import com.maxkeppeler.sheets.core.SheetStyle
import android.graphics.Color
import android.os.Handler
import android.util.Log
import com.example.booklette.model.CartObject
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore


class ShipAddressFragment : Fragment(){
    private var _binding: FragmentShipAddressBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ShipAddressFragmentRecycleViewAdapter

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var selectedItems: ArrayList<CartObject>


    private var shipAddressList: ArrayList<ShipAddressObject> = ArrayList()

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShipAddressBinding.inflate(inflater, container, false)
        val view = binding.root

        adapter = ShipAddressFragmentRecycleViewAdapter(requireContext(), shipAddressList)

        selectedItems = arguments?.getParcelableArrayList<CartObject>("SELECTED_ITEMS") ?: ArrayList()

        binding.rvShipAddress.adapter = adapter
        binding.rvShipAddress.layoutManager = LinearLayoutManager(requireContext())


        auth = Firebase.auth
        db = Firebase.firestore

        if(shipAddressList.size == 0){
            loadShipAddressData()
        }
        else {
            binding.loadingAnim.visibility = View.GONE
            binding.rvShipAddress.visibility = View.VISIBLE
        }

        binding.addShipAddressBtn.setOnClickListener {
            activity?.let {
                AddShipAddressDialog().show(it){
                    style(SheetStyle.BOTTOM_SHEET)
                    title("Add New Address")
                    titleColor(Color.parseColor("#FF0000"))
                }
            }
        }

        binding.ivBackToPrev.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.saveAddressToCheckOut.setOnClickListener {
            val selectedAddress = adapter.getSelectedAddress()
            if (selectedAddress != null) {
                // Pass both the selected address and the cart data to the CheckOutFragment
                val checkOutFragment = CheckOutFragment.passSelectedAddressToCheckOut(selectedAddress)
                checkOutFragment.arguments?.putParcelableArrayList("SELECTED_ITEMS", selectedItems)
                val homeAct = (activity as homeActivity)
                homeAct.supportFragmentManager.popBackStack()
                homeAct.changeFragmentContainer(checkOutFragment, homeAct.smoothBottomBarStack[homeAct.smoothBottomBarStack.size - 1])
            } else {
                // Handle the case where no address is selected
            }
        }

        binding.cartSwipeRefresh.setOnRefreshListener {
            loadShipAddressData()
            binding.cartSwipeRefresh.isRefreshing = false;
        }

        binding.ivBackToPrev.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        return view
    }

    fun loadShipAddressData() {
        shipAddressList.clear()
        binding.loadingAnim.visibility = View.VISIBLE

        binding.rvShipAddress.visibility = View.GONE

        db.collection("accounts").whereEqualTo("UID", auth.uid).get()
            .addOnSuccessListener { documents ->
                if (documents.size() != 1) return@addOnSuccessListener
                for (document in documents) {
                    // Get avatar and seller's name
                    if (document.data.get("shippingAddress") != null) {
                        val shippingAddressArray = document.data.get("shippingAddress") as? ArrayList<Map<String, Any>>
                        shippingAddressArray?.let { shippingAddressArrayData ->
                            for (item in shippingAddressArrayData) {
                                val receiverName = item["receiverName"] as? String ?: ""
                                val receiverPhone = item["receiverPhone"] as? String ?: ""
                                val province = item["province"] as? String ?: ""
                                val city = item["city"] as? String ?: ""
                                val ward = item["ward"] as? String ?: ""
                                val addressNumber = item["addressNumber"] as? String ?: ""
                                val shipLabel = item["shipLabel"] as? String ?: ""
                                shipAddressList.add(
                                    ShipAddressObject(
                                        receiverName,
                                        receiverPhone,
                                        province,
                                        city,
                                        ward,
                                        addressNumber,
                                        shipLabel,
                                        false
                                        )
                                )
                            }

                            adapter.notifyDataSetChanged()

                            Handler().postDelayed({
                                // Code to be executed after the delay
                                // For example, you can start a new activity or update UI elements
                                binding.rvShipAddress.visibility =
                                    View.VISIBLE

                                binding.loadingAnim.visibility =
                                    View.GONE
                            }, 2000)
                        }
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}