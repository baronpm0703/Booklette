package com.example.booklette

import android.graphics.Color
import android.graphics.Paint
import CartFragmentRecyclerViewAdapter
import CheckoutFragment
import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.RectF
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.databinding.FragmentCartBinding
import com.example.booklette.model.CartObject
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore


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

//    private lateinit var auth: FirebaseAuth
//    private lateinit var db: FirebaseFirestore

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

        val adapter = CartFragmentRecyclerViewAdapter(requireContext(), cartList)
        binding.rvCart.adapter = adapter
        binding.rvCart.visibility = View.GONE
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())


        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvCart)


        val auth = Firebase.auth
        val db = Firebase.firestore

        if(cartList.size == 0){
            binding.loadingAnim.visibility = View.VISIBLE
            binding.rvCart.visibility = View.GONE
            db.collection("accounts").whereEqualTo("UID", auth.uid).get()
                .addOnSuccessListener { documents ->
                    if (documents.size() != 1) return@addOnSuccessListener
                    for (document in documents) {
                        // Get avatar and seller's name
                        if (document.data.get("cart") != null) {
                            val cartArray = document.data.get("cart") as ArrayList<Map<String, Any>>
                            cartArray?.let { cartListData ->
                                for (cartMap in cartListData) {
                                    val itemId = cartMap["itemId"] as? String
                                    val storeId = cartMap["storeId"] as? String
                                    val quantity = cartMap["quantity"] as? Number
                                    if(itemId != null && storeId != null){
                                        db.collection("accounts").whereEqualTo("UID", storeId).get()
                                            .addOnSuccessListener { storeDocument ->
                                                for(eachStore in storeDocument){
                                                    val storeName = eachStore.data.get("fullname")
                                                    eachStore.getDocumentReference("store")!!.get().addOnSuccessListener { personalStoreDocument ->
                                                        val itemList = personalStoreDocument["items"] as? Map<String, Any>
                                                        val eachItem = itemList?.get(itemId) as? Map<String, Any>
                                                        db.collection("books")
                                                            .whereEqualTo("bookID", itemId)
                                                            .get()
                                                            .addOnSuccessListener { bookDocument ->
                                                                for (eachBook in bookDocument) {
                                                                    cartList.add(
                                                                        CartObject(
                                                                            eachBook.data["bokID"].toString(),
                                                                            storeId.toString(),
                                                                            storeName.toString(),
                                                                            eachBook.data["image"].toString(),
                                                                            eachBook.data["name"].toString(),
                                                                            eachBook.data["author"].toString(),
                                                                            eachItem?.get("price").toString().toFloat(),
                                                                            quantity?.toInt() ?: 0
                                                                        )
                                                                    )
                                                                    adapter.notifyDataSetChanged()

                                                                    Handler().postDelayed({
                                                                        // Code to be executed after the delay
                                                                        // For example, you can start a new activity or update UI elements
                                                                        binding.rvCart.visibility = View.VISIBLE
                                                                        binding.loadingAnim.visibility = View.GONE

                                                                    }, 2000)
                                                                }

                                                            }
                                                    }
                                                }
                                            }

                                    }
                                }
                            }
                        }
                    }

                }
        }
        else {
            binding.loadingAnim.visibility = View.GONE
            binding.rvCart.visibility = View.VISIBLE
        }
        binding.btnCheckOut.setOnClickListener {
            val checkOutFragment = CheckoutFragment()

            val ft = activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fcvNavigation, checkOutFragment)
                ?.commit()
        }

        return view
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