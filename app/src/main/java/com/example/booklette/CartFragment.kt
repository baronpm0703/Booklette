package com.example.booklette

import android.graphics.Color
import android.graphics.Paint
import CartFragmentRecyclerViewAdapter
import CheckOutFragment
import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.RectF
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.databinding.FragmentCartBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import android.util.Log
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.example.booklette.model.CartObject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


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


    private var totalAmount: Float = 0f


    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: CartFragmentRecyclerViewAdapter

    private var isFailedQuery: Boolean = false
    private var cartList: ArrayList<CartObject> = ArrayList()


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        val view = binding.root
        // Add more items as needed


        adapter = CartFragmentRecyclerViewAdapter(requireContext(), cartList)
        binding.rvCart.adapter = adapter
        adapter.onSelectItemClickListener = object : CartFragmentRecyclerViewAdapter.OnSelectItemClickListener {
            override fun onSelectItemClick(selectedItems: ArrayList<CartObject>) {
                val totalAmount = adapter.calculateTotalAmount()
                if (totalAmount == 0f) {
                    binding.btnCheckOut.isEnabled = false
                    binding.btnCheckOut.setBackgroundResource(R.drawable.button_go_to_check_out_disabled)
                } else {
                    binding.btnCheckOut.isEnabled = true
                    binding.btnCheckOut.setBackgroundResource(R.drawable.button_go_to_check_out)

                }
                val afterFomartedTotalAmount = String.format("%,.0f", totalAmount)
                binding.totalAmount.text = "$afterFomartedTotalAmount VND"
            }
        }

        binding.rvCart.visibility = View.GONE
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())


        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvCart)


        auth = Firebase.auth
        db = Firebase.firestore

        Handler().postDelayed({
            if (binding.loadingAnim.visibility == View.VISIBLE) {
                binding.loadingAnim.visibility = View.GONE
                binding.noCartItemFound.visibility = View.VISIBLE
            }
        }, 10000)

        if(cartList.size == 0){
            loadCartData()
        }
        else {
            binding.loadingAnim.visibility = View.GONE
            binding.rvCart.visibility = View.VISIBLE
            binding.noCartItemFound.visibility = View.GONE
        }

        binding.totalAmount.text = "0 VND"

        binding.btnCheckOut.setOnClickListener {
            // Create a new instance of CheckOutFragment and pass the selectedItems
            val checkOutFragment = CheckOutFragment.passSelectedItemToCheckOut(adapter.getSelectedItems())
            // Navigate to the CheckOutFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fcvNavigation, checkOutFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.cartSwipeRefresh.setOnRefreshListener {
            loadCartData()
            binding.cartSwipeRefresh.isRefreshing = false;
        }

        return view
    }

    fun loadCartData() {
        cartList.clear()

        binding.loadingAnim.visibility = View.VISIBLE
        binding.rvCart.visibility = View.GONE
        binding.noCartItemFound.visibility = View.GONE

        db.collection("accounts").whereEqualTo("UID", auth.uid).get()
            .addOnSuccessListener { documents ->
                if (documents.size() != 1) return@addOnSuccessListener
                for (document in documents) {
                    // Get avatar and seller's name
                    if (document.data.get("cart") != null) {
                        val cartArray = document.data.get("cart") as? Map<String, Any>
                        cartArray?.let { cartListData ->
                            for (item in cartListData) {
                                val itemId = item.key
                                val storeId = (item.value as? Map<String, Any>)?.get("storeID") as? String // Corrected "storeID" to match your data
                                val quantity = (item.value as? Map<String, Any>)?.get("quantity") as? Number
                                if(itemId != null && storeId != null){
                                    db.collection("accounts").whereEqualTo("UID", storeId).get()
                                        .addOnSuccessListener { storeDocument ->
                                            if (storeDocument != null) {
                                                for (eachStore in storeDocument) {
                                                    eachStore.getDocumentReference("store")!!
                                                        .get()
                                                        .addOnSuccessListener { personalStoreDocument ->
                                                            val itemList =
                                                                personalStoreDocument["items"] as? Map<String, Any>
                                                            val eachItem =
                                                                itemList?.get(itemId) as? Map<String, Any>
                                                            val storeName = personalStoreDocument["storeName"]
                                                            db.collection("books")
                                                                .whereEqualTo("bookID", itemId)
                                                                .get()
                                                                .addOnSuccessListener { bookDocument ->
                                                                    for (eachBook in bookDocument) {
                                                                        cartList.add(
                                                                            CartObject(
                                                                                eachBook.data["bookID"].toString(),
                                                                                storeId.toString(),
                                                                                storeName.toString(),
                                                                                eachBook.data["image"].toString(),
                                                                                eachBook.data["name"].toString(),
                                                                                eachBook.data["author"].toString(),
                                                                                eachItem?.get("price")
                                                                                    .toString()
                                                                                    .toFloat(),
                                                                                quantity?.toInt()
                                                                                    ?: 1,
                                                                            )
                                                                        )
                                                                        adapter.notifyDataSetChanged()

                                                                        Handler().postDelayed({
                                                                            // Code to be executed after the delay
                                                                            // For example, you can start a new activity or update UI elements
                                                                            binding.rvCart.visibility =
                                                                                View.VISIBLE
                                                                            binding.loadingAnim.visibility =
                                                                                View.GONE
                                                                            binding.noCartItemFound.visibility =
                                                                                View.GONE
                                                                        }, 2000)
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
                            }
                        }

                    }
                }
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
//        _binding = null
    }


    private val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Handle swiping action here
        val position = viewHolder.adapterPosition
        val adapter = binding.rvCart.adapter as? CartFragmentRecyclerViewAdapter
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
            val adjustedBackground = RectF(background.left, background.top + bottomMargin -3f, background.right, background.bottom-10f)
            c.drawRoundRect(adjustedBackground, 50f, 50f, p)
            val iconSize = 70 // Kích thước của biểu tượng xóa
            val iconMargin = 70// Chuyển đổi itemHeight sang kiểu Int
            val iconLeft = (itemView.right - iconMargin - iconSize ).toInt()
            val iconTop = (itemView.top + (itemHeight - iconSize) / 2 + 26f).toInt()
            val iconRight = (itemView.right - iconMargin).toInt()
            val iconBottom = (iconTop + iconSize).toInt()
            val deleteIcon = resources.getDrawable(R.drawable.ic_delete) // Thay R.drawable.ic_delete bằng ID của biểu tượng xóa thực tế
            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            deleteIcon.draw(c)
            super.onChildDraw(c, recyclerView, viewHolder, clampedDX, dY, actionState, isCurrentlyActive)
        }
    }
}

    private fun showDeleteConfirmationDialog(deletedItemInfo: String, position: Int) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Are you sure you want to delete this item?")

        builder.setPositiveButton("Delete") { dialog, which ->
            val adapter = binding.rvCart.adapter as? CartFragmentRecyclerViewAdapter
            adapter?.removeItem(position)
            adapter?.notifyItemRemoved(position)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()

        val currentPosition = (binding.rvCart.adapter as? CartFragmentRecyclerViewAdapter)?.getItemInfo(position)

        alertDialog.setOnCancelListener {
            currentPosition?.let { restoredItem ->
                val adapter = binding.rvCart.adapter as? CartFragmentRecyclerViewAdapter
                adapter?.notifyItemChanged(position)
            }
        }

        alertDialog.show()
    }
}