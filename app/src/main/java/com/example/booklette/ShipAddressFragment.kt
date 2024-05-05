package com.example.booklette

import CheckOutFragment
import ShipAddressObject
import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booklette.databinding.FragmentShipAddressBinding
import com.maxkeppeler.sheets.core.SheetStyle
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.model.CartObject
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle


class ShipAddressFragment : Fragment(){
    private var _binding: FragmentShipAddressBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ShipAddressFragmentRecycleViewAdapter

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var selectedItems: ArrayList<CartObject>


    private var shipAddressList: ArrayList<ShipAddressObject> = ArrayList()
    private lateinit var fragmentManager: FragmentManager // Add this parameter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShipAddressBinding.inflate(inflater, container, false)
        val view = binding.root

        adapter = ShipAddressFragmentRecycleViewAdapter(requireContext(), shipAddressList)
        val listenFromProfile = arguments?.getString("shippingAddressListenFromMyProfile")

        if(listenFromProfile == "shippingAddressFromProfile"){
            binding.saveAddressToCheckOut.isEnabled = false
            binding.saveAddressToCheckOut.setBackgroundResource(R.drawable.button_go_to_check_out_disabled)
        }else{
            binding.saveAddressToCheckOut.isEnabled = true
            binding.saveAddressToCheckOut.setBackgroundResource(R.drawable.button_go_to_check_out)
        }
        selectedItems = arguments?.getParcelableArrayList<CartObject>("SELECTED_ITEMS") ?: ArrayList()
        binding.rvShipAddress.adapter = adapter
        binding.rvShipAddress.layoutManager = LinearLayoutManager(requireContext())



        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvShipAddress)

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
                            }, 500)
                        }
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // Handle swiping action here
            val position = viewHolder.adapterPosition
            val adapter = binding.rvShipAddress.adapter as? ShipAddressFragmentRecycleViewAdapter
            adapter?.let {
                // Retrieve item information before swiping
                val deletedItemInfo = it.getItemInfo(position)
                // Show delete confirmation dialog
                showDeleteConfirmationDialog(deletedItemInfo.toString(), position)
            }
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val clampedDX = if (Math.abs(dX) > 200) {
                if (dX > 0) 200.toFloat() else -200.toFloat()
            } else {
                dX
            }

            // Vẽ background và icon cho Swipe-To-Remove
            val itemView = viewHolder.itemView
            val itemHeight = itemView.bottom.toFloat() - itemView.top.toFloat()
            val itemWidth = itemHeight

            val p = Paint()
            if (clampedDX  < 0) {
                p.color = Color.rgb(200, 0, 0) // Màu đỏ
                val background = RectF(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                val newHeight = itemHeight*0.85f
                val bottomMargin = (itemHeight - newHeight)
                val adjustedBackground = RectF(background.left, background.top + bottomMargin + 4f, background.right, background.bottom-18f)
                c.drawRoundRect(adjustedBackground, 20f, 20f, p)
                val iconSize = 70 // Kích thước của biểu tượng xóa
                val iconMargin = 70// Chuyển đổi itemHeight sang kiểu Int
                val iconLeft = (itemView.right - iconMargin - iconSize ).toInt()
                val iconTop = (itemView.top + (itemHeight - iconSize) / 2 + 20f).toInt()
                val iconRight = (itemView.right - iconMargin).toInt()
                val iconBottom = (iconTop + iconSize).toInt()
                val deleteIcon = resources.getDrawable(R.drawable.ic_delete) // Thay R.drawable.ic_delete bằng ID của biểu tượng xóa thực tế
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                deleteIcon.draw(c)
                super.onChildDraw(c, recyclerView, viewHolder, clampedDX, dY, actionState, isCurrentlyActive)
            }
        }
    }

    private fun deleteShipAddressItem(item: ShipAddressObject, position: Int) {
        db.collection("accounts")
            .whereEqualTo("UID", auth.uid!!)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val shippingAddresses = document.get("shippingAddress") as? ArrayList<*>
                    if (shippingAddresses != null) {
                        for ((index, address) in shippingAddresses.withIndex()) {
                            if (address is Map<*, *>) {
                                val typedAddress = address as? Map<String, *>
                                if (typedAddress != null && compareAddressObjects(item, typedAddress)) {
                                    document.reference.update("shippingAddress", FieldValue.arrayRemove(address))
                                        .addOnSuccessListener {
                                            // Xóa thành công, cập nhật RecyclerView
                                            shipAddressList.removeAt(position)
                                            adapter.notifyItemRemoved(position)
                                            MotionToast.createColorToast(
                                                context as Activity,
                                                getString(R.string.delete_cart_sucessfuly),
                                                getString(R.string.delete_notification),
                                                MotionToastStyle.SUCCESS,
                                                MotionToast.GRAVITY_BOTTOM,
                                                MotionToast.SHORT_DURATION,
                                                ResourcesCompat.getFont(context as Activity, www.sanju.motiontoast.R.font.helvetica_regular)
                                            )
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w("hello", "Error deleting document", e)
                                            // Xử lý khi xóa thất bại
                                        }
                                    break  // Kết thúc vòng lặp sau khi xóa
                                }
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting documents: ", exception)
            }
    }

    // Hàm so sánh 2 đối tượng địa chỉ giao hàng
    private fun compareAddressObjects(item: ShipAddressObject, address: Map<String, *>): Boolean {
        // Thực hiện so sánh các trường của địa chỉ giao hàng
        val addressNumber = address["addressNumber"] as? String
        val city = address["city"] as? String
        val province = address["province"] as? String
        val receiverName = address["receiverName"] as? String
        val receiverPhone = address["receiverPhone"] as? String
        val ward = address["ward"] as? String
        val shipLabel = address["shipLabel"] as? String

        return addressNumber == item.addressNumber && city == item.city && province == item.province && ward == item.ward && receiverName == item.receiverName && receiverPhone == item.receiverPhone && shipLabel == item.shipLabel
    }

    private fun showDeleteConfirmationDialog(deletedItemInfo: String, position: Int) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Are you sure you want to delete this item?")

        builder.setPositiveButton("Delete") { dialog, which ->
            val adapter = binding.rvShipAddress.adapter as? ShipAddressFragmentRecycleViewAdapter
            val shipAddressObject = adapter?.getItemInfo(position)
            shipAddressObject?.let { addressItem ->
                deleteShipAddressItem(addressItem, position)
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()

        alertDialog.setOnCancelListener {
            val currentPosition = (binding.rvShipAddress.adapter as? ShipAddressFragmentRecycleViewAdapter)?.getItemInfo(position)
            currentPosition?.let { restoredItem ->
                val adapter = binding.rvShipAddress.adapter as? ShipAddressFragmentRecycleViewAdapter
                adapter?.notifyItemChanged(position)
            }
        }

        alertDialog.show()
    }
}